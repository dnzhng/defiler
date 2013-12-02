package dfs;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;

import common.Constants;
import common.DFileID;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;

public class SimpleDFS extends DFS {

	private DBufferCache _cache;
	private FileAssistant _fileAssistant;
	private FreeSpaceManager _freeSpaceManager;

	private int getNumberINodeBlocks(int maxDFiles, int iNodeSize, int blockSize) {
		int maxSize = maxDFiles * iNodeSize;
		int numBlocks = maxSize / blockSize;
		return numBlocks;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public DFileID createDFile() {
		DFileID id = _fileAssistant.getNextFileID();

		NodeLocation location = _freeSpaceManager.allocatedINode();

		DBuffer buffer = _cache.getBlock(location.getBlockNumber());
		
		
		
		// TODO: constant
		int dataHead = _freeSpaceManager.allocateBlock(1);
		

		INode emptyFile = new INode(id, true, 0, dataHead);
		byte[] inodeData = emptyFile.toByteArray();
		
		
		byte[] totalBlock = new byte[location.getOffset() + inodeData.length];
		buffer.read(totalBlock, 0, totalBlock.length - inodeData.length);
		
		for(int i = totalBlock.length - inodeData.length; i < totalBlock.length; ++i){
			totalBlock[i] = inodeData[i - totalBlock.length + inodeData.length];
		}
		buffer.write(totalBlock, 0, totalBlock.length);

		_fileAssistant.addFile(id, location);
		return id;
	}

	private byte[] getInitialINode(int id) {
		byte[] res = new byte[Constants.INODE_SIZE];
		byte[] first = BigInteger.valueOf(id).toByteArray();

		byte[] valid = BigInteger.valueOf(0).toByteArray();

		int writeIndex = 0;

		for (int i = 0; i < first.length; ++i) {
			res[writeIndex + i] = first[i];
		}
		writeIndex += 4;
		for (int i = 0; i < valid.length; ++i) {
			res[writeIndex + i] = valid[i];
		}
		writeIndex += 4;

		for (int i = 4; i < res.length; i += 2) {
			byte[] neg = BigInteger.valueOf(-1).toByteArray();
			for (int j = 0; j < neg.length; ++j) {
				res[writeIndex + i] = neg[i];
			}
			writeIndex += 4;

			byte[] zero = BigInteger.valueOf(0).toByteArray();

			for (int j = 0; j < zero.length; ++j) {
				res[writeIndex + i] = zero[i];
			}
			writeIndex += 4;

		}
		return res;
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
				formatBlock(block);
			}

			NodeLocation location = _fileAssistant.getNodeLocation(dFID);
			formatNodeLocation(location);
			_freeSpaceManager.freeINode(location);
			_fileAssistant.removeFile(dFID);
		}
	}

	private void formatBlock(DBuffer buffer) {
		// Does not really have to do anything.

	}

	private void formatNodeLocation(NodeLocation location) {
		DBuffer buff = _cache.getBlock(location.getBlockNumber());
		byte[] buffer = getZeroByteBuffer(Constants.INODE_SIZE);
		buff.write(buffer, location.getOffset(), Constants.INODE_SIZE);
	}

	private byte[] getZeroByteBuffer(int size) {
		byte[] res = new byte[size];
		for (int i = 0; i < res.length; ++i) {
			res[i] = 0;
		}
		return res;
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

			int currentOffset = 0;

			while (currentCount > 0) {
				DBuffer block = _cache.getBlock(currentBlock);
				int readCount = Math.min(currentCount, Constants.BLOCK_SIZE
						- Constants.BLOCK_HEADER_LENGTH * Integer.SIZE);

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
		byte[] headerBuffer = new byte[Integer.SIZE];
		currentBlock.read(headerBuffer, 0, Constants.BLOCK_HEADER_LENGTH
				* Integer.SIZE);

		int[] res = ByteBuffer.wrap(headerBuffer).asIntBuffer().array();
		return res;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		synchronized (dFID) {

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
				// TODO Auto-generated catch block
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
