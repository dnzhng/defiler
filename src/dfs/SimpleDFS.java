package dfs;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

import common.Constants;
import common.DFileID;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;

public class SimpleDFS extends DFS{

	private DBufferCache _cache;
	private FileAssistant _fileAssistant;
	private FreeSpaceManager _freeSpaceManager;
	
	
	
	private int getNumberINodeBlocks(int maxDFiles, int iNodeSize, int blockSize){
		int maxSize = maxDFiles * iNodeSize;
		int numBlocks =  maxSize/ blockSize;
		return numBlocks;
	}
	
	@Override
	public void init() {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public DFileID createDFile() {
		DFileID id = _fileAssistant.getNextFileID();
		
		NodeLocation location = _freeSpaceManager.allocatedINode();

		DBuffer buffer = _cache.getBlock(location.getBlockNumber());
		
		byte[] inodeData = getInitialINode(id.getDFileID());
		
		buffer.write(inodeData, location.getOffset(), Constants.INODE_SIZE);
		
		_fileAssistant.addFile(id, location);
		return id;
	}

	private byte[] getInitialINode(int id) {
		byte[] res = new byte[Constants.INODE_SIZE];
		byte[] first = BigInteger.valueOf(id).toByteArray();
		
		byte[] valid = BigInteger.valueOf(0).toByteArray();
		
		int writeIndex = 0;
		
		for(int i =0; i < first.length; ++i){
			res[writeIndex + i] = first[i];
		}
		writeIndex +=4;
		for(int i =0; i < valid.length; ++i){
			res[writeIndex + i] = valid[i];
		}
		writeIndex +=4;
		
		
		for(int i = 4; i < res.length; i +=2){
			byte[] neg = BigInteger.valueOf(-1).toByteArray();
			for(int j =0; j < neg.length; ++j){
				res[writeIndex + i] = neg[i];
			}
			writeIndex +=4;
			
			
			byte[] zero = BigInteger.valueOf(0).toByteArray();

			for(int j =0; j < zero.length; ++j){
				res[writeIndex + i] = zero[i];
			}
			writeIndex +=4;
			
		}
		return res;
	}

	@Override
	public void destroyDFile(DFileID dFID) {
		INode file;
		try {
			file = _fileAssistant.getINode(dFID, _cache);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		List<Integer> blocks = file.getBlocks();
		
		for(int block: blocks){
			DBuffer buffer = _cache.getBlock(block);
			formatBlock(buffer);
			_freeSpaceManager.freeBlock(block);
		}
		NodeLocation location = _fileAssistant.getNodeLocation(dFID);
		formatNodeLocation(location);
		_freeSpaceManager.freeINode(location);
		_fileAssistant.removeFile(dFID);
	}

	private void formatBlock(DBuffer buffer) {
		// Does not really have to do anything.
		
	}

	private void formatNodeLocation(NodeLocation location) {
		DBuffer buff = _cache.getBlock(location.getBlockNumber());
		byte[] buffer = getZeroByteBuffer(Constants.INODE_SIZE);
		buff.write(buffer, location.getOffset(), Constants.INODE_SIZE);
	}

	private byte[] getZeroByteBuffer(int size) {
		byte[] res = new byte[size];
		for(int i =0; i < res.length; ++i){
			res[i] = 0;
		}
		return res;
	}

	@Override
	public int read(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(DFileID dFID, byte[] buffer, int startOffset, int count) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int sizeDFile(DFileID dFID) {
		//TODO: return -1 if not valid file?
		int fileSize = -1;
		try {
			fileSize = _fileAssistant.getINode(dFID, _cache).getFileSize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileSize;
	}

	@Override
	public List<DFileID> listAllDFiles() {
		return _fileAssistant.getFiles();
	}

	@Override
	public void sync() {
		_cache.sync();
	}

}
