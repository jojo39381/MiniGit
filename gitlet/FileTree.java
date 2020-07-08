package gitlet;



import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.util.HashMap;

import static gitlet.Command.*;
/** filetree class serialize.
 * @author Joseph Yeh. */
public class FileTree implements Serializable {
    /** commitTree. */
    private static HashMap<String, String> commitTree;
    /** head. */
    private static String _head;
    /** filetree constructor takes in HEAD. */
    FileTree(String head) {
        _head = head;
        commitTree = new HashMap<String, String>();
    }
    /** add file to the tree. takes in COMMIT and COMMITOBJECT.*/
    public void addToTree(String commit, Commit commitObject) {
        File cwd = new File(System.getProperty("user.dir"));
        File repo = Utils.join(cwd, ".gitlet");
        File commitFolder = Utils.join(repo, "commits");

        File commitFile = Utils.join(commitFolder, commit);
        try {
            commitFile.createNewFile();
        } catch (IOException e) {
            System.out.println("lol");
        }
        Utils.writeObject(commitFile, commitObject);
        _head = commit;

        Utils.writeContents(getHead(), commit);
        commitTree.put(commit, commit);
    }


}
