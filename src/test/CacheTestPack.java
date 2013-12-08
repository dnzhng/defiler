package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import dblockcache.BufferCache;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import virtualdisk.IVirtualDisk;
import virtualdisk.VD;



public class CacheTestPack {
	
	public static void multiblocks() throws FileNotFoundException, IOException {
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
		//sorry for gross name. 
		
		IVirtualDisk vd = new VD("file1", true);
		DBufferCache cache = new BufferCache(1, vd);
		
		System.out.println("Test 1 retrieving block");
		DBuffer test1 = cache.getBlock(1);
		System.out.println("Test 2 retrieving block");
		DBuffer test2 = cache.getBlock(1);
		cache.releaseBlock(test1);
		
	}
	
	public static void testLRU() throws FileNotFoundException, IOException {
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
		displayCache(cache, 10);
		byte[] temp = new byte[2];
		DBuffer movingBlock1 = cache.getBlock(0);
		movingBlock1.read(temp, 0, 2);
		cache.releaseBlock(movingBlock1);
		displayCache(cache, 10);
		
		byte[] newstuff = new byte[2];
		DBuffer replacingBlock1 = cache.getBlock(10); 
		
		movingBlock1.read(newstuff, 0, 2);
		cache.releaseBlock(replacingBlock1);
		displayCache(cache, 10);
		
	}
	
	private static void displayCache(DBufferCache cache, int cacheSize) {
		byte[] cacheDisplay = new byte[2*cacheSize];
		for(int i = 0; i < cacheSize; i++) {
			DBuffer temp = cache.getBlock(i);
			temp.read(cacheDisplay, 2*i, 2);
			cache.releaseBlock(temp);
		}
		
		System.out.print("The cache looks like: ");
		for (int j = 0; j < cacheDisplay.length; j++) {
			System.out.print(cacheDisplay[j] + " ");
		}
		System.out.print("\n");
	}
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		@SuppressWarnings("resource")
		Scanner userInputScanner = new Scanner(System.in);
		System.out.println("Which test would you like to run?\n" +
				"1. MultiBlocks\n" +
				"2. Two Threads One Block\n" +
				"3. LRU Test");
		int choice = userInputScanner.nextInt();
		if (choice == 1) 
			multiblocks();
		else if (choice == 2)
			twothreadsoneblock();
		else if (choice == 3)
			testLRU();
		else
			System.out.println("This test does not exist");
		
		
		
		
	}
}
