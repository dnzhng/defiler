package test;

import common.DFileID;

import dfs.DFS;

public class TestClient implements Runnable {

	private DFS myDFS;
	

	private byte[] writeBuffer;
	private DFileID myID;

	private boolean exit = false;
	
	public TestClient(DFS dfs, String writeText) {
		myDFS = dfs;
		writeBuffer = writeText.getBytes();

	}

	public void createFile(){
		myID = myDFS.createDFile();
		boolean x = false;
		for( DFileID id : myDFS.listAllDFiles()){
			if(id.equals(myID)){
				x = true;
			}
		}
		assert(x);
	}

	public void writeFile(){
		myDFS.write(myID, writeBuffer, 0, writeBuffer.length);
	}
	
	public void readFile(){
		byte[] buffer = new byte[writeBuffer.length];

		myDFS.read(myID, buffer, 0, writeBuffer.length);

		for (int i = 0; i < buffer.length; ++i) {
			assert (buffer[i] == writeBuffer[i]);
		}
	}
	
	public void destroyFile(){
		byte[] buffer = new byte[writeBuffer.length];
		myDFS.destroyDFile(myID);
		try{
			myDFS.read(myID, buffer, 0, writeBuffer.length);
		}
		catch(NullPointerException e){
			System.out.println("successfully made a file, wrote it, read it back, and deleted it");
		}
		exit = true;
	}
	
	
	
	
	@Override
	public void run() {
		
		// spin it
		while(exit){
			// lala
		}
	}

}
