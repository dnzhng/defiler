package test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import dfs.DFS;
import dfs.SimpleDFS;

public class SimpleTest {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] clientInfo = {"there","once","was","a","man","who", "named","jan who was struck by lightening. He was ice climbing at the time."};
		
		// error here until implement DFS
		DFS dfs = new SimpleDFS();
	
		List<TestClient> clients = new ArrayList<TestClient>();
		
		for(String s: clientInfo){
			clients.add(new TestClient(dfs, s));
		}
		
		List<Thread> threads = new ArrayList<Thread>();
		
		for(TestClient c: clients){
			Thread t = new Thread(c);
			threads.add(t);
			t.start();
		}
		
		Random r = new Random(911);
	
		for(int i =0; i < threads.size(); ++i){
			clients.get(i).createFile();
			try {
				threads.get(i).sleep(r.nextInt(20));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int i =0; i < threads.size(); ++i){
			clients.get(i).writeFile();
			try {
				threads.get(i).sleep(r.nextInt(20));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for(int i =0; i < threads.size(); ++i){
			clients.get(i).readFile();
			try {
				threads.get(i).sleep(r.nextInt(20));
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		for(int i =0; i < threads.size(); ++i){
			clients.get(i).destroyFile();
		}
	}
}
