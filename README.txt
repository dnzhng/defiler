/**********************************************
 * Please DO NOT MODIFY the format of this file
 **********************************************/

/*************************
 * Team Info & Time spent
 *************************/

	Name1: Scott Valentine 	
	NetId1: sdv4	 	
	Time spent: 35 hours 	

	Name2: Daniel Zhang 	
	NetId2: dyz2	 	
	Time spent: 15 hours 	 


/******************
 * Files to submit
 ******************/

	lab4.jar // An executable jar including all the source files and test cases.
	README	// This file filled with the lab implementation details
        DeFiler.log   // (optional) auto-generated log on execution of jar file

/************************
 * Implementation details
 *************************/




/* 
 * This section should contain the implementation details and a overview of the
 * results. You are required to provide a good README document along with the
 * implementation details. In particular, you can pseudocode to describe your
 * implementation details where necessary. However that does not mean to
 * copy/paste your Java code. Rather, provide clear and concise text/pseudocode
 * describing the primary algorithms (for e.g., scheduling choices you used)
 * and any special data structures in your implementation. We expect the design
 * and implementation details to be 3-4 pages. A plain textfile is encouraged.
 * However, a pdf is acceptable. No other forms are permitted.
 *
 * In case of lab is limited in some functionality, you should provide the
 * details to maximize your partial credit.  
 * */
 
 DBuffer/Buffer:
 *******************************************************************************
 We used the Buffer class to extend the DBuffer abstract class. With this, we
 created several instance variables to describe the status of individual buffers:
 1. _blockID: Describes the ID of the buffer for identification 
 2. _isDirty: Describes if the block is dirty, and should be synched with the VDF
 3. _isBusy: Describes if the block is in the process of performing I/O process
 4. _isValid: Describes if the block has valid data written to it and can be accessed
 
 To construct each buffer, we pass an ID and a virtual disk object with which the
 buffer can interact. The rest of the methods were all provided by the given API.
 
 For concurrency control, we put locks around certain methods, such that only 
 one thread can interact with a specific buffer at a time. For example, a thread
 becomes busy if it is either pushing/fetching the VDF, or if the DFS is interacting
 with the buffer. When performing I/O with the VDF, other threads must wait until
 the VDF calls IOComplete before being able to interact with a given thread. 
 
 DBufferCache/BufferCache:
 *******************************************************************************
 We used the BufferCache class to extend the DBufferCache abstract class. The 
 cache is what generates threads whenever buffers must access the VDF, and then
 stores it in blocks within itself. The buffers are stored in an ArrayList, 
 which is an easy way to keep track of all of the buffers. This also allows us
 to implement a simple LRU eviction policy. The most recently used buffers are
 always moved to the end of the list, and the least recently used can be found
 at the front of the list. 
 
 To make sure that the cache is threadsafe (i.e a thread cannot remove a block
 while another thread is using it), we used a HashSet of the held blocks. 
 Whenever a block was held, it was added to the hash set, and other threads must
 wait until that block was removed from the held list before they could hold it
 themselves. This was used to avoid altering the given APIs. The HashSet was
 also used when evicting blocks. If all the blocks were held, then the cache had
 to wait before evicting any blocks. Otherwise, it would evict the first block
 that was not currently held.  
 
 We also added several private methods to the BufferCache class. Essentially, 
 when a block was requested, the cache would first check if it was in the cache.
 Whenever the cache had to fetch or push from/to the VDF, the cache generated a
 fetchthread or a pushthread, depending on the operation, and would return 
 (after checking if it was valid/held) the block from the VDF. 
  

File system make up
*************************************
In terms of the file system organization, we organized the underlying disk into 
three parts:

1.	Block 0: Reserved for metadata for the DFS. Although we did not utilized this in 
	our implementation, one could use this for a number of things. We could use it to keep 
	track of the total number of files, how much total space is used (although we have 
	other ways of determing this), and the variety of constants that we find in 
	Common.Constants class. One could easily use the first block to save these constants, 
	that way we can always load the DFS, even if our Common.Constants have changed. 
2.	Blocks 1 – N, where N is one more than the number of blocks needed to store all 
	of the inodes. These blocks are used to store inode data. Each block holds several 
	inode segments (which is currently 32 bytes per inode) that we can use to read in 
	files. Each inode consists of four 4-byte fields: 
	a.	DFileID number – the id of this file. This allows us to keep consistent 
		naming across reloads of DFS.
	b.	Valid number – if this is -1 (picked since it is not zero), then the inode 
		is valid. Otherwise, the inode is not valid. Not valid inodes represent deleted
		files that have not been overwritten. This way, we can quickly deleted files, but 
		we don’t have to do the work of deleting the data until we need to. This is similar 
		to the way we freed allocated blocks in the heap manager lab.
	c.	Size – this is the size of the file that this inode represents. This allows us 
		to quickly calculate the size of a file. However, it also means that we must edit 
		the inode block every time we write a file.
	d.	Index of the first data block of the file – this is where the block where the 
		data stored in this file starts. Every new file automatically gets one data block, 
		so this should always be a valid block index on the DFS. However, if it is not (i.e. 
		this is a negative number), we assume that this file has no data.
	e.	Everything else – this is garbage to us. Other implementations could leverage 
		this. For instance, we could use this to store pointers to indirect blocks, or all 
		of the data blocks. For simplicity, we decided to implement data blocks as a linked 
		list. More about this below.
3.	Data blocks – This is where all of the clients’ data is stored. Each block is 
	divided into two pieces: metadata, and the actual data. We implemented the blocks 
	such that each block acts as a node in a linked list that represents the entire file. 
	This way, we don’t have to worry about allocating consecutive blocks to make a file 
	fit, and we never have to worry about fragmentation of the underlying disk (almost 
	true, since we give each block to exactly one file, it is possible the a files does 
	not utilize the full block, and we store less data then our maximum possible). Our 
	implementation of the metadata includes two values.

	a. The current size of the file in the block (since it is possible that the file does not 
		utilized the entire block), 
	b. The index in the VDS of the next block in the file (if this is < 0, we assume that 
		the current block is the last one). 

One downside to this is that we lose some space in the block that we devote the metadata. 
In our implementation, this amounts to two integers (8 bytes total), so a file that is 500 
blocks long, loses 512*8 =4096 bytes (4kbs).   Also, it would be very inefficient if we 
allowed for random accessing of the file. Since we always start at the beginning of a file,
 we will have to visit each block until the end of a write anyways, so effectively it does 
 not require more block requests than another implementation. 



DFS layer synchronization.
*********************************************
The only synchronization that happens at the DFS level is that only one thread can read, 
write, get the size of, or delete a file at a time. Obviously, there are issues if we let 
multiple clients try to operate on a file at a time. To implement this, each method that 
takes a DFileID as an argument, executes the code in a synchronized(DFileID) block. Also, 
when creating a file is synchronized with respect to the DFS object. This way only one 
thread can create a file at a time – preventing multiple clients from making the same 
files (i.e files that share the DFileID integer value). 



Resource Management:
**********************************************
FileAssistant – Keeps track of files.
	1.	Keeps a map of each DFileID to the location on disk of the file’s inode
	2.	Keeps track of the file names that can be used if a client creates a file
FreeSpaceManager – Manages free and allocated space on the disk.
	1.	Keeps track of all of the allocated blocks on disk. Uses a linked list, similar 
		to what we saw in the heapmanager lab. However, since blocks are only allocated
		one at a time, it isn’t important to prevent fragmentation.
	2.	Keeps track of free locations in the inode region of the VDS. 

This has methods that frees and allocates data blocks and inode location, respectively. 



Assumptions we made:
**************************************************
We will assume that when recovering from a failure (and rebooting), that
the constants will not change (this means less metadata in the inode blocks)

Size is the RAW DATA SIZE of a file (does not include inode data or block metadata)
	- I think that this is like "file size" and "size on disk" differences

If two threads try to r/w the same file, the second one to request r/w from the DFS will block until the first has finished.

If we have reached the maximum amount of files in DFS, then calling DFS.createFile() will return a null DFileID. It is important
for the clients to take this into account!


DFS does not conform to the standard that test programs call .init(). Why? this 
does not seem like something the TestClient should worry about. The test client 
specifies whether or not to format the DFS instead. If it formats, then there 
is no reason to call init() (everything is already 0). If it decided to not format 
(i.e. tries to load a previous instance of DFS), then it does need to call init().
In the various SimpleDFS constructors, we handle the logic of this. That way, the 
test client only has to worry about to format or not to format.


/************************
 * Feedback on the lab
 ************************/
1. Not have this due during finals. There is enough stress during this time without adding an extra project.

2. Provide a test program. There are a lot of places that this could go wrong. It would be very useful to
	have a program that we know should work correctly. It would save us a lot of time debugging stuff that
	isn't so much related to building a file system.



/************************
 * References
 ************************/

Piazza
Slide deck from lecture.

/*
 * List of collaborators involved including any online references/citations.
 * */
