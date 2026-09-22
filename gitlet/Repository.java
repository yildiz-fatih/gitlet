package gitlet;

import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.File;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.*;

/** Represents a gitlet repository. */
public class Repository {
    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
    public static final File BLOBS_DIR = join(OBJECTS_DIR, "blobs");
    public static final File COMMITS_DIR = join(OBJECTS_DIR, "commits");
    public static final File HEADS_DIR = join(GITLET_DIR, "refs", "heads");
    public static final File INDEX_FILE = join(GITLET_DIR, "index");
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");

    private static void writeIndex(Map<String, String> indexMap) {
        StringBuilder indexBuilder = new StringBuilder();
        for (var entry : indexMap.entrySet()) {
            indexBuilder.append(entry.getValue() + " " + entry.getKey() + "\n");
        }
        writeContents(INDEX_FILE, indexBuilder.toString());
    }

    private static Map<String, String> readIndex() {
        var indexMap = new HashMap<String, String>(); // mapping of filename -> hash
        if (INDEX_FILE.exists()) {
            var indexString = readContentsAsString(INDEX_FILE);
            var lines = indexString.lines().collect(Collectors.toList());
            for (String line : lines) {
                int firstSpace = line.indexOf(' ');
                String hash = line.substring(0, firstSpace);
                String name = line.substring(firstSpace + 1);
                indexMap.put(name, hash);
            }
        }
        return indexMap;
    }

    private static Commit getHeadCommit() {
        var head = readContentsAsString(HEAD_FILE);
        var branchFile = join(GITLET_DIR, head.split(" ")[1]);
        var commitHash = readContentsAsString(branchFile);
        var commitFile = join(COMMITS_DIR, commitHash);
        return readObject(commitFile, Commit.class);
    }

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
        var initialCommit = new Commit("initial commit", new Date(0), new ArrayList<>(), new HashMap<>());
        // serialize commit
        var commitBytes = serialize(initialCommit);
        // hash the serialized commit object
        var commitHash = sha1(commitBytes);
        // put it in ".gitlet/objects/commits/sha1-hash-of-this-commit"
        var commitObject = join(COMMITS_DIR, commitHash);
        COMMITS_DIR.mkdirs();
        writeContents(commitObject, commitBytes);
        // create a ".gitlet/refs/heads/master" and put the sha1-hash of the commit in it
        var masterRef = join(HEADS_DIR, "master");
        HEADS_DIR.mkdirs();
        writeContents(masterRef, commitHash);
        // create a "./gitlet/HEAD" and put "ref: refs/heads/master" in it
        writeContents(HEAD_FILE, "ref: refs/heads/master");
    }

    public static void add(String filename) {
        // file stuff
        var file = join(CWD, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        var fileBytes = readContents(file);
        var fileHash = sha1(fileBytes);

        var indexMap = readIndex(); // mapping of filename -> hash

        // If the current working version of the file is identical to the version in the current commit,
        //            do not stage it to be added, and remove it from the staging area if it is already there
        var commit = getHeadCommit();
        var committedHash = commit.getFilemap().get(filename);
        if (fileHash.equals(committedHash)) {
            indexMap.remove(filename);
            writeIndex(indexMap);
            return;
        }

        // ".gitlet/index" -> "hash filename\n"
        indexMap.put(filename, fileHash);
        writeIndex(indexMap);

        // ".gitlet/objects/blobs/sha1-hash-of-this-file"
        BLOBS_DIR.mkdirs();
        var fileObject = join(BLOBS_DIR, fileHash);
        writeContents(fileObject, fileBytes);
    }

}
