package dfs;

/**
 * 
 * Block number is int of block number on file system (usually first 
 * couple reserved for inodes and metadata)
 * 
 * Offset is the offset in the block in bytes to the first byte of the location.
 * 
 * @author sdv4
 *
 */
public class NodeLocation {

	private int _blockNumber;
	private int _offset;
	
	public NodeLocation(int blockNumber, int offset){
		_blockNumber = blockNumber;
		_offset = offset;
	}

	public int getBlockNumber() {
		return _blockNumber;
	}

	public int getOffset() {
		return _offset;
	}
	
}
