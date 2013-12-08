package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import dblockcache.BufferCache;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;

import virtualdisk.IVirtualDisk;
import virtualdisk.VD;

public class CacheTest2 {
	public static void main(String args[]) throws FileNotFoundException, IOException {
		IVirtualDisk vd = new VD("file1", false);
		
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
}
