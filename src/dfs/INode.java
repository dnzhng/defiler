package dfs;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import common.Constants;
import common.DFileID;
import dblockcache.DBuffer;

public class INode {
	private DFileID _ID;
	private boolean _valid;
	private int[][] _fragments;
	
	
	public INode(DBuffer iNodeBlock, int offset) throws IOException{
		
		int currentOffset = offset;
		
		// TODO: get rid of this constant
		_ID = new DFileID(readInt(iNodeBlock, currentOffset, Constants.INODE_BLOCK_SIZE));
		
		currentOffset +=Constants.INODE_BLOCK_SIZE;

		int valid = readInt(iNodeBlock, currentOffset, Constants.INODE_BLOCK_SIZE);
		
		if(valid == 0){
			_valid = true;
		}
		else{
			_valid = false;
			return;
		}
		
		// how many fragments?
		// TODO: block size constant
		int numFrags = (Constants.INODE_SIZE - Constants.INODE_BLOCK_SIZE*2) /Constants.INODE_BLOCK_SIZE;
		
		_fragments = new int[numFrags][2];
		
		currentOffset +=Constants.INODE_BLOCK_SIZE;
		for(int i =0; i < numFrags; ++i){
			
			int blockNumber = readInt(iNodeBlock, currentOffset, Constants.INODE_BLOCK_SIZE);
			currentOffset +=Constants.INODE_BLOCK_SIZE;
			int blockSize = readInt(iNodeBlock, currentOffset, Constants.INODE_BLOCK_SIZE);
			currentOffset +=Constants.INODE_BLOCK_SIZE;
			_fragments[i][0] = blockNumber;
			_fragments[i][1] = blockSize;
		}
		
		
		
		
	}
	
	
	private int readInt(DBuffer iNodeBlock, int offset, int size) throws IOException{
		byte[] buffer = new byte[4];
		
		int result = iNodeBlock.read(buffer, offset, 4);
		
		if(result != 0){
			// TODO: what happens when we throw this exception
			throw new IOException();
		}
		return new BigInteger(buffer).intValue();
	}

	public DFileID getDFileID(){
		return _ID;
	}
	
	public boolean isValid(){
		return _valid;
	}
	
	public int getFileSize(){
		int size = 0;
		for(int i = 0; i < _fragments.length; ++i){
			size += _fragments[i][1];
		}
		return size;
	}
	
	public List<Integer> getBlocks(){
		List<Integer> res = new ArrayList<Integer>();
		for(int i = 0; i < _fragments.length; ++i){
			int currentBlock = _fragments[i][0];
			int size = _fragments[i][1];
			
	
			while(size > 0){
				res.add(currentBlock);
				currentBlock +=1;
				size -= Constants.BLOCK_SIZE;
			}
		}
		return res;
	}
	
	
	
	
	
}
