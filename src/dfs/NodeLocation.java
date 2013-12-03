package dfs;

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
