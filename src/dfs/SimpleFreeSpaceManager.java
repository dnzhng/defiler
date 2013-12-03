package dfs;

import common.Constants;

/**
 * Keeps track of free space in the file system and available locations for INodes
 * 
 * @author Scott Valentine
 *
 */
public class SimpleFreeSpaceManager implements FreeSpaceManager {

	boolean[] _inodeFreeMap;
	
	public SimpleFreeSpaceManager(){
		_inodeFreeMap = new boolean[Constants.MAX_DFILES];
	}
	
	
	@Override
	public int allocateBlock() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean allocateBlock(int blockID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void freeBlock(int block) {
		// TODO Auto-generated method stub

	}

	@Override
	public NodeLocation allocatedINode() {
		for(int i = 0; i < _inodeFreeMap.length; ++i){
			if(_inodeFreeMap[i]){
				return locationOf(i);
			}
		}
		return null;
	}

	private NodeLocation locationOf(int i) {
		int offset = Constants.INODE_SIZE * i % Constants.BLOCK_SIZE;
		int blockNum = Constants.INODE_SIZE*i / Constants.BLOCK_SIZE + Constants.INODE_REGION_START;
		return new NodeLocation(offset, blockNum);
	}


	@Override
	public void freeINode(NodeLocation location) {
		_inodeFreeMap[getIndex(location)] = true;
	}



	@Override
	public boolean allocateINode(NodeLocation location) {
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
