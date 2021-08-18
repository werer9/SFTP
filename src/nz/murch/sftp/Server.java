package nz.murch.sftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private final int MAX_THREADS = 20;

    private String hostname;
    private int port;

    private ServerSocket serverSocket;
    private Socket clientSocket;

    public Server() {
        this("localhost", 8080);
    }

    public Server(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private void waitForRequest() throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            clientSocket = serverSocket.accept();
            ServerConnection serverConnection = new ServerConnection(clientSocket);
            serverConnection.run();
        }
    }

    public static void main(String[] args) {
        Server server = new Server();
        try {
            server.waitForRequest();
            server.serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
