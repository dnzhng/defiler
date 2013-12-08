/**********************************************
 * Please DO NOT MODIFY the format of this file
 **********************************************/

/*************************
 * Team Info & Time spent
 *************************/

	Name1: Scott Valentine 	// Edit this accordingly
	NetId1: fn	 	// Edit
	Time spent: 20 hours 	// Edit 

	Name2: Daniel Zhang 	// Edit this accordingly
	NetId2: dyz2	 	// Edit
	Time spent: 15 hours 	// Edit 

	Name3: Full Name 	// Edit this accordingly
	NetId3: fn	 	// Edit
	Time spent: 10 hours 	// Edit 
	
	We have all completed the Course Evaluation

/******************
 * Files to submit
 ******************/

	lab4.jar // An executable jar including all the source files and test cases.
	README	// This file filled with the lab implementation details
        DeFiler.log   // (optional) auto-generated log on execution of jar file

/************************
 * Implementation details
 *************************/

Assumption: we will assume that when recovering from a failure (and rebooting), that
the constants will not change (this means less metadata in the inode blocks)

Assumption: size is the RAW DATA SIZE of a file (does not include inode data or block metadata)
	- I think that this is like "file size" and "size on disk" differences

If two threads try to r/w the same file, the second one to request r/w from the DFS will block until the first has finished.


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
  

/************************
 * Feedback on the lab
 ************************/

/*
 * Any comments/questions/suggestions/experiences that you would help us to
 * improve the lab.
 * */

/************************
 * References
 ************************/

/*
 * List of collaborators involved including any online references/citations.
 * */
