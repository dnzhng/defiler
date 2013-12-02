package dfs;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import common.Constants;
import common.DFileID;
import dblockcache.DBuffer;

public class INode {
	
	
	private DFileID _ID;
	private boolean _valid;
	private int _headBlock;
	private int _size;
	
	
	public INode(byte[] inodeData) throws IOException{
		assert(inodeData.length == Constants.INODE_SIZE);
		
		int offset = 0;
		_ID = new DFileID(readInt(inodeData, offset));
		
		initData(inodeData);
		
	}
	


	public INode(DFileID id, byte[] inodeData){
		_ID = id;
		initData(inodeData);
	}
	
	

	
	
	public INode(DFileID id, boolean valid, int size, int headBlock) {
		_ID = id;
		_valid = valid;
		_size = size;
		_headBlock = headBlock;
	}
	
	private void initData(byte[] data) {
		int offset = Integer.SIZE/8;
		int valid = readInt(data, offset);
		offset += Integer.SIZE/8;
		
		_size = readInt(data, offset);
		offset += Integer.SIZE/8;
		_headBlock = readInt(data, offset);
		
		if(valid == 0){
			_valid = true;
		}
		else{
			_valid = false;
		}
		
	}
	
	
	private int readInt(byte[] data, int offset){
		
		byte[] integer = new byte[Integer.SIZE/8];
		
		for(int i = 0; i < Integer.SIZE/8; ++i){
			integer[i] = data[offset + i];
		}
		ByteBuffer wrapped = ByteBuffer.wrap(integer);
		return wrapped.getInt(0);
	}

	public DFileID getDFileID(){
		return _ID;
	}
	
	public boolean isValid(){
		return _valid;
	}
	
	public int getFileSize(){
		return _size;
	}
	
	public int getHeadBlock(){
		return _headBlock;
	}


	private byte[] toByteArray(int val){
		int integer = val;
		byte[] bytes = new byte[Integer.SIZE/8];
		for (int i = 0; i < Integer.SIZE/8; i++) {
		    bytes[i] = (byte)(integer >>> (i * 8));
		}
		return bytes;
	}
	
	
	public byte[] toByteArray() {
		byte[] res = new byte[Constants.INODE_SIZE];
		
		byte[] id = toByteArray(_ID.getDFileID());
		
		int v = -1;
		if(_valid){
			v = 0;
		}
		byte[] valid = toByteArray(v);
		byte[] size = toByteArray(_size);
		byte[] headBlock = toByteArray(_headBlock);
		
		int offset = 0;
		
		copyArray(res, id, offset);
		offset += Integer.SIZE/8;
		copyArray(res, valid, offset);
		offset += Integer.SIZE/8;
		copyArray(res, size, offset);
		offset += Integer.SIZE/8;
		copyArray(res, headBlock, offset);
		offset += Integer.SIZE/8;
		
		for(int i = offset; i < res.length; i ++){
			res[i] = 0;
		}

		return res;
	}
	
	
	private void copyArray(byte[] copy, byte[] orig, int offset){
		
		for(int i = 0; i < orig.length && i + offset < copy.length; ++i){
			copy[i+offset] = orig[i];
		}
	}
	

	
	
	
	
}
