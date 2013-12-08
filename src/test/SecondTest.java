package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import virtualdisk.IVirtualDisk;
import virtualdisk.VD;
import dblockcache.BufferCache;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;

public class SecondTest {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		IVirtualDisk vd = new VD("file1", false);
		DBufferCache cache = new BufferCache(3, vd);
		
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
