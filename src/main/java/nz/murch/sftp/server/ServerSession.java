package nz.murch.sftp.server;

import java.io.IOException;
import java.net.Socket;

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


    private ServerConnection connection;
    private States state;

    private String hostname;

    public ServerSession(Socket socket, String hostname) throws IOException {
        this.connection = new ServerConnection(socket);
        this.state = States.WELCOME;

        this.hostname = hostname;
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
        String input = this.connection.readFromClient();
    }
}
