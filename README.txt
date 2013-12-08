/**********************************************
 * Please DO NOT MODIFY the format of this file
 **********************************************/

/*************************
 * Team Info & Time spent
 *************************/

	Name1: Full Name 	// Edit this accordingly
	NetId1: fn	 	// Edit
	Time spent: 10 hours 	// Edit 

	Name2: Full Name 	// Edit this accordingly
	NetId2: fn	 	// Edit
	Time spent: 10 hours 	// Edit 

	Name3: Full Name 	// Edit this accordingly
	NetId3: fn	 	// Edit
	Time spent: 10 hours 	// Edit 

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

If we have reached the maximum amount of files in DFS, then calling DFS.createFile() will return a null DFileID. It is important
for the clients to take this into account!


DFS does not conform to the standard that test programs call .init(). Why? this does not seem like something the TestClient should worry
about. The test client specifies whether or not to format the DFS instead. If it formats, then there is no reason to call init() (
everything is already 0). If it decided to not format (i.e. tries to load a previous instance of DFS), then it does need to call init().
In the various SimpleDFS constructors, we handle the logic of this. That way, the test client only has to worry about to format or not 
to format.


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
