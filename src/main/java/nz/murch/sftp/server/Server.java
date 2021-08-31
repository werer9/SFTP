package nz.murch.sftp.server;


import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Server {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();
        generateTestFile();
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("Server open");
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

    private static void generateTestFile() {
        Path file = Paths.get("test");
        if (!Files.exists(file)) {
            try {
                Files.createDirectories(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        file = Paths.get("test/test.txt");
        Charset charset = StandardCharsets.US_ASCII;
        String s = "test";
        try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
            writer.write(s, 0, s.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
