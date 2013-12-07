package dfs.FreeSpaceManager;

import dblockcache.DBufferCache;
import dfs.INode;
import dfs.NodeLocation;

public interface FreeSpaceManager {

		
	public int allocateBlock();
	
	public boolean allocateBlock(int blockID);
	
	public void freeBlock(int block);
	
	public NodeLocation allocatedINode();
	
	public void freeINode(NodeLocation location);

	public boolean allocateINode(NodeLocation location);
	
	public void freeFileData(INode file, DBufferCache cache);
	
}
