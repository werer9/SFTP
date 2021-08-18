package nz.murch.sftp;

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
        while (keepRunning) {
            switch (this.state) {
                case LOGIN ->
                        // wait for a login response
                        this.login();
                case NORMAL ->
                        // wait for any command
                        this.normal();
                case LOAD ->
                        // if command requires more info, wait for that here
                        this.load();
                case DISCONNECT ->
                        // close thread
                        this.disconnect();
            }
        }
    }

    public void stop() throws IOException {
        this.clientSocket.close();
        this.input.close();
        this.output.close();
        this.keepRunning = false;
    }

    private void login() {
        String command = "";
        try {
            command = this.input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SFTPCommand cmd = interpretCommand(command);


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

    private SFTPCommand interpretCommand(String command) {
        command = command.substring(0, 4);
        for (SFTPCommand cmd : this.commands) {
            if (cmd.toString().equals(command)) {
                return cmd;
            }
        }

        return null;
    }
 }
