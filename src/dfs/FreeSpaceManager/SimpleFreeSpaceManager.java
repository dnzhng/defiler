package dfs.FreeSpaceManager;

import common.Constants;
import dfs.NodeLocation;
import dfs.freelist.FreeList;

/**
 * Keeps track of free space in the file system and available locations for INodes
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
	
	
	public SimpleFreeSpaceManager(int size){
		
		_offset = Constants.MAX_DFILES/Constants.BLOCK_SIZE*Constants.INODE_SIZE + 1;
		_freeList = new FreeList(size - _offset);
		initInodeFreeMap();
	}
	
	private void initInodeFreeMap(){
		_inodeFreeMap = new boolean[Constants.MAX_DFILES];
		for(int i =0; i < _inodeFreeMap.length; ++i){
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
		
		if(_freeList.isAllocated(block)){
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
		for(int i = 0; i < _inodeFreeMap.length; ++i){
			if(_inodeFreeMap[i]){
				_inodeFreeMap[i] = false;
				return locationOf(i);
			}
		}
		return null;
	}

	private NodeLocation locationOf(int i) {
		int offset = Constants.INODE_SIZE * i % Constants.BLOCK_SIZE;
		int blockNum = Constants.INODE_SIZE*i / Constants.BLOCK_SIZE + Constants.INODE_REGION_START;
		return new NodeLocation(blockNum, offset);
	}


	@Override
	public synchronized void freeINode(NodeLocation location) {
		_inodeFreeMap[getIndex(location)] = true;
	}



	@Override
	public synchronized boolean allocateINode(NodeLocation location) {
		int index  = getIndex(location);
		if(!_inodeFreeMap[index]){
			return false;
		}
		_inodeFreeMap[index] = false;
		return true;
	}


	private int getIndex(NodeLocation location) {
		return (location.getBlockNumber() - Constants.INODE_REGION_START)*Constants.BLOCK_SIZE / Constants.INODE_SIZE;
	}

}
