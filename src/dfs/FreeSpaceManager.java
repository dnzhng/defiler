package dfs;

import java.util.List;

public interface FreeSpaceManager {

		
	public int allocateBlock(int numBlocks);
	
	/**
	 * List of all blocks that can be added starting with startingBlock
	 * 
	 * @param numBlocks
	 * @param startingBlock
	 * @return
	 */
	public List<Integer> allocatedBlocks(int numBlocks, int startingBlock);
	
	public void freeBlock(int block);
	
	public NodeLocation allocatedINode();
	
	public void freeINode(NodeLocation location);
	
}
