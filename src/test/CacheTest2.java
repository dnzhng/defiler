package test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import dblockcache.BufferCache;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import virtualdisk.IVirtualDisk;
import virtualdisk.VD;



public class CacheTest2 {
	
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
	
	public static void main(String args[]) throws FileNotFoundException, IOException {
		@SuppressWarnings("resource")
		Scanner userInputScanner = new Scanner(System.in);
		System.out.println("Which test would you like to run?\n" +
				"1. MultiBlocks\n" +
				"2. Two Threads One Block");
		int choice = userInputScanner.nextInt();
		if (choice == 1) 
			multiblocks();
		else if (choice == 2)
			twothreadsoneblock();
		else
			System.out.println("This test does not exist");
		
		
		
		
	}
}
