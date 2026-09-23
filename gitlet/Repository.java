package gitlet;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;

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
        var indexMap = new TreeMap<String, String>(); // mapping of filename -> hash
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
        // check for an existing gitlet directory
        if (Repository.GITLET_DIR.isDirectory()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }

        /* create the ".gitlet" directory */
        GITLET_DIR.mkdir();

        /* create the initial commit */
        var initialCommit = new Commit("initial commit", new Date(0), new ArrayList<>(), new TreeMap<>());
        // serialize
        var commitBytes = serialize(initialCommit);
        // hash the bytes
        var commitHash = sha1(commitBytes);
        // make directory ".gitlet/objects/commits"
        COMMITS_DIR.mkdirs();
        // write the commit to a file at ".gitlet/objects/commits/commit-hash"
        var commitObject = join(COMMITS_DIR, commitHash);
        writeContents(commitObject, commitBytes);

        /* create the master branch at ".gitlet/refs/heads/master" */
        // make directory ".gitlet/refs/heads"
        HEADS_DIR.mkdirs();
        // write the commit hash to the master branch file
        var masterRef = join(HEADS_DIR, "master");
        writeContents(masterRef, commitHash);

        /* create the "./gitlet/HEAD" file, write the master branch to it as "ref: refs/heads/master" */
        writeContents(HEAD_FILE, "ref: refs/heads/master");
    }

    public static void add(String filename) {
        // ensure file exists
        var file = join(CWD, filename);
        if (!file.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        // read the file's bytes
        var fileBytes = readContents(file);
        // hash the bytes
        var fileHash = sha1(fileBytes);
        // look up the current commit's recorded hash for this filename
        var currentCommit = getHeadCommit();
        var committedHash = currentCommit.getFilemap().get(filename);
        // load the staging area (".gitlet/index") into a map
        var indexMap = readIndex(); // mapping of filename -> hash

        // check if the added file has changed since the current commit
        if (fileHash.equals(committedHash)) {
            // working version is identical to what's already committed, it's now a stale entry in the staging area
            // remove it from the staging area
            indexMap.remove(filename);
            writeIndex(indexMap);
            return;
        }

        // make directory ".gitlet/objects/blobs"
        BLOBS_DIR.mkdirs();
        // write the bytes to a file at ".gitlet/objects/blobs/<file's-hash>"
        var fileObject = join(BLOBS_DIR, fileHash);
        writeContents(fileObject, fileBytes);

        // add it to the staging area (".gitlet/index")
        indexMap.put(filename, fileHash);
        writeIndex(indexMap);
    }

    public static void commit(String message) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        var indexMap = readIndex(); // mapping of filename -> hash
        if (indexMap.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        var headString = readContentsAsString(HEAD_FILE);
        var branchFile = join(GITLET_DIR, headString.split(" ")[1]);
        var parentCommitHash = readContentsAsString(branchFile);
        var commitFile = join(COMMITS_DIR, parentCommitHash);
        var parentCommit = readObject(commitFile, Commit.class);
        var parentFilemap = parentCommit.getFilemap();

        // build the new filemap for the commit
        var currentFilemap = new TreeMap<String, String>();
        // copy (bring over) the parentFilemap
        currentFilemap.putAll(parentFilemap);
        // bring over the files in the index: add the new ones, update the existing ones (from the parentfilemap)
        currentFilemap.putAll(indexMap);
        // create the new commit
        var currentCommit = new Commit(message, new Date(), new ArrayList<>(List.of(parentCommitHash)), currentFilemap);
        // serialize -> hash -> write to ".gitlet/objects/commits/commit-hash"
        var currentCommitBytes = serialize(currentCommit);
        var currentCommitHash = sha1(currentCommitBytes);
        var currentCommitObject = join(COMMITS_DIR, currentCommitHash);
        writeContents(currentCommitObject, currentCommitBytes);
        // update the branch file to point to the new hash
        writeContents(branchFile, currentCommitHash);
        // clear the index
        indexMap.clear();
        writeIndex(indexMap);
    }

}
