package nz.murch.sftp.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Vector;

public class ServerSession extends Thread {

    // enum for server state machine
    public enum ServerStates {
        WELCOME,
        USER,
        ACCOUNT,
        PASSWORD,
        COMMAND,
        LOGOUT,
    }

    // enum for data stream type
    public enum Types {
        ASCII,
        BINARY,
        CONTINUOUS
    }

    // enum for STOR command modes
    public enum StoreModes {
        NEW,
        OLD,
        APP
    }

    // vector containing all of the SFTP commands data
    private final Vector<SFTPCommand> commands = new Vector<>(Arrays.asList(
            new User(this),
            new Account(this),
            new Password(this),
            new Type(this),
            new List(this),
            new ChangeDirectory(this),
            new Kill(this),
            new Name(this),
            new ToBe(this),
            new Done(this),
            new Retrieve(this),
            new Send(this),
            new Stop(this),
            new Store(this),
            new Size(this)
    ));

    private final ServerConnection connection;
    private ServerStates state;

    private final String hostname;
    private String[] accountData;
    private Path cwd;

    private SFTPCommand presentCommand;
    private SFTPCommand previousCommand;
    private String[] arguments;

    private Types streamType;

    private boolean keepRunning;

    public ServerSession(Socket socket, String hostname) throws IOException {
        // create new server connection to interact with client more easily
        this.connection = new ServerConnection(socket);
        // set default state
        this.state = ServerStates.WELCOME;
        // set default streaming mode
        this.streamType = Types.BINARY;
        // set current working directory
        this.cwd = Paths.get("");

        // set hostname of this server
        this.hostname = hostname;
        this.keepRunning = true;
    }

    @Override
    public void run() {
        // main loop
        while (keepRunning) {
            try {
                // state machine
                switch (this.state) {
                    case WELCOME -> this.welcome();
                    case USER -> this.user();
                    case ACCOUNT -> this.account();
                    case PASSWORD -> this.password();
                    case COMMAND -> this.command();
                    case LOGOUT -> this.logout();
                }
            } catch (IOException e) {
                this.state = ServerStates.LOGOUT;
                e.printStackTrace();
            }
        }
    }

    private void welcome() throws IOException {
        // welcome state method
        // sent welcome message
        this.connection.writeToClient("+" + hostname + " Welcome :)");
        // change FSM state to receive username
        this.state = ServerStates.USER;
    }

    private void user() throws IOException {
        // user state method
        // load data from client
        loadInputData();

        // check for user command with arguments
        if (presentCommand.toString().equals("USER") && arguments[0] != null) {
            // get user info and interact with client
            this.accountData = this.retrieveUser(arguments[0]);
            SFTPResponses response = this.writeToClient();
        }

    }

    private void account() throws IOException {
        // account state method
        // load data from client
        this.loadInputData();

        // check for account command and arguments
        if (this.presentCommand.toString().equals("ACCT") && this.arguments[0] != null) {
            // handle client and check user data
            SFTPResponses response = this.writeToClient();
        }
    }

    private void password() throws IOException {
        // password state method
        this.loadInputData();

        // check for password command and arguments
        if (this.presentCommand.toString().equals("PASS") && this.arguments[0] != null) {
            SFTPResponses response = this.writeToClient();
        }
    }

    private void command() throws IOException {
        // command state method

        this.loadInputData();

        // determine input command
        switch (this.presentCommand.toString()) {
            case "CDIR":
                // check arguments present
                if (this.arguments.length >= 1) {
                    // check if file is directory
                    if (Files.isDirectory(this.cwd.resolve(this.arguments[0]))) {
                        // format directory information for handling by ChangeDirectory object
                        this.cwd = this.cwd.resolve(this.arguments[0]).toAbsolutePath();
                        this.arguments[0] = this.cwd.toString();
                        System.setProperty("user.dir", this.cwd.toString());
                    } else {
                        // path not directory
                        this.presentCommand.setError("Specified path is not a directory");
                    }
                } else {
                    this.presentCommand.setError("No path specified");
                }
                break;
            case "NAME":
                // check for arguments
                if (this.arguments.length >= 1) {
                    // format path information
                    Path file = this.cwd.resolve(this.arguments[0]).toAbsolutePath();
                    this.arguments[0] = file.toString();
                    // handle tobe information
                    this.tobe(file);
                    return;
                } else {
                    this.presentCommand.setError("Can't find null");
                }
                break;
            case "RETR":
                // retrieve file command
                if (this.arguments.length >= 1) {
                    Path file = this.cwd.resolve(this.arguments[0]).toAbsolutePath();
                    this.arguments[0] = file.toString();
                    // handle send command
                    this.send(file);
                    return;
                } else {
                    this.presentCommand.setError("Can't find null");
                }
                break;
            case "DONE":
                // TODO automatically run this in every state
                // change FSM state to logout
                this.arguments = new String[]{this.hostname};
                this.state = ServerStates.LOGOUT;
                break;
            case "STOR":
                // store command
                // check for required arguments
                if (this.arguments.length >= 2) {
                    StoreModes mode;
                    // check store mode
                    switch (this.arguments[0]) {
                        case "NEW":
                            mode = StoreModes.NEW;
                            break;
                        case "OLD":
                            mode = StoreModes.OLD;
                            break;
                        case "APP":
                            mode = StoreModes.APP;
                            break;
                        default:
                            return;
                    }
                    // format path
                    Path file = this.cwd.resolve(this.arguments[1]).toAbsolutePath();
                    this.arguments[1] = file.toString();
                    // handle store request
                    this.store(file, mode);
                    return;
                } else {
                    this.presentCommand.setError("Can't find null");
                }
                break;
        }

        this.writeToClient();
    }

    private void store(Path file, StoreModes mode) throws IOException {
        // update client
        this.writeToClient();
        if (this.presentCommand.response == SFTPResponses.ERR)
            return;
        this.loadInputData();

        // check size command recieved after store
        if (this.presentCommand.toString().equals("SIZE") && this.previousCommand.toString().equals("STOR") &&
                this.arguments.length >= 1) {
            // get size of file and tell client
            int size = Integer.parseInt(this.arguments[0].substring(0, this.arguments[0].length()-1));
            this.writeToClient();
            if (this.presentCommand.response == SFTPResponses.ERR)
                return;
            // set file writing mode
            OpenOption[] options = new OpenOption[]{StandardOpenOption.CREATE};
            if (Files.exists(file)) {
                switch (mode) {
                    case NEW -> {
                        // if file already exists create new file with same name and a numver
                        // e.g. if test.txt exists create a test(0).txt
                        String fileName = file.toString();
                        String prefix;
                        String suffix = "";
                        int indexOfDot;
                        if (fileName.contains(".")) {
                            indexOfDot = fileName.lastIndexOf('.');
                            suffix = fileName.substring(indexOfDot);
                            prefix = fileName.substring(0, indexOfDot);
                        } else {
                            prefix = fileName;
                        }

                        int i = 0;
                        do {
                            file = Paths.get(prefix + "(" + i + ")" + suffix);
                            i++;
                        } while (Files.exists(file));

                    }
                    case APP -> options[0] = StandardOpenOption.APPEND;
                    case OLD -> options[0] = StandardOpenOption.TRUNCATE_EXISTING;
                }
            }

            // write socket input stream data to file output stream
            try (OutputStream fos = Files.newOutputStream(file, options)) {
                byte[] buffer = new byte[4096];
                int total = 0;
                int length;
                while (total < size) {
                    length = this.connection.getInputStream().read(buffer);
                    total += length;
                    fos.write(buffer, 0, length);
                }
                fos.close();
                this.connection.writeToClient("+Saved " + file);
            } catch (IOException e) {
                e.printStackTrace();
                this.connection.writeToClient("-Couldn't save because an IOException occurred");
            }
        }
    }

    private void send(Path file) throws IOException {
        // update client
        this.writeToClient();
        if (this.presentCommand.response == SFTPResponses.ERR)
            return;
        this.loadInputData();

        // check send command after retrieve command
        if (this.presentCommand.toString().equals("SEND") && this.previousCommand.toString().equals("RETR")) {
            if (Files.exists(file)) { // check file does exist
                try (InputStream fis = new FileInputStream(file.toFile())) {
                    // copy file input stream data to socket output stream
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = fis.read(buffer)) > 0) {
                        this.connection.getOutputStream().write(buffer, 0, count);
                    }
                    fis.close();
                    this.connection.getOutputStream().flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (this.presentCommand.toString().equals("STOP") && this.previousCommand.toString().equals("RETR")) {
            // if stop command received after retrieve, then terminate operation
            this.writeToClient();
        }
    }

    private void tobe(Path fileOld) throws IOException {
        // update client
        this.writeToClient();
        if (this.presentCommand.response == SFTPResponses.ERR)
            return;
        this.loadInputData();

        // if tobe command recieved after name command
        if (this.presentCommand.toString().equals("TOBE") && this.previousCommand.toString().equals("NAME")) {
            // check for arguments
            if (this.arguments.length >= 1) {
                // rename file and tell client
                if (Files.exists(fileOld)) {
                    Files.move(fileOld, fileOld.resolveSibling(this.arguments[0]));
                    this.arguments[0] = fileOld + " renamed to " + this.arguments[0];
                } else {
                    this.presentCommand.setError("File wasn't renamed because original file doesn't exist");
                }
            } else {
                this.presentCommand.setError("File wasn't renamed because no file was specified");
            }

            this.writeToClient();
        }
    }

    private void logout() {
        // logout state method
        // close connection and stop thread
        this.connection.closeConnection();
        this.keepRunning = false;
    }

    private void loadInputData() throws IOException {
        // load client ascii message
        String input = this.connection.readFromClient();
        // truncate null character
        input = input.substring(0, input.length()-1);
        // set previous command and update present command
        this.previousCommand = this.presentCommand;
        this.presentCommand = interpretCommand(input);
        // get arguments from input
        this.arguments = getCommandArguments(input);
    }

    private SFTPResponses writeToClient() throws IOException {
        // execute command and write output to client
        SFTPResponses response = this.presentCommand.executeCommand(this.arguments);
        this.connection.writeToClient(this.presentCommand.getResponseData());
        return response;
    }

    private SFTPCommand interpretCommand(String command) {
        // extract the command name from the input text
        command = command.substring(0, 4);
        for (SFTPCommand cmd : this.commands) {
            if (cmd.toString().equals(command)) {
                return cmd;
            }
        }

        return null;
    }

    public static String[] getCommandArguments(String command) {
        // truncate command and separate arguments into array of strings
        String[] fullCommand = command.split(" ");
        return Arrays.copyOfRange(fullCommand, 1, fullCommand.length);
    }

    public void setServerState(ServerStates state) {
        // update server FSM state remotely
        this.state = state;
    }

    public ServerStates getServerState() {
        // get server FSM state
        return this.state;
    }

    public String[] getAccountData() {
        // get server account login data
        return this.accountData;
    }

    private String[] retrieveUser(String username) {
        // retireve user data from database file
        String[] data = new String[]{"null"};
        try {
            // read each line until username matches a line
            BufferedReader reader = new BufferedReader(new FileReader("database"));
            String line = reader.readLine();
            while (line != null) {
                // separate data using comma into array
                data = line.split(",");
                if (data[0].equals(username)) {
                    return data;
                } else {
                    data = new String[]{"null"};
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return data;
    }

    public SFTPCommand getPresentCommand() {
        // get command most recently received from client
        return this.presentCommand;
    }

    public String[] getArguments() {
        // get most recent command arguments received
        return this.arguments;
    }

    public void setStreamType(Types type) {
        // set output stream type
        this.streamType = type;
    }

    public Path getCurrentWorkingDirectory() {
        // get current working directory of session
        return this.cwd;
    }
}
