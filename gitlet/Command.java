package gitlet;

import java.io.File;

import java.io.IOException;
import java.util.HashMap;
import java.util.Collections;
import java.util.Map;

/** command class.
 * @author Joseph Yeh */
public class Command {
    /** working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** repo directory. */
    static final File REPO = Utils.join(CWD, ".gitlet");
    /** reference folder. */
    static final File REF = Utils.join(REPO, "ref");
    /** head folder. */
    static final File HEADFOLDER = Utils.join(REF, "head");
    /** head file. */
    static final File HEAD = Utils.join(HEADFOLDER, "master");
    /** current branch. */
    static final File CURRENT_BRANCH = Utils.join(REF, "currentBranch");
    /** staging area. */
    static final File STAGE = Utils.join(REPO, "stage");
    /** commits folder. */
    static final File COMMITS = Utils.join(REPO, "commits");
    /** blobs folder. */
    static final File ALLBLOBS = Utils.join(REPO, "blobs");
    /** removal area. */
    static final File REMOVAL = Utils.join(REPO, "removal");

    /** remote. */
    static final File REMOTE = Utils.join(REPO, "remote");


    /** initialize repo. */
    public void init() {
        if (!REPO.exists()) {
            Commit first = new Commit("initial commit", "");




            REPO.mkdir();
            REF.mkdir();
            HEADFOLDER.mkdir();
            try {
                HEAD.createNewFile();
            } catch (IOException e) {
                System.out.println("lol");
            }

            STAGE.mkdir();
            COMMITS.mkdir();
            ALLBLOBS.mkdir();
            REMOVAL.mkdir();
            REMOTE.mkdir();
            Utils.writeContents(CURRENT_BRANCH, "master");

            area = new Stage(true);
            first.computeSHA();
            initCommit(first, "master");
            Utils.writeContents(HEAD, first.getShaId());
        } else {
            System.out.println("A Gitlet version-control system already "
                    + "exists in the current directory.");
        }

    }



    /** get head of current branch. RETURNS file. */
    public static File getHead() {

        return Utils.join(HEADFOLDER,
                Utils.readContentsAsString(CURRENT_BRANCH));
    }

    /** staging area. */
    private static Stage area;

    /** stage file. takes in FILENAME.*/
    public static void add(String fileName) {
        area = new Stage(true);
        File adding = new File(fileName);
        if (adding.exists()) {
            area.addToStage(adding, fileName);
        } else {
            System.out.println("file does not exist");
        }


    }
    /** initialized commit. takes in FILE and BRANCH.*/
    public void initCommit(Commit file, String branch) {
        FileTree tree = new FileTree(branch);
        tree.addToTree(file.getShaId(), file);
        Utils.writeContents(CURRENT_BRANCH, branch);

    }
    /** commit file. takes in MESSAGE. */
    public void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
        }

        String head = Utils.readContentsAsString(getHead());
        Commit prev = Utils.readObject(Utils.join(COMMITS, head), Commit.class);

        prev.updateCommit(message, head);
        prev.bloblify();


        prev.computeSHA();
        FileTree tree = new FileTree("master");
        tree.addToTree(prev.getShaId(), prev);



    }
    /** remove file. takes in FILENAME. */
    public static void remove(String fileName) {
        File stagedFile = Utils.join(STAGE, fileName);
        File file = getHead();
        String head = Utils.readContentsAsString(file);
        Commit current = Utils
                .readObject(Utils.join(COMMITS, head), Commit.class);
        boolean tracked = current.getMap().containsKey(fileName);
        boolean staged = stagedFile.exists();
        if (!tracked && !staged) {
            System.out.println("No reason to remove the file.");
        }

        if (tracked) {
            area = new Stage(true);
            area.addToRemove(fileName);
            File working = Utils.join(CWD, fileName);
            if (working.exists()) {
                working.delete();
            }
        }
        if (staged) {
            stagedFile.delete();
        }





    }





    /** prints log of current branch. */
    public void printLog() {
        String headID = Utils.readContentsAsString(getHead());
        Commit current = getCommit(headID);

        Commit i = current;
        while (true) {

            String result = "===" + "\n"
                    + "commit " + i.getShaId() + "\n"
                    + "Date: " + i.getDate() + "\n"
                    + i.getMessage() + "\n";
            System.out.println(result);
            if (i.getParent().equals("")) {
                break;
            }
            i = getCommit(i.getParent());
        }


    }
    /** gets commit. takes in ID. RETURNS commit. */
    public static Commit getCommit(String id) {
        File currentID = Utils.join(COMMITS, id);
        Commit current = Utils.readObject(currentID, Commit.class);
        return current;

    }

    /** gets commit. takes in ID and REMOTE. RETURNS commit. */
    public static Commit getCommit(String id, String remote) {
        File commitFolder = Utils.join(remote, "commits");
        File currentID = Utils.join(commitFolder, id);
        Commit current = Utils.readObject(currentID, Commit.class);
        return current;

    }





    /** find commit. takes in MESSAGE */
    public void find(String message) {
        boolean found = false;
        for (String i : Utils.plainFilenamesIn(COMMITS)) {
            Commit j = getCommit(i);
            if (j.getMessage().equals(message)) {
                System.out.println(j.getShaId());
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }

    /** print global log. */
    public void printGlobalLog() {
        for (String i : Utils.plainFilenamesIn(COMMITS)) {
            Commit j = getCommit(i);
            String result = "===" + "\n"
                    + "commit " + j.getShaId()
                    + "\n" + "Date: " + j.getDate() + "\n"
                    + j.getMessage() + "\n";
            System.out.println(result);

        }



    }




    /** process status. RETURNS processed string */
    public static String[] process() {
        String added = "";
        String mod = "";
        String head = Utils.readContentsAsString(getHead());
        Commit cur = getCommit(head);
        HashMap<String, String> map = cur.getMap();
        for (String i : Utils.plainFilenamesIn(STAGE)) {
            added += i + "\n";
            File staged = Utils.join(STAGE, i);
            String content = Utils.readContentsAsString(staged);
            File work = Utils.join(CWD, i);
            if (work.exists()) {
                String contentW = Utils.readContentsAsString(work);
                if (!content.equals(contentW)) {
                    mod += i + " " + "(modified)\n";
                    map.remove(i);
                }
            } else {
                mod += i + " " + "(deleted)\n";
                map.remove(i);
            }
        }
        for (String i : map.keySet()) {
            File path = Utils.join(CWD, i);
            File rem = Utils.join(REMOVAL, i);
            if (!path.exists() && !rem.exists()) {
                mod += i + " " + "(deleted)\n";
            } else if (path.exists()) {
                byte[] wo = Utils.readContents(path);
                String id = Utils.sha1((Object) wo);
                File sta = Utils.join(STAGE, i);
                if (!id.equals(map.get(i)) && !sta.exists()) {
                    mod += i + " " + "(modified)\n";
                }
            }
        }
        String[] result = new String[2];
        result[0] = added;
        result[1] = mod;
        return result;
    }
    /** print status. */
    public void printStatus() {
        if (!REPO.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        String head = Utils.readContentsAsString(getHead());
        Commit cur = getCommit(head);
        HashMap<String, String> map = cur.getMap();
        System.out.println("=== Branches ===");
        String current = Utils.readContentsAsString(CURRENT_BRANCH);
        for (String i : Utils.plainFilenamesIn(HEADFOLDER)) {
            if (i.equals(current)) {
                System.out.println("*" + i);
            } else {
                System.out.println(i);
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        String[] processed = process();
        System.out.println(processed[0]);
        System.out.println("=== Removed Files ===");
        for (String i : Utils.plainFilenamesIn(REMOVAL)) {
            System.out.println(i);
        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println(processed[1]);
        System.out.println("=== Untracked Files ===");
        for (String i : Utils.plainFilenamesIn(CWD)) {
            File stagedFile = Utils.join(STAGE, i);
            boolean tracked = map.containsKey(i);
            boolean staged = stagedFile.exists();
            if (!tracked && !staged) {
                System.out.println(i);
            }
        }
        System.out.println("");





    }
    /** checkout file. takes in FILENAME */
    public void checkoutFile(String fileName) {
        File file = getHead();
        String head = Utils.readContentsAsString(file);
        Commit current = Utils.readObject(Utils.join(COMMITS, head),
                Commit.class);
        HashMap<String, String> map = current.getMap();

        if (map.containsKey(fileName)) {
            byte[] content =
                    Utils.readContents(Utils.join(ALLBLOBS, map.get(fileName)));
            File working = Utils.join(CWD, fileName);
            Utils.writeContents(working, (Object) content);

        } else {
            System.out.println("File does not exist in that commit");
        }
    }
    /** checkout commit file. takes in COMMIT and FILENAME. */
    public static void checkoutCommitFile(String commit, String fileName) {
        File commitPath = null;
        for (String id : Utils.plainFilenamesIn(COMMITS)) {
            String sub = id.substring(0, commit.length());
            if (commit.equals(sub)) {
                commitPath = Utils.join(COMMITS, id);
            }
        }
        if (commitPath != null) {
            Commit thatCommit = Utils.readObject(commitPath, Commit.class);
            HashMap<String, String> map = thatCommit.getMap();
            if (map.containsKey(fileName)) {
                byte[] content = Utils
                        .readContents(Utils.join(ALLBLOBS, map.get(fileName)));
                File working = Utils.join(CWD, fileName);
                Utils.writeContents(working, (Object) content);

            } else {
                System.out.println("File does not exist in that commit.");
            }
        } else {
            System.out.println("No commit with that id exists.");
        }

    }

    /** checkout branch. takes in BRANCH. */
    public static void checkoutBranch(String branch) {
        if (branch.contains(File.separator)) {
            branch = branch.replaceAll(File.separator, "of");
        }

        if (Utils.readContentsAsString(CURRENT_BRANCH).equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File branchFile = Utils.join(HEADFOLDER, branch);
        if (branchFile.exists()) {
            String branchString = Utils.readContentsAsString(branchFile);
            Commit m = getCommit(branchString);
            String h = Utils.readContentsAsString(getHead());
            Commit current = getCommit(h);
            HashMap<String, String> map = m.getMap();
            HashMap<String, String> copy = current.getMap();

            for (String fileName : map.keySet()) {
                if (!copy.containsKey(fileName)
                        && Utils.join(CWD, fileName).exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                } else {
                    copy.remove(fileName);
                }
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String fileName = entry.getKey();
                String id = entry.getValue();
                byte[] content = Utils.readContents(Utils.join(ALLBLOBS, id));
                File working = Utils.join(CWD, fileName);
                Utils.writeContents(working, (Object) content);

            }
            for (String keys : copy.keySet()) {
                Utils.join(CWD, keys).delete();
            }

            for (String stageFile : STAGE.list()) {
                File staged = Utils.join(STAGE, stageFile);
                staged.delete();
            }
            Utils.writeContents(CURRENT_BRANCH, branch);

        } else {
            System.out.println("No such branch exists.");
        }
    }




    /** Make Branch. takes in BRANCHNAME */

    public void makeBranch(String branchName) {
        File newBranch = Utils.join(HEADFOLDER, branchName);
        if (!newBranch.exists()) {
            String currentHead = Utils.readContentsAsString(getHead());
            Utils.writeContents(newBranch, currentHead);
        } else {
            System.out.println("A branch with that name already exists.");
        }
    }

    /** delete branch. takes in BRANCHNAME. */
    public void deleteBranch(String branchName) {
        File branch = Utils.join(HEADFOLDER, branchName);
        String current = Utils.readContentsAsString(CURRENT_BRANCH);
        if (!current.equals(branchName)) {
            if (branch.exists()) {
                branch.delete();
            } else {
                System.out.println("A branch with that name does not exist.");

            }
        } else {
            System.out.println("Cannot remove the current branch.");
        }

    }
    /** reset file. takes in COMMITID. */
    public void reset(String commitID) {
        if (Utils.join(COMMITS, commitID).exists()) {
            Commit m = getCommit(commitID);
            Commit current = getCommit(Utils.readContentsAsString(getHead()));
            HashMap<String, String> map = m.getMap();
            HashMap<String, String> copy = current.getMap();
            for (String fileName : map.keySet()) {
                if (!copy.containsKey(fileName)
                        && Utils.join(CWD, fileName).exists()) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    return;
                } else {
                    copy.remove(fileName);
                }
            }
            for (Map.Entry<String, String> entry : map.entrySet()) {

                String fileName = entry.getKey();
                String id = entry.getValue();
                byte[] content = Utils.readContents(Utils.join(ALLBLOBS, id));
                File working = Utils.join(CWD, fileName);
                Utils.writeContents(working, (Object) content);

            }
            for (String keys : copy.keySet()) {
                Utils.join(CWD, keys).delete();
            }
            Utils.writeContents(getHead(), commitID);

            for (String stageFile : STAGE.list()) {
                File staged = Utils.join(STAGE, stageFile);
                staged.delete();
            }
        } else {
            System.out.println("No commit with that id exists.");
        }


    }

    /** checks for error cases. takes in BRANCH. RETURNS conflict?. */
    public static boolean errorCheck(String branch) {
        if (STAGE.list().length > 0 | REMOVAL.list().length > 0) {
            System.out.println("You have uncommitted changes.");
            return true;
        }
        if (!Utils.join(HEADFOLDER, branch).exists()) {
            System.out.println("A branch with that name does not exist.");
            return true;
        }
        if (Utils.readContentsAsString(CURRENT_BRANCH).equals(branch)) {
            System.out.println("Cannot merge a branch with itself.");
            return true;
        }
        File branchFile = Utils.join(HEADFOLDER, branch);
        Commit given = getCommit(Utils.readContentsAsString(branchFile));
        Commit current = getCommit(Utils.readContentsAsString(getHead()));
        HashMap<String, String> givenMap = given.getMap();
        HashMap<String, String> copy = current.getMap();
        HashMap<String, String> currentMap = current.getMap();
        for (String fileName : givenMap.keySet()) {
            if (!copy.containsKey(fileName)
                    && Utils.join(CWD, fileName).exists()) {
                System.out.println("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                return true;
            }
        }
        return false;


    }

    /** whether the function encountered a conflict or not. */
    private static boolean conflict = false;
    /** process given. takes in GIVEN, CURRENT, SPLITPOINT.
     * RETURNS hashmap current. */
    public static HashMap<String, String>
        processGiven(Commit given, Commit current, Commit splitPoint) {
        HashMap<String, String> givenMap = given.getMap();
        HashMap<String, String> splitMap = splitPoint.getMap();
        HashMap<String, String> copy = current.getMap();
        for (Map.Entry<String, String> entry : givenMap.entrySet()) {
            String fileName = entry.getKey();
            String id = entry.getValue();
            if ((splitMap.containsKey(fileName)
                    && !splitMap.get(fileName).equals(id))
                    && (copy.containsKey(fileName)
                    && copy.get(fileName).equals(splitMap.get(fileName)))) {
                checkoutCommitFile(given.getShaId(), fileName);
                add(fileName);
                copy.remove(fileName);
                continue;
            }
            if (!splitMap.containsKey(fileName)
                    && !copy.containsKey(fileName)) {
                checkoutCommitFile(given.getShaId(), fileName);
                add(fileName);
                continue;
            }
            if (splitMap.containsKey(fileName)) {
                if (!splitMap.get(fileName).equals(id)
                        && copy.containsKey(fileName)
                        && !copy.get(fileName).equals(id)
                        && !copy.get(fileName)
                        .equals(splitMap.get(fileName))) {
                    File working = Utils.join(CWD, fileName);
                    Utils.writeContents(working,
                            newFile(copy.get(fileName), id));
                    conflict = true;
                    add(fileName);
                    copy.remove(fileName);
                    continue;
                }
                if (!splitMap.get(fileName).equals(id)
                        && !copy.containsKey(fileName)) {
                    File working = Utils.join(CWD, fileName);
                    Utils.writeContents(working, newFile("", id));
                    add(fileName);
                    conflict = true;
                    continue;
                }
            }
            if (!splitMap.containsKey(fileName)) {
                if (copy.containsKey(fileName)
                        && !copy.get(fileName).equals(id)) {
                    File working = Utils.join(CWD, fileName);
                    Utils.writeContents(working,
                            newFile(copy.get(fileName), id));
                    conflict = true;
                    add(fileName);
                    copy.remove(fileName);
                }
            }
        }
        return copy;
    }
    /** process current. takes in COPY, GIVEN, SPLITPOINT. */
    public static void processCurrent(
            HashMap<String, String> copy, Commit given, Commit splitPoint) {
        HashMap<String, String> givenMap = given.getMap();
        HashMap<String, String> splitMap = splitPoint.getMap();
        for (Map.Entry<String, String> entry : copy.entrySet()) {
            String fileName = entry.getKey();
            String id = entry.getValue();
            if ((splitMap.containsKey(fileName)
                    && splitMap.containsValue(id))
                    && !givenMap.containsKey(fileName)) {
                remove(fileName);
                continue;
            }
            if (splitMap.containsKey(fileName)) {
                if (!splitMap.get(fileName).equals(id)
                        && givenMap.containsKey(fileName)
                        && !givenMap.get(fileName).equals(id)
                        && !givenMap.get(fileName)
                        .equals(splitMap.get(fileName))) {
                    File working = Utils.join(CWD, fileName);
                    Utils.writeContents(working,
                            newFile(copy.get(fileName), id));
                    add(fileName);
                    conflict = true;
                    continue;
                }
                if (!splitMap.get(fileName).equals(id)
                        && !givenMap.containsKey(fileName)) {
                    File working = Utils.join(CWD, fileName);
                    Utils.writeContents(working,
                            newFile(id, ""));
                    add(fileName);
                    conflict = true;
                    continue;
                }
            }
            if (!splitMap.containsKey(fileName)) {
                if (givenMap.containsKey(fileName)
                        && !givenMap.get(fileName).equals(id)) {
                    File working = Utils.join(CWD, fileName);
                    Utils.writeContents(working,
                            newFile(copy.get(fileName), id));
                    add(fileName);
                    conflict = true;
                }
            }
        }
    }





    /** merge file. takes in BRANCH, NAME, RBRANCH, and REMOTE. */
    public static void merge(String branch, String name, String rbranch,
                             boolean remote) {
        if (errorCheck(branch)) {
            return;
        }
        allCommits = new HashMap<String, Integer>();
        allMatching = new HashMap<Integer, String>();

        File branchFile = Utils.join(HEADFOLDER, branch);
        Commit given = getCommit(Utils.readContentsAsString(branchFile));
        Commit current = getCommit(Utils.readContentsAsString(getHead()));

        Commit splitPoint = null;
        getAll(given.getShaId(), 0, null);
        getMatching(current.getShaId(), 0, null);
        int min = Collections.min(allMatching.keySet());
        splitPoint = getCommit(allMatching.get(min));
        conflict = false;
        if (splitPoint != null) {
            if (splitPoint.getShaId().equals(given.getShaId())) {
                System.out.println("Given branch is an "
                        + "ancestor of the current branch.");
                return;
            }
            if (splitPoint.getShaId().equals(current.getShaId())) {
                checkoutBranch(branch);
                System.out.println("Current branch fast-forwarded.");
                return;
            }

            HashMap<String, String> processed =
                    processGiven(given, current, splitPoint);
            processCurrent(processed, given, splitPoint);
            if (!remote) {
                mergeCommit("Merged " + branch + " into "
                        + Utils.readContentsAsString(CURRENT_BRANCH)
                        + ".", branch);
            } else {
                mergeCommit("Merged " + name + "/" + rbranch + " into "
                        + Utils.readContentsAsString(CURRENT_BRANCH)
                        + ".", branch);
            }
            if (conflict) {
                System.out.println("Encountered a merge conflict.");
            }
        }

    }

    /** creates conflict file that combines together.
     * takes in CURRENT and GIVEN.
     * RETURNS combined file of the conflicted things.*/
    public static String newFile(String current, String given) {
        if (!current.equals("")) {
            current = Utils.readContentsAsString(Utils.join(ALLBLOBS, current));
        }
        if (!given.equals("")) {
            given = Utils.readContentsAsString(Utils.join(ALLBLOBS, given));
        }
        String file = "<<<<<<< HEAD\n" + current + "=======\n"
                + given + ">>>>>>>\n";
        return file;
    }

    /** merge commit. takes in MESSAGE and BRANCHNAME. */
    public static void mergeCommit(String message, String branchName) {

        String head = Utils.readContentsAsString(getHead());
        Commit prev = Utils.readObject(Utils.join(COMMITS, head), Commit.class);
        prev.updateCommit(message, head);
        prev.addSecondParent(Utils
                .readContentsAsString(Utils
                        .join(HEADFOLDER, branchName)));
        prev.bloblify();


        prev.computeSHA();
        FileTree tree = new FileTree("master");
        tree.addToTree(prev.getShaId(), prev);
    }



    /** all Commits Hashmap. */
    private static HashMap<String, Integer> allCommits =
            new HashMap<String, Integer>();

    /** get all commits from given. takes in GIVEN and DISTANCE and REMOTE. */
    public static void getAll(String given, Integer distance, String remote) {
        Commit m;
        if (remote != null) {
            m = getCommit(given, remote);
        } else {
            m = getCommit(given);
        }

        while (true) {
            allCommits.put(m.getShaId(), distance);
            if (m.getSecondParent() != null) {
                getAll(m.getSecondParent(), distance + 1, remote);
            }
            if (m.getParent().equals("")) {
                break;
            }
            if (remote != null) {
                m = getCommit(m.getParent(), remote);
            } else {
                m = getCommit(m.getParent());
            }
            distance += 1;

        }

    }
    /** allMatching hashmap. */
    private static HashMap<Integer, String> allMatching =
            new HashMap<Integer, String>();

    /** get matching commits. takes in CURRENT and DISTANCE and REMOTE. */
    public static void getMatching(String current, Integer distance,
                                   String remote) {
        Commit m;
        if (remote != null) {
            m = getCommit(current, remote);
        } else {
            m = getCommit(current);
        }

        while (true) {
            if (allCommits.containsKey(m.getShaId())) {
                allMatching.put(distance, m.getShaId());
            }

            if (m.getSecondParent() != null) {
                getMatching(m.getSecondParent(), distance + 1, remote);
            }
            if (m.getParent().equals("")) {
                break;
            }

            if (remote != null) {
                m = getCommit(m.getParent(), remote);
            } else {
                m = getCommit(m.getParent());
            }

            distance += 1;

        }


    }

    /** add remote repo. takes in NAME and DIRECTORY. */
    public static void addRemote(String name, String directory) {
        File remote = Utils.join(REMOTE, name);
        if (remote.exists()) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        Utils.writeContents(remote, directory);


    }
    /** remove remote repo. takes in NAME. */
    public static void removeRemote(String name) {
        File remote = Utils.join(REMOTE, name);
        if (remote.exists()) {
            remote.delete();
        } else {
            System.out.println("A remote with that name does not exist.");
        }

    }


    /** fetch files. takes in NAME and BRANCH. */
    public static void fetch(String name, String branch) {

        allCommits = new HashMap<String, Integer>();
        allMatching = new HashMap<Integer, String>();
        File remote = Utils.join(REMOTE, name);
        String path = Utils.readContentsAsString(remote);

        File repo = new File(path);
        if (!repo.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        File reference = Utils.join(path, "ref");
        File head = Utils.join(reference, "head");
        File blobs = Utils.join(path, "blobs");
        File branchFile = Utils.join(head, branch);
        if (!branchFile.exists()) {
            System.out.println("That remote does not have that branch.");
            return;
        }
        String branchHead = Utils.readContentsAsString(branchFile);
        Commit given = getCommit(branchHead, path);
        Commit current = getCommit(Utils.readContentsAsString(getHead()));

        Commit splitPoint = null;
        getAll(given.getShaId(), 0, path);
        getMatching(current.getShaId(), 0, null);
        int min = Collections.min(allMatching.keySet());
        splitPoint = getCommit(allMatching.get(min));
        while (!given.getShaId().equals(splitPoint.getShaId())) {
            File commit = Utils.join(COMMITS, given.getShaId());
            Utils.writeObject(commit, given);
            HashMap<String, String> map = given.getMap();
            for (String val : map.values()) {
                File blobFile = Utils.join(blobs, val);
                File currentBlob = Utils.join(ALLBLOBS, val);
                byte[] con = Utils.readContents(blobFile);
                Utils.writeContents(currentBlob, (Object) con);
            }

            given = getCommit(given.getParent(), path);
        }

        File remoteb = Utils.join(HEADFOLDER, name + "of" + branch);
        Utils.writeContents(remoteb, branchHead);
    }


    /** pull files. takes in NAME and BRANCH. */
    public static void pull(String name, String branch) {
        fetch(name, branch);
        merge(name + "of" + branch, name, branch, true);
    }


    /** push files. takes in NAME and BRANCH. */
    public static void push(String name, String branch) {
        File remote = Utils.join(REMOTE, name);
        String path = Utils.readContentsAsString(remote);
        File repo = new File(path);
        if (!repo.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        File reference = Utils.join(path, "ref");
        File head = Utils.join(reference, "head");
        File blobs = Utils.join(path, "blobs");
        File commits = Utils.join(path, "commits");
        File branchFile = Utils.join(head, branch);
        String branchHead = Utils.readContentsAsString(branchFile);
        Commit given = getCommit(branchHead, path);
        String givenId = given.getShaId();
        Commit current = getCommit(Utils.readContentsAsString(getHead()));
        String currentId = current.getShaId();
        if (!branchFile.exists()) {
            Utils.writeContents(branchFile, currentId);
        }
        while (true) {
            if (givenId.equals(current.getShaId())) {
                break;
            }
            if (current.getParent().equals("")) {
                System.out.println("Please pull down "
                        + "remote changes before pushing.");
                return;
            }
            current = getCommit(current.getParent());
        }
        Commit current1 = getCommit(Utils.readContentsAsString(getHead()));
        while (true) {
            File commit = Utils.join(commits, current1.getShaId());
            Utils.writeObject(commit, current1);
            HashMap<String, String> map = current1.getMap();
            for (String val : map.values()) {
                File blobFile = Utils.join(ALLBLOBS, val);
                File remoteBlob = Utils.join(blobs, val);
                byte[] con = Utils.readContents(blobFile);
                Utils.writeContents(remoteBlob, (Object) con);
            }
            if (givenId.equals(current1.getShaId())) {
                break;
            }
            current1 = getCommit(current1.getParent());
        }
        File remoteb = Utils.join(head, name + "of" + branch);
        Utils.writeContents(remoteb, currentId);
        File curr = Utils.join(reference, "currentBranch");
        Utils.writeContents(curr, name + "of" + branch);
    }













}


















