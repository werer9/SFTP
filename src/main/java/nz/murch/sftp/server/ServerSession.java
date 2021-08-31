package nz.murch.sftp.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Vector;

public class ServerSession extends Thread {

    private enum States {
        WELCOME,
        USER,
        ACCOUNT,
        PASSWORD,
        COMMAND,
        LOGOUT,
    }

    private enum Types {
        ASCII,
        BINARY,
        CONTINUOUS
    }

    private final Vector<SFTPCommand> commands = new Vector<>(Arrays.asList(
            new User(),
            new Account(),
            new Password(),
            new Type(),
            new List(),
            new ChangeDirectory(),
            new Kill(),
            new Name(),
            new ToBe(),
            new Done(),
            new Retrieve(),
            new Send(),
            new Stop()
    ));

    private final ServerConnection connection;
    private States state;

    private final String hostname;
    private String username;
    private String account;
    private Path cwd;

    private String input;
    private SFTPCommand presentCommand;
    private SFTPCommand previousCommand;
    private String[] arguments;

    private Types streamType;

    private boolean keepRunning;

    public ServerSession(Socket socket, String hostname) throws IOException {
        this.connection = new ServerConnection(socket);
        this.state = States.WELCOME;
        this.streamType = Types.BINARY;
        this.cwd = Paths.get("");

        this.hostname = hostname;
        this.username = "";
        this.account = "";
        this.keepRunning = true;
    }

    @Override
    public void run() {
        while (keepRunning) {
            try {
                switch (this.state) {
                    case WELCOME:
                        this.welcome();
                        break;
                    case USER:
                        this.user();
                        break;
                    case ACCOUNT:
                        this.account();
                        break;
                    case PASSWORD:
                        this.password();
                        break;
                    case COMMAND:
                        this.command();
                        break;
                    case LOGOUT:
                        this.logout();
                        break;
                }
            } catch (IOException e) {
                this.state = States.LOGOUT;
                e.printStackTrace();
            }
        }
    }

    private void welcome() throws IOException {
        this.connection.writeToClient("+" + hostname + " Welcome :)");
        this.state = States.USER;
    }

    private void user() throws IOException {
        loadInputData();

        assert this.presentCommand != null;
        if (this.presentCommand.toString().equals("USER") && this.arguments[0] != null) {
            this.username = this.arguments[0];
            SFTPResponses response = this.writeToClient();
            if (response == SFTPResponses.SUCCESS) {
                this.state = States.ACCOUNT;
            } else if (response == SFTPResponses.LOGIN) {
                this.state = States.COMMAND;
            }

        }
    }

    private void account() throws IOException {
        this.loadInputData();

        assert this.presentCommand != null;
        if (this.presentCommand.toString().equals("ACCT") && this.arguments[0] != null) {
            this.account = this.arguments[0];
            SFTPResponses response = this.writeToClient();
            if (response == SFTPResponses.SUCCESS) {
                this.state = States.PASSWORD;
            } else if (response == SFTPResponses.LOGIN) {
                this.state = States.COMMAND;
            }

        }
    }

    private void password() throws IOException {
        this.loadInputData();

        assert this.presentCommand != null;
        if (this.presentCommand.toString().equals("PASS") && this.arguments[0] != null) {
            SFTPResponses response = this.writeToClient();
            if (response == SFTPResponses.SUCCESS) {
                this.state = States.ACCOUNT;
            } else if (response == SFTPResponses.LOGIN) {
                this.state = States.COMMAND;
            }

        }
    }

    private void command() throws IOException {
        this.loadInputData();

        switch (this.presentCommand.toString()) {
            case "TYPE":
                if (this.arguments[0] != null) {
                    switch (this.arguments[0]) {
                        case "A" -> this.streamType = Types.ASCII;
                        case "B" -> this.streamType = Types.BINARY;
                        case "C" -> this.streamType = Types.CONTINUOUS;
                    }
                }
                break;
            case "LIST":
                if (this.arguments[1].equals("./") || this.arguments[1].equals("\0")) {
                    this.arguments[1] = this.cwd.toString();
                }
                break;
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
            case "KILL":
                if (this.arguments.length >= 1) {
                    Path file = this.cwd.resolve(this.arguments[0]).toAbsolutePath();
                    this.arguments[0] = file.toString();
                } else {
                    this.presentCommand.setError("Not deleted because no path specified");
                }
                break;
            case "NAME":
                if (this.arguments.length >= 1) {
                    Path file = this.cwd.resolve(this.arguments[0]).toAbsolutePath();
                    this.arguments[0] = file.toString();
                    this.tobe(file.toFile());
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
                this.arguments[0] = this.hostname;
                this.state = States.LOGOUT;
                break;
            default:
                return;
        }

        this.writeToClient();
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

    private void tobe(File fileOld) throws IOException {
        this.writeToClient();
        if (this.presentCommand.response == SFTPResponses.ERR)
            return;
        this.loadInputData();

        if (this.presentCommand.toString().equals("TOBE") && this.previousCommand.toString().equals("NAME")) {
            if (this.arguments.length >= 1) {
                File fileNew = this.cwd.resolve(this.arguments[0]).toFile();

                if (fileOld.exists()) {
                    if (fileOld.renameTo(fileNew)) {
                        this.arguments[0] = fileOld + " renamed to " + fileNew;
                    } else {
                        this.presentCommand.setError("File wasn't renamed because file already exists or " +
                                "insufficient privileges");
                    }
                } else {
                    this.presentCommand.setError("File wasn't renamed because original file doesn't exist");
                }
            } else {
                this.presentCommand.setError("File wasn't renamed because no file was specified");
            }
        }
    }

    private void logout() {
        this.connection.closeConnection();
        this.keepRunning = false;
    }

    private void loadInputData() throws IOException {
        this.input = this.connection.readFromClient();
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
}
