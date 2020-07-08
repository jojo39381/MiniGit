package gitlet;


/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Joseph Yeh
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    /** the commands: init, add, commit. takes in ARGS. */
    public static void main(String... args) {
        Command center = new Command();
        if (args.length < 1) {
            System.out.println("Please enter a command.");
            return;
        }
        process(args, center);
    }


    /** process algo. takes in ARGS and CENTER. */
    public static void process(String[] args, Command center) {
        switch (args[0]) {
        case "init":
            center.init();
            break;
        case "add":
            center.add(args[1]);
            break;
        case "commit":
            center.commit(args[1]);
            break;
        case "rm":
            center.remove(args[1]);
            break;
        case "find":
            center.find(args[1]);
            break;
        case "log":
            center.printLog();
            break;
        case "global-log":
            center.printGlobalLog();
            break;
        case "status":
            center.printStatus();
            break;
        case "checkout":
            checkoutProcess(args);
            break;
        case "branch":
            center.makeBranch(args[1]);
            break;
        case "rm-branch":
            center.deleteBranch(args[1]);
            break;
        case "reset":
            center.reset(args[1]);
            break;
        case "merge":
            center.merge(args[1], null, null, false);
            break;
        case "add-remote":
            center.addRemote(args[1], args[2]);
            break;
        case "rm-remote":
            center.removeRemote(args[1]);
            break;
        case "fetch":
            center.fetch(args[1], args[2]);
            break;
        case "pull":
            center.pull(args[1], args[2]);
            break;
        case "push":
            center.push(args[1], args[2]);
            break;
        default:
            System.out.println("No command with that name exists.");
        }
    }











    /** checkout process arguments. takes in COMMANDS. */
    public static void checkoutProcess(String[] commands) {
        Command center = new Command();
        if (commands.length < 2) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (commands.length == 3 && commands[1].equals("--")) {
            center.checkoutFile(commands[2]);
            return;
        }
        if (commands.length == 4 && commands[2].equals("--")) {
            center.checkoutCommitFile(commands[1], commands[3]);
            return;
        }
        if (commands.length == 2) {
            center.checkoutBranch(commands[1]);
            return;
        }
        System.out.println("Incorrect operands.");

    }




}
