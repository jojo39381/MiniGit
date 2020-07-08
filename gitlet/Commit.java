package gitlet;

import java.io.File;
import java.io.Serializable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static gitlet.Command.*;

/** commit class seraliable.
 * @author Joseph Yeh */
public class Commit implements Serializable {









    /** commits message. */
    private String _message;
    /** commits date . */
    private String _date;
    /** commits parent. */
    private String _parent;
    /** second parent of commits. */
    private String _secondParent;
    /** SHA_ID commits. */
    private String shaId;
    /** mapping of blobs of commits. */
    private HashMap<String, String> mapToBlobs;

    /** construct commit. takes in MESSAGE and PARENT. */
    Commit(String message, String parent) {
        _message = message;
        _parent = parent;
        _secondParent = null;
        mapToBlobs = new HashMap<String, String>();
        SimpleDateFormat format =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        if (parent.equals("")) {
            _date = format.format(new Date(0));
        } else {
            _date = format.format(new Date().getTime());
        }

    }

    /** get message. RETURNS string messages of commits.  */
    public String getMessage() {
        return _message;
    }
    /** get date. RETURNS date committed. */
    public String getDate() {
        return _date;
    }
    /** compute the sha value of a commit. */
    public void computeSHA() {
        byte[] yeet = Utils.serialize(this);
        String id = Utils.sha1((Object) yeet);
        shaId = id;

    }

    /** get sha id of commits. RETURNS sha id */
    public String getShaId() {
        return shaId;
    }

    /** get parent. RETURNS parent string of a commit.*/
    public String getParent() {
        return _parent;
    }
    /** get from staging area to commit file. */
    void bloblify() {
        List<String> fileList = Utils.plainFilenamesIn(STAGE);
        List<String> removeList = Utils.plainFilenamesIn(REMOVAL);
        if (fileList.size() == 0 && removeList.size() == 0) {
            System.out.println("No changes added to the commit.");
            return;
        }
        for (String file : fileList) {
            File staged = Utils.join(STAGE, file);
            byte[] content = Utils.readContents(staged);

            String stagedID = Utils.sha1((Object) content);
            File blob = Utils.join(ALLBLOBS, stagedID);
            Utils.writeContents(blob, (Object) content);

            mapToBlobs.put(staged.getName(), stagedID);

            staged.delete();
        }
        for (String file : removeList) {
            File remove = Utils.join(REMOVAL, file);
            mapToBlobs.remove(file);
            remove.delete();

        }



    }
    /** get mapping. RETURNS hashmap of blobs and commit ids. */
    public HashMap<String, String> getMap() {
        return mapToBlobs;
    }

    /** update commit and clone. takes in MESSAGE and PARENT. */
    public void updateCommit(String message, String parent) {
        SimpleDateFormat format =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        _date = _date = format.format(new Date().getTime());
        _message = message;
        _parent = parent;
    }
    /** add second parent to commit. takes in SECOND. */
    public void addSecondParent(String second) {
        _secondParent = second;
    }
    /** get second parent. RETURNS second parent of the commit. */
    public String getSecondParent() {
        return _secondParent;

    }





}
