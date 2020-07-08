# Design Document

# Classes and Data Structure
## FileTree

This class creates new file trees that keeps tracks of all of the files created, committed, and different versions of files.

**Fields**

1. int length: The length of the tree.
2. File current: The current file that the user is on.
3. HashMap hashToNode: The hashmap that maps all the commit names to the nodes for easy accessing and searching.
4. Node<T> root: Represents the master node, the root of the tree.
5. String log: All the logs and history of commits and branches, etc.


## Commit

This class creates a new file that is ready to be committed. This commit object can then be stored in the FileTree data structure when it is staged for commit. 

**Fields**

1. string message: The commit message.
2. Date date: The commit data.
3. String log: The log of the commit file.


## Stage

This class contains all the files the are staged for commit using the git add command. It can then be used to create commit objects in order to be added to the tree.

**Fields**

1. ArrayList<File> staged: All the files that are staged for commit.
2. int size: amount of the files that are staged.


## Commands

This class handles all the commands and process them. It serves as the command center for all the command arguments.

**Fields**

1. String command: The command that is being processed.


# Algorithms
## Class FileTree
1. FileTree(): The class constructor. Construct a tree that represents all of the commits and branches. 
2. getCommit(int index): Retrieves the commit at the index specified.
3. getCommit(string message): Retrieves the commit with the specified message.
4. addCommit(Commit node): Add the commit to the existing FileTree.
5. addBranch(Commit node): Create a new branch and add the commit to the existing FileTree.


## Class Commit
1. Commit(): The class constructor. Make a commit with all the relevant information that is ready to be added to the file tree.
2. getInfo(string code): Gets the information about the commit file such as date and file type, etc.
3. getInfo(string name): Gets the information about the commit file.


## Class Stage
1. Stage(): The class constructor. Makes a list of all the files that are staged to be committed.
2. length(): Gets the amount of files that are staged for commit.
3. getInfo(): Gets a list of all the files that are ready to be committed.


## Class Command
1. processCommand: process the command that is inputted by the terminal or test file.
2. Command(String command): The class constructor. Switches commands into actionable statements and functions.
3. addCommand(): Add the file to be staged for commit.
4. commitCommand(): Commit the file that are already staged.
5. mergeCommand(): Merge the branches.
6. logCommand(): Shows all the history of the commits and its relevant information.
7. statusCommand(): Shows the current status of the commits and files.



# Persistence

To allow for persistence, all of our file classes will implement serialization. All our files would be placed in the .gitlet directory inside of our project folder. Inside the directory, there would be folders that signify and different branches. In these folders will be our files that are committed. They will be named by the SHA-1 code that are given to them fo easy accessing the searching. 

To save our project status and allow for persistence, we will save the files each time they are committed using the addCommit() function. Every time we branch or merge, we will also save the status of our program. To retrieve our state, we simply get the SHA-1 code for the file we are looking for, and we search with the hashmap to get the file back. 






