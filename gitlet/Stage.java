package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Command.*;

/** stage class.
 *  @author Joseph Yeh. */
public class Stage implements Serializable {
    /** staged files. */
    private static HashMap<String, File> stagedFiles;
    /** staging folder. */
    private static File _staging;

    /** stage constructor. takes in an argument ACTIVE. */
    Stage(boolean active) {
        if (active) {
            _staging = STAGE;
        }
    }
    /** add file to stage. takes in READY and FILENAME. */
    void addToStage(File ready, String fileName) {
        File copy = Utils.join(STAGE, fileName);
        try {
            copy.createNewFile();
        } catch (IOException e) {
            System.out.println("lol");
        }

        byte[] info = Utils.readContents(ready);
        String commitVersion = Utils.readContentsAsString(getHead());
        File commit = Utils.join(COMMITS, commitVersion);
        Commit version = Utils.readObject(commit, Commit.class);
        File removal = Utils.join(REMOVAL, fileName);
        if (removal.exists()) {
            removal.delete();
        }
        if (!version.getParent().equals("")) {
            if (version.getMap().containsKey(fileName)
                    && version.getMap().get(fileName)
                    .equals(Utils.sha1((Object) Utils.readContents(ready)))) {
                if (Utils.join(STAGE, fileName).exists()) {
                    remove(Utils.join(STAGE, fileName));
                }
            } else {
                Utils.writeContents(copy, (Object) info);
            }
        } else {
            Utils.writeContents(copy, (Object) info);
        }
    }

    /** add file to be staged for removal. takes in FILENAME. */
    void addToRemove(String fileName) {
        File copy = Utils.join(REMOVAL, fileName);
        try {
            copy.createNewFile();
        } catch (IOException e) {
            System.out.println("lol");
        }







    }


    /** remove a file. takes in REMOVING. */
    void remove(File removing) {
        removing.delete();
    }


}








