package dfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import common.Constants;
import common.DFileID;
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

	public SimpleDFS(int size){
		_fileAssistant = new SimpleFileAssistant();
		_freeSpaceManager = new SimpleFreeSpaceManager(size);
		// TODO: initiate _cache and lower levels.
	}
	
	
	@Override
	public void init() {

		// TODO: initialize cache and file assistant and free space manger
		int inodeBlocks = Constants.MAX_DFILES*Constants.INODE_SIZE/Constants.BLOCK_SIZE;
		
		for(int i = 1; i <= inodeBlocks; ++i){
			mapFilesInBlock(i);
		}
	}

	private void mapFilesInBlock(int blockID) {
		DBuffer block = _cache.getBlock(blockID);
				
		byte[] buff = new byte[Constants.BLOCK_SIZE];
		
		block.read(buff, 0, Constants.BLOCK_SIZE);
		int offset = 0;
		
		while(offset < buff.length){
			byte[] data = new byte[Constants.INODE_SIZE];
			for(int i = 0; i < Constants.INODE_SIZE; ++i){
				data[i] = buff[offset + i];
			}
			INode currentBlock;
			try {
				currentBlock = new INode(data);
			} catch (IOException e) {
				e.printStackTrace();
				// TODO: BAD BAD BAD
				return;
			}

			if(currentBlock.isValid()){
				addFile(currentBlock, new NodeLocation(blockID, offset));
			}
			offset += Constants.INODE_SIZE;
		}
	}

	private void addFile(INode currentBlock, NodeLocation location) {
		DFileID id = currentBlock.getDFileID();
		
		_fileAssistant.addFile(id, location);
		_freeSpaceManager.allocateINode(location);
		
		int dataBlock = currentBlock.getHeadBlock();

		while(dataBlock > 0){
			_freeSpaceManager.allocateBlock(dataBlock);
			dataBlock = getNextBlock(dataBlock);
		}
	}
	
	


	private int getNextBlock(int dataBlock) {
		return getHeader(_cache.getBlock(dataBlock))[1];
	}

	@Override
	public DFileID createDFile() {
		DFileID id = _fileAssistant.getNextFileID();

		NodeLocation location = _freeSpaceManager.allocatedINode();

		int dataHead = _freeSpaceManager.allocateBlock();
		initEmptyBlock(dataHead);

		INode emptyFile = new INode(id, true, 0, dataHead);
		updateINode(location, emptyFile);
		_fileAssistant.addFile(id, location);
		return id;
	}
	
	private void updateINode(NodeLocation location, INode inode){
		DBuffer buffer = _cache.getBlock(location.getBlockNumber());

		byte[] inodeData = inode.toByteArray();

		byte[] totalBlock = new byte[location.getOffset() + inodeData.length];
		buffer.read(totalBlock, 0, totalBlock.length - inodeData.length);
		
		for(int i = totalBlock.length - inodeData.length; i < totalBlock.length; ++i){
			totalBlock[i] = inodeData[i - totalBlock.length + inodeData.length];
		}
		buffer.write(totalBlock, 0, totalBlock.length);
	}
	
	
	
	private byte[] toByteArray(int val) {
		ByteBuffer dbuf = ByteBuffer.allocate(Integer.SIZE / 8);
		dbuf.putInt(val);
		return dbuf.array();
	}

	private void initEmptyBlock(int blockID){
		DBuffer block = _cache.getBlock(blockID);
		
		//TODO: constants!!!
		byte[] size = toByteArray(0);
		byte[] next = toByteArray(-1);
		
		
		byte[] buffer = new byte[size.length + next.length];
		for(int i = 0; i < size.length; ++ i){
			buffer[i] = size[i];
			buffer[i + size.length] = next[i];
		}
		block.write(buffer, 0, Constants.BLOCK_HEADER_LENGTH*Integer.SIZE/8);	
	}
	
	

	@Override
	public void destroyDFile(DFileID dFID) {
		synchronized (dFID) {
			INode file;
			try {
				file = _fileAssistant.getINode(dFID, _cache);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

			int currentBlock = file.getHeadBlock();
			while (currentBlock > 0) {
				_freeSpaceManager.freeBlock(currentBlock);
				DBuffer block = _cache.getBlock(currentBlock);
				currentBlock = getHeader(block)[1];
			}
			
			NodeLocation location = _fileAssistant.getNodeLocation(dFID);
			
			file.setValid(false);
			
			updateINode(location, file);
			_freeSpaceManager.freeINode(location);
			_fileAssistant.removeFile(dFID);
		}
	}


	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		synchronized (dFID) {
			INode file;
			try {
				file = _fileAssistant.getINode(dFID, _cache);
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
			int currentBlock = file.getHeadBlock();

			int currentCount = count;

			int currentOffset = startOffset;

			while (currentCount > 0) {
				DBuffer block = _cache.getBlock(currentBlock);
				int readCount = Math.min(currentCount, Constants.BLOCK_SIZE
						- Constants.BLOCK_HEADER_LENGTH * Integer.SIZE/8);

				block.read(buffer, currentOffset, readCount);

				currentBlock = getHeader(block)[1];

				if (currentBlock < 0) {
					// BAD!!!
				}
				currentOffset += readCount;
				currentCount -= readCount;
			}

		}

		return 0;
	}

	/**
	 * 
	 * Size at 0 Next Block at 1
	 * 
	 * @param currentBlock
	 * @return
	 */
	private int[] getHeader(DBuffer currentBlock) {
		byte[] headerBuffer = new byte[Constants.BLOCK_SIZE];
		currentBlock.read(headerBuffer, 0, Constants.BLOCK_SIZE);
		
		byte[] metadata = new byte[Constants.BLOCK_HEADER_LENGTH*Integer.SIZE/Byte.SIZE];
		
		for(int i = 0; i < metadata.length; ++i){
			metadata[i] = headerBuffer[headerBuffer.length-1-i];
		}
		
		int[] res = ByteBuffer.wrap(metadata).asIntBuffer().array();
		return res;
	}

	private void setHeader(DBuffer block, int[] header){
		byte[] data = new byte[Constants.BLOCK_SIZE];
		byte[] headerData = getByteArray(header);
		for(int i = 0; i < headerData.length; ++i){
			int offset = Constants.BLOCK_SIZE-Constants.BLOCK_HEADER_LENGTH*Integer.SIZE/8-1;
			data[offset + i] = headerData[i];
		}
		
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

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		synchronized (dFID) {
			INode file;
			try {
				file = _fileAssistant.getINode(dFID, _cache);
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
			
			int prevBlock = -1;
			int currentBlock = file.getHeadBlock();
			int currentOffset = 0;
			int currentCount = count;
			
			while(currentCount > 0){
				if(currentBlock < 0){
					
					if(prevBlock < 0){
						// special case! TODO: take care of this even though we imply it is taken care of elsewhere in the code
					}
					
					currentBlock =_freeSpaceManager.allocateBlock();
					DBuffer last = _cache.getBlock(prevBlock);
					int[] prevHeader = getHeader(last);
					prevHeader[1] = currentBlock;
					prevHeader[0] = Constants.BLOCK_SIZE-Constants.BLOCK_HEADER_LENGTH*Integer.SIZE/Byte.SIZE;
					setHeader(last, prevHeader);
				}
				
				DBuffer current = _cache.getBlock(currentBlock);
				int writeLen = Math.min(currentCount, Constants.BLOCK_SIZE-Constants.BLOCK_HEADER_LENGTH*Integer.SIZE/Byte.SIZE);
				current.write(buffer, currentOffset, writeLen);
				
				currentCount -= writeLen;
				currentOffset += writeLen;
				file.setSize(file.getFileSize() + writeLen);
				
				prevBlock = currentBlock;
				int[] header = getHeader(current);
				header[0] = writeLen;
				currentBlock = header[1];
				setHeader(current, header);
			}
			updateINode(_fileAssistant.getNodeLocation(dFID), file);
		}
		return 0;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		// TODO: return -1 if not valid file
		synchronized (dFID) {
			int fileSize = -1;
			try {
				fileSize = _fileAssistant.getINode(dFID, _cache).getFileSize();
			} catch (IOException e) {
				e.printStackTrace();
			}
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

}
