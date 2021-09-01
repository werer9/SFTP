package nz.murch.sftp.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Vector;

public class ServerSession extends Thread {

    public enum ServerStates {
        WELCOME,
        USER,
        ACCOUNT,
        PASSWORD,
        COMMAND,
        LOGOUT,
    }

    public enum Types {
        ASCII,
        BINARY,
        CONTINUOUS
    }

    public enum StoreModes {
        NEW,
        OLD,
        APP
    }

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

    private String input;
    private SFTPCommand presentCommand;
    private SFTPCommand previousCommand;
    private String[] arguments;

    private Types streamType;

    private boolean keepRunning;

    public ServerSession(Socket socket, String hostname) throws IOException {
        this.connection = new ServerConnection(socket);
        this.state = ServerStates.WELCOME;
        this.streamType = Types.BINARY;
        this.cwd = Paths.get("");

        this.hostname = hostname;
        this.keepRunning = true;
    }

    @Override
    public void run() {
        while (keepRunning) {
            try {
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
        this.connection.writeToClient("+" + hostname + " Welcome :)");
        this.state = ServerStates.USER;
    }

    private void user() throws IOException {
        loadInputData();

        if (presentCommand.toString().equals("USER") && arguments[0] != null) {
            this.accountData = this.retrieveUser(arguments[0]);
            SFTPResponses response = this.writeToClient();
        }

    }

    private void account() throws IOException {
        this.loadInputData();

        assert this.presentCommand != null;
        if (this.presentCommand.toString().equals("ACCT") && this.arguments[0] != null) {
            SFTPResponses response = this.writeToClient();
        }
    }

    private void password() throws IOException {
        this.loadInputData();

        assert this.presentCommand != null;
        if (this.presentCommand.toString().equals("PASS") && this.arguments[0] != null) {
            SFTPResponses response = this.writeToClient();
        }
    }

    private void command() throws IOException {
        this.loadInputData();

        switch (this.presentCommand.toString()) {
            case "CDIR":
                // TODO Add locked files
                if (this.arguments.length >= 1) {
                    if (Files.isDirectory(this.cwd.resolve(this.arguments[0]))) {
                        this.cwd = this.cwd.resolve(this.arguments[0]).toAbsolutePath();
                        this.arguments[0] = this.cwd.toString();
                        System.setProperty("user.dir", this.cwd.toString());
                    } else {
                        this.presentCommand.setError("Specified path is not a directory");
                    }
                } else {
                    this.presentCommand.setError("No path specified");
                }
                break;
            case "NAME":
                if (this.arguments.length >= 1) {
                    Path file = this.cwd.resolve(this.arguments[0]).toAbsolutePath();
                    this.arguments[0] = file.toString();
                    this.tobe(file);
                    return;
                } else {
                    this.presentCommand.setError("Can't find null");
                }
                break;
            case "RETR":
                if (this.arguments.length >= 1) {
                    Path file = this.cwd.resolve(this.arguments[0]).toAbsolutePath();
                    this.arguments[0] = file.toString();
                    this.send(file);
                    return;
                } else {
                    this.presentCommand.setError("Can't find null");
                }
                break;
            case "DONE":
                // TODO automatically run this in every state
                this.arguments = new String[]{this.hostname};
                this.state = ServerStates.LOGOUT;
                break;
            case "STOR":
                if (this.arguments.length >= 2) {
                    StoreModes mode;
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
                    Path file = this.cwd.resolve(this.arguments[1]).toAbsolutePath();
                    this.arguments[1] = file.toString();
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
        this.writeToClient();
        if (this.presentCommand.response == SFTPResponses.ERR)
            return;
        this.loadInputData();

        if (this.presentCommand.toString().equals("SIZE") && this.previousCommand.toString().equals("STOR") &&
                this.arguments.length >= 1) {
            int size = Integer.parseInt(this.arguments[0].substring(0, this.arguments[0].length()-1));
            this.writeToClient();
            if (this.presentCommand.response == SFTPResponses.ERR)
                return;
            OpenOption[] options = new OpenOption[]{StandardOpenOption.CREATE};
            if (Files.exists(file)) {
                switch (mode) {
                    case NEW -> {
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
        this.writeToClient();
        if (this.presentCommand.response == SFTPResponses.ERR)
            return;
        this.loadInputData();

        if (this.presentCommand.toString().equals("SEND") && this.previousCommand.toString().equals("RETR")) {
            if (Files.exists(file)) {
                try (InputStream fis = new FileInputStream(file.toFile())) {
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
            this.writeToClient();
        }
    }

    private void tobe(Path fileOld) throws IOException {
        this.writeToClient();
        if (this.presentCommand.response == SFTPResponses.ERR)
            return;
        this.loadInputData();

        if (this.presentCommand.toString().equals("TOBE") && this.previousCommand.toString().equals("NAME")) {
            if (this.arguments.length >= 1) {
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
        this.connection.closeConnection();
        this.keepRunning = false;
    }

    private void loadInputData() throws IOException {
        this.input = this.connection.readFromClient();
        this.input = this.input.substring(0, this.input.length()-1);
        this.previousCommand = this.presentCommand;
        this.presentCommand = interpretCommand(input);
        this.arguments = getCommandArguments(input);
    }

    private SFTPResponses writeToClient() throws IOException {
        SFTPResponses response = this.presentCommand.executeCommand(this.arguments);
        this.connection.writeToClient(this.presentCommand.getResponseData());
        return response;
    }

    private SFTPCommand interpretCommand(String command) {
        command = command.substring(0, 4);
        for (SFTPCommand cmd : this.commands) {
            if (cmd.toString().equals(command)) {
                return cmd;
            }
        }

        return null;
    }

    public static String[] getCommandArguments(String command) {
        String[] fullCommand = command.split(" ");
        return Arrays.copyOfRange(fullCommand, 1, fullCommand.length);
    }

    public void setServerState(ServerStates state) {
        this.state = state;
    }

    public ServerStates getServerState() {
        return this.state;
    }

    public String[] getAccountData() {
        return this.accountData;
    }

    private String[] retrieveUser(String username) {
        String[] data = new String[]{"null"};
        try {
            BufferedReader reader = new BufferedReader(new FileReader("database"));
            String line = reader.readLine();
            while (line != null) {
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
        return this.presentCommand;
    }

    public String[] getArguments() {
        return this.arguments;
    }

    public void setStreamType(Types type) {
        this.streamType = type;
    }

    public Path getCurrentWorkingDirectory() {
        return this.cwd;
    }
}
