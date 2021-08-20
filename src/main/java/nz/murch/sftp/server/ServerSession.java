package nz.murch.sftp.server;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

public class ServerSession extends Thread {

    private enum States {
        WELCOME,
        USER,
        ACCOUNT,
        PASSWORD,
        COMMAND,
        COMMAND_CONT,
        LOGOUT,
    }

    private final Vector<SFTPCommand> commands = new Vector<>(Arrays.asList(
            new User(),
            new Account(),
            new Password(),
            new Type()
    ));

    private final ServerConnection connection;
    private States state;

    private final String hostname;
    private String username;
    private String account;

    private String input;
    private SFTPCommand presentCommand;
    private SFTPCommand previousCommand;
    private String[] arguments;

    public ServerSession(Socket socket, String hostname) throws IOException {
        this.connection = new ServerConnection(socket);
        this.state = States.WELCOME;

        this.hostname = hostname;
        this.username = "";
        this.account = "";
    }

    @Override
    public void run() {
        while (true) {
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
                }
            } catch (IOException e) {
                this.connection.closeConnection();
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
            SFTPResponses response = this.presentCommand.executeCommand(this.arguments);
            this.connection.writeToClient(this.presentCommand.getResponseData());
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
            SFTPResponses response = this.presentCommand.executeCommand(this.arguments);
            this.connection.writeToClient(this.presentCommand.getResponseData());
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
            SFTPResponses response = this.presentCommand.executeCommand(this.arguments);
            this.connection.writeToClient(this.presentCommand.getResponseData());
            if (response == SFTPResponses.SUCCESS) {
                this.state = States.ACCOUNT;
            } else if (response == SFTPResponses.LOGIN) {
                this.state = States.COMMAND;
            }

        }
    }

    private void command() throws IOException {
        this.loadInputData();

    }

    private void loadInputData() throws IOException {
        this.input = this.connection.readFromClient();
        this.previousCommand = this.presentCommand;
        this.presentCommand = interpretCommand(input);
        this.arguments = getCommandArguments(input);
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
