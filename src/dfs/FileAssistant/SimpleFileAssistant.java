package dfs.FileAssistant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import common.Constants;
import common.DFileID;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import dfs.INode;
import dfs.NodeLocation;


/**
 * Keeps track of current files and available file names.
 * 
 * @author Scott Valentine
 *
 */

public class SimpleFileAssistant implements FileAssistant{
	Map<DFileID, NodeLocation> _fileOffsets;
	TreeSet<Integer> _freeNames;
	
	public SimpleFileAssistant(){
		_fileOffsets = new HashMap<DFileID, NodeLocation>();
		initNames();
	}
	
	private void initNames() {
		_freeNames = new TreeSet<Integer>();
		for(int i = 0; i < Constants.MAX_DFILES; ++i){
			_freeNames.add(i);
		}
	}

	public void addFile(DFileID id, NodeLocation location){
		
		if(_freeNames.contains(id.getDFileID())){
			_freeNames.remove(id.getDFileID());
		}
		
		_fileOffsets.put(id, location);
	}
	
	public void removeFile(DFileID id){		
		_fileOffsets.remove(id);
	}
	
	public INode getINode(DFileID id, DBufferCache cache) throws IOException{
		
		// TODO: what happens if id is not an inode?
		NodeLocation location = _fileOffsets.get(id);
		
		DBuffer buffer = cache.getBlock(location.getBlockNumber());
		
		byte[] readBuffer = new byte[Constants.BLOCK_SIZE];
		buffer.read(readBuffer, 0, Constants.BLOCK_SIZE);
		
		byte[] inodeData = new byte[Constants.INODE_SIZE];
		int offset = location.getOffset();
		
		
		for(int i = 0; i< inodeData.length; ++i){
			inodeData[i] = readBuffer[offset + i];
		}
		cache.releaseBlock(buffer);
		return new INode(id, inodeData);
		
	}
	
	@Override
	public List<DFileID> getFiles() {
		
		List<DFileID> res = new ArrayList<DFileID>();
		
		for(DFileID id : _fileOffsets.keySet()){
			res.add(id);
		}
		return res;
	}

	@Override
	public NodeLocation getNodeLocation(DFileID id) {
		return _fileOffsets.get(id);
	}

	@Override
	public DFileID getNextFileID() {
		return new DFileID(_freeNames.first());
	}




}
