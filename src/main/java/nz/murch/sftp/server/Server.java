package nz.murch.sftp.server;


import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server open");
        File file = new File("test");
        if (!file.exists()) {
            file.mkdir();
        }
        while (true) {
            try {
                socket = serverSocket.accept();
                Thread serverSession = new ServerSession(socket, "localhost");
                System.out.println("Connection established with: " + socket.getInetAddress() + ":" + socket.getPort());
                serverSession.start();
            } catch (IOException e) {
                socket.close();
                e.printStackTrace();
            }
        }

    }
}
