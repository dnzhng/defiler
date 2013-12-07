package dfs.FileAssistant;

import java.io.IOException;
import java.util.List;

import common.DFileID;
import dblockcache.DBuffer;
import dfs.INode;
import dfs.NodeLocation;

public interface FileAssistant {

	
	public void addFile(DFileID id, NodeLocation location);
	
	public void removeFile(DFileID id);
	
	public INode getINode(DFileID id, DBuffer buffer) throws IOException;
	
	public List<DFileID> getFiles();
	
	public NodeLocation getNodeLocation(DFileID id);

	public DFileID getNextFileID();
	
}
