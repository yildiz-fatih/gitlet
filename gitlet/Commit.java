package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/** Represents a gitlet commit object. */
public class Commit implements Serializable {
    /** The message of this Commit. */
    private String message;
    /** The date of this commit */
    private Date timestamp;
    /** Sha1 hashes of the parent commits */
    private List<String> parents;
    /** Mapping of filename -> sha1 hash for all files */
    private Map<String, String> filemap;

    public Commit(String message, Date timestamp, List<String> parents, Map<String, String> filemap) {
        this.message = message;
        this.timestamp = timestamp;
        this.parents = parents;
        this.filemap = filemap;
    }
}
