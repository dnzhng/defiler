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
	
	
	public INode(int[][] fragments){
		_fragments = fragments;
	}
	
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
	
	public List<Integer> getBlocks(int offset, int count){
		List<Integer> res = new ArrayList<Integer>();

		int currentOffset = offset;
		
		int startFragment = 0;
		
		for(int i = 0; i < _fragments.length; ++i){
			if(currentOffset == 0){
				startFragment = i;
				break;
			}
			
			if(currentOffset >= _fragments[i][1]){
				currentOffset -= _fragments[i][1];
			}
			else{
				startFragment = i;
				break;
			}			
		}
		// currOffset is our offset in the block
		
		int currentBlock = _fragments[startFragment][0];
		
		int leftInFrag = _fragments[startFragment][1];
		
		while(currentOffset >= Constants.BLOCK_SIZE && leftInFrag >= Constants.BLOCK_SIZE){
			currentBlock += 1;
			currentOffset -= Constants.BLOCK_SIZE;
			leftInFrag -= Constants.BLOCK_SIZE;
		}
		
		// current offset is the first block to put in the list.
		
		int currentCount = count - offset % Constants.BLOCK_SIZE;
		leftInFrag -= offset % Constants.BLOCK_SIZE;
		if(currentCount != count){
			res.add(currentBlock);
			currentBlock +=1;
		
			leftInFrag -= offset % Constants.BLOCK_SIZE;
			
			if(leftInFrag <= 0){
				startFragment +=1;
				leftInFrag = _fragments[startFragment][1];
				currentBlock = _fragments[startFragment][0];
			}
		}
		
		while(currentCount > 0 && startFragment < _fragments.length){
			
			while(leftInFrag > 0 && currentCount > 0){
				res.add(currentBlock);
				
				currentBlock +=1;
				currentCount -= Constants.BLOCK_SIZE;
				leftInFrag -= Constants.BLOCK_SIZE;
			}
			startFragment +=1;
			
			if(startFragment >= _fragments.length){
				break;
			}
			
			currentBlock = _fragments[startFragment][0];
			leftInFrag = _fragments[startFragment][1];

		}
		return res;
	}

	
	
	
	
}
