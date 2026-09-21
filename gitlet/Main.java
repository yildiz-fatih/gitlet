package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system. */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        /*
            * TODO: If a user inputs a command with the wrong number or format of operands,
            *       print the message "Incorrect operands." and exit.
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
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
            // TODO: FILL THE REST IN
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }
}