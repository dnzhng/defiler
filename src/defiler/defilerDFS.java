package defiler;
import java.util.*;
import common.DFileID;
import dfs.DFS;

public class defilerDFS extends DFS{
	
	private int DFileIDTracker = 0;

	defilerDFS() {
		super();
		// TODO Auto-generated constructor stub
	} 
	
	public DFileID createDFile() {
		DFileID newFileID = new DFileID(DFileIDTracker);
		DFileIDTracker++;
		return newFileID;
	}
	
	public void destroyDFile(DFileID dFID) {
		// Depending on how we keep track of dfileids, just remove dfileid from
		// whatever we use. 
	}
	
	public int read(DFileID dFID, byte[] ubuffer, int startOffset, int count) {
		
	}
	
	public int write(DFileID dFID, byte[] ubuffer, int startOffset, int count) {
		
	}
	
	public List<DFileID> listAllDFiles() {
		//Depending on how we keep track of DFiles, we can loop through and 
		//Append to the list. 
	}
	
	public void sync() {
		
	}
	

}
