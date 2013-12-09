package dblockcache;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import virtualdisk.IVirtualDisk;
import virtualdisk.VD;



public class CacheTestPack {
	
	public static void multiblocks() throws FileNotFoundException, IOException {
		System.out.println("This is a typical usage of the cache");
		IVirtualDisk vd = new VD("file1", true);
		
		DBufferCache cache = new BufferCache(3, vd);
		DBuffer testbuf1 = cache.getBlock(1);
		DBuffer testbuf2 = cache.getBlock(2);
		DBuffer testbuf3 = cache.getBlock(3);
		
		byte[] buffer1 = {1, 1}; 
		
		testbuf1.write(buffer1, 0, 2);
		
		byte[] buffer2 = {2, 2};
		
		testbuf2.write(buffer2, 0, 2);
		
		byte[] buffer3 = {3, 3}; 
		
		testbuf3.write(buffer3, 0, 2);
		

		
		cache.releaseBlock(testbuf1);
		cache.releaseBlock(testbuf2);
		cache.releaseBlock(testbuf3);
		
		DBuffer testbuf4 = cache.getBlock(3);
		byte[] buffer4 = {4, 4};
		testbuf4.write(buffer4, 0, 2);

		cache.sync();
		
		cache = new BufferCache(3, vd);
		
		byte[] read1 = new byte[6];
		
		DBuffer x = cache.getBlock(1);
		DBuffer y = cache.getBlock(2);
		DBuffer z = cache.getBlock(3);
		
		x.read(read1, 0, 2);
		y.read(read1, 2, 2);
		z.read(read1, 4, 2);
		
		cache.releaseBlock(x);
		cache.releaseBlock(y);
		cache.releaseBlock(z);
			
		
		for(int i=0; i < read1.length; ++i) {
			System.out.print(read1[i] + " ");
		}
	}
	
	public static void twothreadsoneblock() throws FileNotFoundException, IOException {
		System.out.println("This test shows what happens when two threads try to act on a block\n" +
				"Before it becomes released. The 2nd thread will wait indefinitely\n" +
				"until the block is released\n\n" +
				"NOTE: Please remember to stop the program");
		
		IVirtualDisk vd = new VD("file1", true);
		DBufferCache cache = new BufferCache(1, vd);
		
		System.out.println("Test 1 retrieving block");
		DBuffer test1 = cache.getBlock(1);
		System.out.println("Test 2 retrieving block");
		DBuffer test2 = cache.getBlock(1);
		cache.releaseBlock(test1);
		
	}
	
	public static void testLRU() throws FileNotFoundException, IOException {
		System.out.println("This test attempts to show the LRU in action");
		IVirtualDisk vd = new VD("file1", true);
		DBufferCache cache = new BufferCache(10, vd);
		DBuffer[] buffers = new DBuffer[10];
		
		for (int i = 0; i < 10; i++) {
			buffers[i] = cache.getBlock(i);
			byte[] buffer = {(byte) i, (byte) i};
			buffers[i].write(buffer, 0, 2);
			cache.releaseBlock(buffers[i]);
		}
		
		cache.sync();
		System.out.println("The initial cache looks like this");
		displayCache((BufferCache) cache, 10); //Should be 0 1 2 3 4 5 6 8 9
		
		byte[] temp = new byte[2];
		DBuffer movingBlock0 = cache.getBlock(0);
		movingBlock0.read(temp, 0, 2);
		cache.releaseBlock(movingBlock0);
		System.out.println("The LRU block was used, moved to the MRU position");
		displayCache((BufferCache) cache, 10); //Should be 1 2 3 4 5 6 7 9 0
		
		byte[] newstuff = new byte[2];
		DBuffer replacingBlock1 = cache.getBlock(10); 
		
		replacingBlock1.read(newstuff, 0, 2);
		cache.releaseBlock(replacingBlock1);
		System.out.println("The LRU block has been replaced");
		displayCache((BufferCache) cache, 10); //Should be 2 3 4 5 6 7 9 0 10
		
	}
	
	private static void displayCache(BufferCache cache, int cacheSize) {
		int[] temp = new int[cacheSize];
		for (int i = 0; i < cache.bufferlistcopy.size(); i++) {
			temp[i] = cache.bufferlistcopy.get(i).getBlockID();
		}
		System.out.println(Arrays.toString(temp) + "\n");
	}
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		//@SuppressWarnings("resource")
//		Scanner userInputScanner = new Scanner(System.in);
//		System.out.println("Which test would you like to run?\n" +
//				"1. MultiBlocks\n" +
//				"2. Two Threads One Block\n" +
//				"3. LRU Test");
			multiblocks();
			//twothreadsoneblock();
			testLRU();
		
		
		
	}
}
