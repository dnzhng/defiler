package dfs.FreeSpaceManager;

import java.nio.ByteBuffer;

import common.Constants;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import dfs.INode;
import dfs.NodeLocation;
import dfs.freelist.FreeList;

/**
 * Keeps track of free space in the file system and available locations for
 * INodes
 * 
 * TODO: make sure free list has the correct offset
 * 
 * @author Scott Valentine
 * 
 */
public class SimpleFreeSpaceManager implements FreeSpaceManager {

	private boolean[] _inodeFreeMap;
	private FreeList _freeList;
	private int _offset;

	public SimpleFreeSpaceManager(int size) {

		_offset = Constants.MAX_DFILES / Constants.BLOCK_SIZE
				* Constants.INODE_SIZE + 1;
		_freeList = new FreeList(size - _offset);
		initInodeFreeMap();
	}

	private void initInodeFreeMap() {
		_inodeFreeMap = new boolean[Constants.MAX_DFILES];
		for (int i = 0; i < _inodeFreeMap.length; ++i) {
			_inodeFreeMap[i] = true;
		}
	}

	@Override
	public synchronized int allocateBlock() {
		return _freeList.allocate() + _offset;
	}

	@Override
	public synchronized boolean allocateBlock(int blockID) {

		int block = blockID - _offset;

		if (_freeList.isAllocated(block)) {
			return false;
		}
		_freeList.allocate(block);
		return true;
	}

	@Override
	public synchronized void freeBlock(int block) {
		_freeList.free(block - _offset);
	}

	@Override
	public synchronized NodeLocation allocatedINode() {
		for (int i = 0; i < _inodeFreeMap.length; ++i) {
			if (_inodeFreeMap[i]) {
				_inodeFreeMap[i] = false;
				return locationOf(i);
			}
		}
		return null;
	}

	private NodeLocation locationOf(int i) {
		int offset = Constants.INODE_SIZE * i % Constants.BLOCK_SIZE;
		int blockNum = Constants.INODE_SIZE * i / Constants.BLOCK_SIZE
				+ Constants.INODE_REGION_START;
		return new NodeLocation(blockNum, offset);
	}

	@Override
	public synchronized void freeINode(NodeLocation location) {
		_inodeFreeMap[getIndex(location)] = true;
	}

	@Override
	public synchronized boolean allocateINode(NodeLocation location) {
		int index = getIndex(location);
		if (!_inodeFreeMap[index]) {
			return false;
		}
		_inodeFreeMap[index] = false;
		return true;
	}

	private int getIndex(NodeLocation location) {
		return (location.getBlockNumber() - Constants.INODE_REGION_START)
				* Constants.BLOCK_SIZE / Constants.INODE_SIZE;
	}

	@Override
	public void freeFileData(INode file, DBufferCache cache) {
		int currentBlock = file.getHeadBlock();
		while (currentBlock > 0) {
			freeBlock(currentBlock);
			DBuffer block = cache.getBlock(currentBlock);
			currentBlock = getHeader(block)[1];
			cache.releaseBlock(block);
		}

	}

	
	// TODO: these are repeated methods.
	private int[] getHeader(DBuffer currentBlock) {
		byte[] headerBuffer = new byte[Constants.BLOCK_SIZE];
		currentBlock.read(headerBuffer, 0, Constants.BLOCK_SIZE);

		byte[] metadata = new byte[Constants.BLOCK_HEADER_LENGTH * Integer.SIZE
				/ Byte.SIZE];

		for (int i = 0; i < metadata.length; ++i) {
			metadata[i] = headerBuffer[headerBuffer.length - 1 - i];
		}
		return readBytes(metadata);
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
			for (int j = offset; j < 4; ++j) {
				intData[j] = data[j];
			}

			res[i] = ByteBuffer.wrap(intData).getInt();
			offset += 4;

		}
		return res;
	}

}
