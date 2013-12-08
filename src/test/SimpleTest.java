package test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.Constants;
import common.DFileID;
import dfs.DFS;
import dfs.SimpleDFS;

public class SimpleTest {

	public static void makeFiles(int numClients, int dataAmount) {

		DFS dfs;
		try {
			dfs = new SimpleDFS(Constants.NUM_OF_BLOCKS, true);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		List<TestClient> clients = new ArrayList<TestClient>();

		for (int i = 0; i < numClients; ++i) {
			clients.add(new TestClient(dfs, i,dataAmount));
		}

		List<Thread> threads = new ArrayList<Thread>();

		for (TestClient c : clients) {
			Thread t = new Thread(c);
			threads.add(t);
			t.start();
		}

		Random r = new Random(911);

		for (int i = 0; i < threads.size(); ++i) {
			clients.get(i).createFile();
			try {
				threads.get(i).sleep(r.nextInt(20));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (int i = 0; i < threads.size(); ++i) {
			clients.get(i).writeFile();
			try {
				threads.get(i).sleep(r.nextInt(200));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (int i = 0; i < threads.size(); ++i) {
			clients.get(i).readFile();
			try {
				threads.get(i).sleep(r.nextInt(20));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		dfs.sync();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int numClients = 3;
		int numFiles = numClients;
		
		
		
		makeFiles(numClients, Constants.BLOCK_SIZE*2);
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		loadFilesAndVerify(numFiles);
		
	}

	private static void loadFilesAndVerify(int numFiles) {
		DFS dfs;
		try {
			dfs = new SimpleDFS(Constants.NUM_OF_BLOCKS, false);
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		System.out.println(dfs.listAllDFiles());
		List<DFileID> files = dfs.listAllDFiles();
		DFileID id = files.get(0);

		System.out.println(dfs.sizeDFile(id));

		
		
		
		
		List<TestClient> clients = new ArrayList<TestClient>();

		for (int i = 0; i < numFiles; ++i) {
			clients.add(new TestClient(dfs, i, Constants.BLOCK_SIZE*2 ,files.get(i)) );
		}

		List<Thread> threads = new ArrayList<Thread>();

		for (TestClient c : clients) {
			Thread t = new Thread(c);
			threads.add(t);
			t.start();
		}
		
		for(TestClient client : clients){
			client.readFile();
		}
		
	}
}
