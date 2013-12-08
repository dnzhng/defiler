package common;

/*
 * This class contains the global constants used in DFS
 */

public class Constants {

	/* The below constants indicate that we have approximately 268 MB of
	 * disk space with 67 MB of memory cache; a block can hold upto 32 inodes and
	 * the maximum file size is constrained to be 500 blocks. These are compile
	 * time constants and can be changed during evaluation.  Your implementation
	 * should be free of any hard-coded constants.  
	 */

	public static final int NUM_OF_BLOCKS = 8; // 2^18
	public static final int BLOCK_SIZE = 128; // 1kB
	
	
	// we need to assume min INODE_SIZE is 16 bytes
	public static final int INODE_SIZE = 32; //32 Bytes
	
	// inode block size ... size of Int
	public static final int INODE_BLOCK_SIZE = 4; // 4 bytes
	
	public static final int BLOCK_HEADER_LENGTH = 2;
	
	
	
	public static final int NUM_OF_CACHE_BLOCKS = 2; // 2^16
	public static final int MAX_FILE_SIZE = BLOCK_SIZE*1; // Constraint on the max file size

	public static final int MAX_DFILES = 4; // For recylcing DFileIDs
		
	// Do we need this?
	
	// these are specific to the 32 byte implementation of inodes
	// Guarantees storing files smaller than 75 kb (can do more, but not always going to succeed)
	// of course this is assuming there is always infite amount of storage left
	public static final int MIN_BLOCKS_FOR_FILE = 1;
	public static final int SECOND_BLOCKS_FOR_FILE = 8;
	public static final int THIRD_BLOCKS_FOR_FILE = 16;
	

	/* DStore Operation types */
	public enum DiskOperationType {
		READ, WRITE
	};

	/* Virtual disk file/store name */
	public static final String vdiskName = "DSTORE.dat";
	public static final int INODE_REGION_START = 1;
	
}
