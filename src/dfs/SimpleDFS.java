package dfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.List;

import virtualdisk.IVirtualDisk;
import virtualdisk.VD;
import common.Constants;
import common.DFileID;
import dblockcache.BufferCache;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import dfs.FileAssistant.FileAssistant;
import dfs.FileAssistant.SimpleFileAssistant;
import dfs.FreeSpaceManager.FreeSpaceManager;
import dfs.FreeSpaceManager.SimpleFreeSpaceManager;

public class SimpleDFS extends DFS {

	private DBufferCache _cache;
	private FileAssistant _fileAssistant;
	private FreeSpaceManager _freeSpaceManager;

	public SimpleDFS(int size, String filename, boolean format)
			throws FileNotFoundException, IOException {
		IVirtualDisk vd = new VD(filename, format);
		initDataStructures(vd, size);
		if(!format){
			init();
		}
	}

	public SimpleDFS(int size, boolean format) throws FileNotFoundException,
			IOException {
		IVirtualDisk vd = new VD(format);
		initDataStructures(vd, size);
		if(!format){
			init();
		}
	}

	public SimpleDFS(int size) throws FileNotFoundException, IOException {
		IVirtualDisk vd = new VD();
		initDataStructures(vd, size);
	}

	@Override
	public void init() {
		int inodeBlocks = Constants.MAX_DFILES * Constants.INODE_SIZE
				/ Constants.BLOCK_SIZE;

		for (int i = Constants.INODE_REGION_START; i < Constants.INODE_REGION_START
				+ inodeBlocks; ++i) {
			mapFilesInBlock(i);
		}
	}

	@Override
	public synchronized DFileID createDFile() {
		DFileID newFileID = _fileAssistant.getNextFileID();
		NodeLocation inodeLocation = _freeSpaceManager.allocatedINode();
		int dataHead = _freeSpaceManager.allocateBlock();
		initEmptyBlock(dataHead);
		INode newFile = new INode(newFileID, true, 0, dataHead);

		// little weird. TODO: maybe fix this?
		updateINode(inodeLocation, newFile);
		_fileAssistant.addFile(newFileID, inodeLocation);
		return newFileID;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		synchronized (dFID) {
			INode file;
			NodeLocation inodeLocation = _fileAssistant.getNodeLocation(dFID);
			DBuffer inodeBuffer = _cache.getBlock(inodeLocation
					.getBlockNumber());
			_cache.releaseBlock(inodeBuffer);
			try {
				file = _fileAssistant.getINode(dFID, inodeBuffer);
			} catch (IOException e) {
				System.err.println("Unable to delete file " + dFID.getDFileID()
						+ ". Problem reading iNode.");
				return;
			}
			_freeSpaceManager.freeFileData(file, _cache);
			file.setValid(false);
			updateINode(inodeLocation, file);
			_freeSpaceManager.freeINode(inodeLocation);
			_fileAssistant.removeFile(dFID);	
		}
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		synchronized (dFID) {
			INode file;

			NodeLocation inodeLocation = _fileAssistant.getNodeLocation(dFID);
			DBuffer inodeBuffer = _cache.getBlock(inodeLocation
					.getBlockNumber());

			try {
				file = _fileAssistant.getINode(dFID, inodeBuffer);
			} catch (IOException e) {
				System.err.println("Unable to read Inode for file "
						+ dFID.getDFileID() + ".");
				_cache.releaseBlock(inodeBuffer);
				return 0;
			}
			_cache.releaseBlock(inodeBuffer);

			int currentBlock = file.getHeadBlock();
			int currentCount = count;
			int currentOffset = startOffset;

			while (currentCount > 0) {
				DBuffer block = _cache.getBlock(currentBlock);
				int readCount = Math.min(currentCount, Constants.BLOCK_SIZE
						- Constants.BLOCK_HEADER_LENGTH * Integer.SIZE
						/ Byte.SIZE);

				int bytesRead = readToBuffer(block, buffer, currentOffset,
						readCount);

				readCount = Math.min(bytesRead, readCount);

				currentBlock = getHeader(block)[1];
				_cache.releaseBlock(block);

				if (currentBlock < 0) {
					break;
				}
				currentOffset += readCount;
				currentCount -= readCount;
			}
			return count - currentCount;
		}
	}

	private int readToBuffer(DBuffer block, byte[] buffer, int offset, int count) {
		int headerLength = Constants.BLOCK_HEADER_LENGTH * Integer.SIZE/Byte.SIZE;
		byte[] blockData = new byte[count + headerLength];
		int retVal = block.read(blockData, 0, count + headerLength);
		retVal -= headerLength;
		for (int i = 0; i < retVal; ++i) {
			buffer[i + offset] = blockData[i + headerLength];
		}
		return retVal;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		
		synchronized (dFID) {
			NodeLocation inodeLocation = _fileAssistant.getNodeLocation(dFID);
			DBuffer inodeBuffer = _cache.getBlock(inodeLocation
					.getBlockNumber());
			INode file;
			try {
				file = _fileAssistant.getINode(dFID, inodeBuffer);
			} catch (IOException e) {
				e.printStackTrace();
				_cache.releaseBlock(inodeBuffer);
				return 0;
			}
			_cache.releaseBlock(inodeBuffer);
			
			int prevBlock = -1;
			int currentBlock = file.getHeadBlock();
			int currentOffset = 0;// should be else
			int currentCount = count;

			while (currentCount > 0) {
				if (currentBlock < 0) {

					if (prevBlock < 0) {
						// special case! TODO: take care of this even though we
						// imply it is taken care of elsewhere in the code
					}

					currentBlock = _freeSpaceManager.allocateBlock();
					initEmptyBlock(currentBlock);
					DBuffer last = _cache.getBlock(prevBlock);
					int[] prevHeader = getHeader(last);
					prevHeader[1] = currentBlock;
					prevHeader[0] = Constants.BLOCK_SIZE
							- Constants.BLOCK_HEADER_LENGTH * Integer.SIZE
							/ Byte.SIZE;
					writeHeader(last, prevHeader);
					_cache.releaseBlock(last);
				}

				DBuffer current = _cache.getBlock(currentBlock);
				int writeLen = Math.min(currentCount, Constants.BLOCK_SIZE
						- Constants.BLOCK_HEADER_LENGTH * Integer.SIZE
						/ Byte.SIZE);
				
				int writtenCount = writeToBuffer(current, buffer, currentOffset, writeLen);
				
				writeLen = Math.min(writtenCount, writeLen);
				
				currentCount -= writeLen;
				currentOffset += writeLen;
				file.setSize(file.getFileSize() + writeLen);

				prevBlock = currentBlock;
				int[] header = getHeader(current);
				header[0] = writeLen;
				currentBlock = header[1];
				writeHeader(current, header);
				_cache.releaseBlock(current);
			}
			updateINode(_fileAssistant.getNodeLocation(dFID), file);
			return count - currentCount;
		}
	}
	
	private int writeToBuffer(DBuffer block, byte[] buffer, int offset, int count){
		int headerLength = Constants.BLOCK_HEADER_LENGTH * Integer.SIZE/Byte.SIZE;
		int retVal = 0;
		byte[] blockData = new byte[count + headerLength];
		retVal = block.read(blockData, 0, headerLength + count);
		retVal -= headerLength;
		for (int i = 0; i < retVal; ++i) {
			 blockData[i + headerLength] = buffer[i + offset];
		}
		
		block.write(blockData, 0, blockData.length);
		return retVal;
	}
	
	
	

	@Override
	public int sizeDFile(DFileID dFID) {
		synchronized (dFID) {
			int fileSize = -1;
			NodeLocation inodeLocation = _fileAssistant.getNodeLocation(dFID);
			DBuffer inodeBuffer = _cache.getBlock(inodeLocation
					.getBlockNumber());
			try {
				fileSize = _fileAssistant.getINode(dFID, inodeBuffer).getFileSize();
			} catch (IOException e) {
				System.err.println("Unable to calculate file size for file " + dFID.getDFileID() + ".");
			}
			_cache.releaseBlock(inodeBuffer);
			return fileSize;
		}
	}

	@Override
	public List<DFileID> listAllDFiles() {
		return _fileAssistant.getFiles();
	}

	@Override
	public void sync() {
		_cache.sync();
	}

	private void initDataStructures(IVirtualDisk vd, int size) {
		_cache = new BufferCache(Constants.NUM_OF_BLOCKS, vd);
		_fileAssistant = new SimpleFileAssistant();
		_freeSpaceManager = new SimpleFreeSpaceManager(size);
	}

	private void mapFilesInBlock(int blockID) {
		DBuffer block = _cache.getBlock(blockID);

		byte[] buffer = new byte[Constants.BLOCK_SIZE];

		block.read(buffer, 0, Constants.BLOCK_SIZE);
		int offset = 0;

		while (offset < buffer.length) {
			byte[] inodeData = new byte[Constants.INODE_SIZE];
			for (int i = 0; i < Constants.INODE_SIZE; ++i) {
				inodeData[i] = buffer[offset + i];
			}
			INode currentBlock;
			try {
				currentBlock = new INode(inodeData);
			} catch (IOException e) {
				System.err.println("Failure to get Inode from block " + blockID
						+ ", and offset " + offset + ".");
				return;
			}

			if (currentBlock.isValid()) {
				addFile(currentBlock, new NodeLocation(blockID, offset));
			}
			else
				System.out.println("Hey");
			offset += Constants.INODE_SIZE;
		}
		_cache.releaseBlock(block);
	}

	private void addFile(INode file, NodeLocation location) {
		DFileID id = file.getDFileID();

		_fileAssistant.addFile(id, location);
		_freeSpaceManager.allocateINode(location);

		int dataBlock = file.getHeadBlock();

		while (dataBlock > 0) {
			_freeSpaceManager.allocateBlock(dataBlock);
			dataBlock = getNextBlock(dataBlock);
		}
	}

	private int getNextBlock(int dataBlock) {
		DBuffer block = _cache.getBlock(dataBlock);
		int retVal = getHeader(block)[1];
		_cache.releaseBlock(block);
		return retVal;
	}

	private void updateINode(NodeLocation location, INode inode) {
		DBuffer buffer = _cache.getBlock(location.getBlockNumber());

		byte[] inodeData = inode.toByteArray();

		byte[] totalBlock = new byte[location.getOffset() + inodeData.length];
		buffer.read(totalBlock, 0, location.getOffset());

		for (int i = location.getOffset(); i < Constants.INODE_SIZE
				+ location.getOffset(); ++i) {
			totalBlock[i] = inodeData[i - location.getOffset()];
		}
		buffer.write(totalBlock, 0, totalBlock.length);
		_cache.releaseBlock(buffer);
	}

	private byte[] toByteArray(int val) {
		ByteBuffer dbuf = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
		dbuf.putInt(val);
		return dbuf.array();
	}

	private void initEmptyBlock(int blockID) {
		DBuffer block = _cache.getBlock(blockID);
		int[] initialFileData = { 0, -1 };
		writeHeader(block, initialFileData);
		_cache.releaseBlock(block);
	}

	/**
	 * 
	 * Size at 0 Next Block at 1
	 * 
	 * @param currentBlock
	 * @return
	 */
	private int[] getHeader(DBuffer currentBlock) {
		
		int count = Constants.BLOCK_HEADER_LENGTH * Integer.SIZE
				/ Byte.SIZE;
		
		byte[] headerBuffer = new byte[count];
		currentBlock.read(headerBuffer, 0, Constants.BLOCK_HEADER_LENGTH * Integer.SIZE
				/ Byte.SIZE);
		return readBytes(headerBuffer);
	}

	private void writeHeader(DBuffer block, int[] header) {
		byte[] headerData = getByteArray(header);
		block.write(headerData, 0, headerData.length);

	}

	private int[] readBytes(byte[] data) {

		int length = data.length / 4;

		if (data.length % 4 != 0) {
			length -= data.length % 4;
		}
		int[] res = new int[length];

		int offset = 0;
		for (int i = 0; i < res.length; ++i) {

			byte[] intData = new byte[4];
			for (int j = 0; j < Integer.SIZE/Byte.SIZE; ++j) {
				intData[j] = data[j + offset];
			}

			res[i] = ByteBuffer.wrap(intData).getInt();
			offset += Integer.SIZE/Byte.SIZE;

		}
		return res;
	}

	private byte[] getByteArray(int[] header) {
		byte[] res = new byte[header.length * Integer.SIZE / Byte.SIZE];

		int offset = 0;
		for (int i = 0; i < header.length; ++i) {
			byte[] val = toByteArray(header[i]);

			for (int j = 0; j < val.length; ++j) {
				res[offset] = val[j];
				offset++;
			}
		}
		return res;
	}

}
