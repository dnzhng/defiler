package dfs.FileAssistant;

import java.io.IOException;
import java.util.List;

import common.DFileID;
import dblockcache.DBufferCache;
import dfs.INode;
import dfs.NodeLocation;

public interface FileAssistant {

	
	public void addFile(DFileID id, NodeLocation location);
	
	public void removeFile(DFileID id);
	
	public INode getINode(DFileID id, DBufferCache cache) throws IOException;
	
	public List<DFileID> getFiles();
	
	public NodeLocation getNodeLocation(DFileID id);

	public DFileID getNextFileID();
	
}