package nz.murch.sftp.server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Arrays;
import java.util.Vector;

public class ServerConnection implements Runnable {

    private enum States {
        DISCONNECT,
        LOAD,
        LOGIN,
        NORMAL

    }

    private States state;
    private Socket clientSocket;

    private BufferedReader input;
    private DataOutputStream output;

    private Vector<SFTPCommand> commands = new Vector<>(Arrays.asList(
            new User(),
            new Account(),
            new Password(),
            new Type()
    ));

    private boolean loginReceived;
    private boolean keepRunning;

    public ServerConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.state = States.LOGIN;
        this.loginReceived = false;
        this.keepRunning = true;

        try {
            this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            this.output = new DataOutputStream(this.clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            this.output.writeBytes("+localhost Welcome :)\0");
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (keepRunning) {
            switch (this.state) {
                case LOGIN:
                        // wait for a login response
                        try {
                            this.login();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                case NORMAL:
                        // wait for any command
                        this.normal();
                        break;
                case LOAD:
                        // if command requires more info, wait for that here
                        this.load();
                        break;
                case DISCONNECT:
                        // close thread
                        this.disconnect();
                        break;
            }
        }
    }

    public void stop() throws IOException {
        this.clientSocket.close();
        this.input.close();
        this.output.close();
        this.keepRunning = false;
    }

    private void login() throws IOException {
        String command = "";
        command = this.input.readLine();

        SFTPCommand cmd = interpretCommand(command);
        assert cmd != null;
        SFTPResponses response = cmd.executeCommand(getCommandArguments(command));

        output.writeBytes(cmd.getResponseData());

        if (response == SFTPResponses.LOGIN) {
            this.loginReceived = true;
        }

        if (this.loginReceived) {
            this.state = States.NORMAL;
        }
}

    private void normal() {

    }

    private void load() {

    }

    private void disconnect() {
        try {
            this.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public SFTPCommand interpretCommand(String command) {
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
