package test;

import java.io.FileNotFoundException;
import java.io.IOException;

import dblockcache.BufferCache;
import dblockcache.DBuffer;
import dblockcache.DBufferCache;
import virtualdisk.IVirtualDisk;
import virtualdisk.VD;

public class CacheTest {

	
	
	
	public static void main(String[] args) throws FileNotFoundException, IOException{
		IVirtualDisk vd = new VD("scot",true);
		
		DBufferCache cache = new BufferCache(1, vd);
		DBuffer x = cache.getBlock(4);
		
		byte[] bufferw = {4,4};
		
		x.write(bufferw, 0, 2);
		
		cache.releaseBlock(x);
		
		x = cache.getBlock(1);
		
		byte[] writ2 = {1,1};
		
		x.write(writ2, 0, 2);
		
		cache.releaseBlock(x);
		
		cache.sync();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		
		cache = new BufferCache(1, vd);
		
		byte[] bufferr = new byte[2];
		DBuffer y = cache.getBlock(1);
		
		y.read(bufferr, 0, 2);
		
		for(int i = 0; i < bufferr.length; ++i){
			System.out.print(bufferr[i] + " ");
		}
		cache.releaseBlock(y);
		y = cache.getBlock(4);
		
		y.read(bufferr, 0, 2);
		
		for(int i = 0; i < bufferr.length; ++i){
			System.out.print(bufferr[i] + " ");
		}
		
	}
}
