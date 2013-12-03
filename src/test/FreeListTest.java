package test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.junit.Test;

import dfs.freelist.FreeList;

public class FreeListTest {

	
	public FreeList getFreeList(){
		return new FreeList(10);
	}
	
	private HashSet<Integer> allocatedAFew(FreeList fl){
		HashSet<Integer> allocated = new HashSet<Integer>();
		
		Random rand = new Random();
		
		for(int i = 0; i < 10; ++i){
			allocated.add(fl.allocate());
		}
		return allocated;
	}
	
	@Test
	public void allocateTest() {
		FreeList fl = getFreeList();
		
		HashSet<Integer> allocated = allocatedAFew(fl);
		for(int al : allocated){
			assert(fl.isAllocated(al));
		}
		for(int i =0; i < 10; ++ i){
			if(!allocated.contains(i)){
				assert(!fl.isAllocated(i));
			}
		}
	}
	
	@Test
	public void freeTest(){
		FreeList fl = getFreeList();
		System.out.println(fl);

		HashSet<Integer> alloc = allocatedAFew(fl);
		System.out.println(fl);

		
			fl.free(9);
			System.out.println(fl);

			fl.free(5);
			System.out.println(fl);

			assert(!fl.isAllocated(5));
			assert(!fl.isAllocated(9));
			fl.allocate();
			System.out.println(fl);
		
			assert(fl.isAllocated(5));
			assert(fl.isAllocated(9));
	}

}
