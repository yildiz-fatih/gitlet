package gitlet;

import java.io.File;
import java.util.Date;

import static gitlet.Utils.*;

/** Represents a gitlet repository. */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    public static void init() {
        if (Repository.GITLET_DIR.isDirectory()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        // create a ".gitlet/" directory
        if (!GITLET_DIR.mkdir()) {
            System.out.println("UNEXPECTED");
            System.exit(1);
        }
        // create new Commit
        var initialCommit = new Commit("initial commit", new Date(0), null, null);
        // serialize commit
        var commitBytes = Utils.serialize(initialCommit);
        // hash the serialized commit object
        var commitHash = Utils.sha1(commitBytes);
        // put it in ".gitlet/objects/commits/sha1-hash-of-this-commit"
        var commitObject = Utils.join(GITLET_DIR, "objects", "commits", commitHash);
        Utils.join(GITLET_DIR, "objects", "commits").mkdirs();
        Utils.writeContents(commitObject, commitBytes);
        // create a ".gitlet/refs/heads/master" and put the sha1-hash of the commit in it
        var masterRef = Utils.join(GITLET_DIR, "refs", "heads", "master");
        Utils.join(GITLET_DIR, "refs", "heads").mkdirs();
        Utils.writeContents(masterRef, commitHash);
        // create a "./gitlet/HEAD" and put "ref: refs/heads/master" in it
        var head = Utils.join(GITLET_DIR, "HEAD");
        Utils.writeContents(head, "ref: refs/heads/master");
    }

}
