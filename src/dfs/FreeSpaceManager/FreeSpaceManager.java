package dfs.FreeSpaceManager;

import dfs.NodeLocation;

public interface FreeSpaceManager {

		
	public int allocateBlock();
	
	public boolean allocateBlock(int blockID);
	
	public void freeBlock(int block);
	
	public NodeLocation allocatedINode();
	
	public void freeINode(NodeLocation location);

	public boolean allocateINode(NodeLocation location);
	
}
