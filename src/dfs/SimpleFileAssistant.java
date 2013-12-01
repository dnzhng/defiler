package dfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.DFileID;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;


public class SimpleFileAssistant implements FileAssistant{
	Map<DFileID, NodeLocation> _fileOffsets;
	
	public SimpleFileAssistant(){
		_fileOffsets = new HashMap<DFileID, NodeLocation>();
	}
	
	public void addFile(DFileID id, NodeLocation location){
		_fileOffsets.put(id, location);
	}
	
	public void removeFile(DFileID id){
		
		// need to set it to zero!
		
		_fileOffsets.remove(id);
	}
	
	public INode getINode(DFileID id, DBufferCache cache) throws IOException{
		
		// TODO: what happens if id is not an inode?
		NodeLocation location = _fileOffsets.get(id);
		
		DBuffer buffer = cache.getBlock(location.getBlockNumber());
		return new INode(buffer, location.getOffset());
		
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DFileID getNextFileID() {
		// TODO Auto-generated method stub
		return null;
	}




}
