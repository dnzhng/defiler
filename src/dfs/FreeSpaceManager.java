package dfs;

public interface FreeSpaceManager {

		
	public int allocateBlock(int numBlocks);
	
	public void freeBlock(int block);
	
	public NodeLocation allocatedINode();
	
	public void freeINode(NodeLocation location);
	
}
