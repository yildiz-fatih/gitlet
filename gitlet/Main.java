package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system. */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        /*
            * TODO: // this is a Gitlet specs requirement to keep in mind for all command implementations
            *   If a user inputs a command with the wrong number or format of operands,
            *   print the message "Incorrect operands." and exit with 0.
         */
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        var isGitletDir = Repository.GITLET_DIR.isDirectory();
        if (!firstArg.equals("init") && !isGitletDir) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        switch(firstArg) {
            case "init":
                if (args.length != 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            case "commit":
                if (args.length != 2) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}