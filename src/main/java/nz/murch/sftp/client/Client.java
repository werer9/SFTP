package nz.murch.sftp.client;

import nz.murch.sftp.server.ServerSession;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client {

    private final DataInputStream input;
    private final DataOutputStream output;

    private final Socket clientSocket;

    public Client() throws IOException {
        this("localhost", 8080);
    }

    public Client(String host, int port) throws IOException {
        this.clientSocket = new Socket(host, port);
        this.output = new DataOutputStream(clientSocket.getOutputStream());
        this.input = new DataInputStream(clientSocket.getInputStream());
        System.out.println(this.input.readUTF());
    }

    public String request(String requestData) throws IOException {
        this.output.writeUTF(requestData + "\0");
        this.output.flush();

        return this.input.readUTF();
    }

    public void closeSocket() {
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String retrieveFile(String filename) throws IOException {
        String response = this.request("RETR " + filename);
        int size = Integer.parseInt(response.substring(0, response.length()-1));
        if (response.charAt(0) != '-') {
            Path file = Paths.get("client/" + filename);
            this.output.writeUTF("SEND" + "\0");
            this.output.flush();
            try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
                byte[] buffer = new byte[4096];
                int total = 0;
                int length;
                while (total < size) {
                    length = this.input.read(buffer);
                    total += length;
                    fos.write(buffer, 0, length);
                }
            }
            this.output.flush();
        }

        return response;
    }

    public String stop() throws IOException {
        return this.request("STOP");
    }

    public String storeFile(String filename, ServerSession.StoreModes mode) throws IOException {
        String response = this.request("STOR " + mode.toString() + " " + filename);

        if (response.charAt(0) != '-') {
            Path file = Paths.get("client/" + filename);
            long size = Files.size(file);
            response = this.request("SIZE " + size + "\0");
            if (response.charAt(0) != '-') {
                try (FileInputStream fis = new FileInputStream(file.toFile())) {
                    byte[] buffer = new byte[4096];
                    int count;
                    while ((count = fis.read(buffer)) >= 0) {
                        this.output.write(buffer, 0, count);
                    }
                }
            }
        }

        response = this.input.readUTF();
        return response;
    }

    public String user(String username) throws IOException {
        return this.request("USER " + username);
    }

    public String account(String account) throws IOException {
        return this.request("ACCT " + account);
    }

    public String password(String password) throws IOException {
        return this.request("PASS " + password);
    }

    public String type(ServerSession.Types type) throws IOException {
        String typeString = switch (type) {
            case ASCII -> "A";
            case BINARY -> "B";
            case CONTINUOUS -> "C";
        };
        return this.request("TYPE " + typeString);
    }

    public String kill(String path) throws IOException {
        return this.request("KILL " + path);
    }

    public String name(String path) throws IOException {
        return this.request("NAME " + path);
    }

    public String toBe(String path) throws IOException {
        return this.request("TOBE " + path);
    }

    public String changeDirectory(String path) throws IOException {
        return this.request("CDIR " + path);
    }

    public String list(String args) throws IOException {
        return this.request("LIST " + args);
    }

    public String done() throws IOException {
        return this.request("DONE");
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            System.out.println(client.user("user"));
            System.out.println(client.user("user1"));
            System.out.println(client.account("account2"));
            System.out.println(client.password("abc123"));
            System.out.println(client.password("password"));
            System.out.println(client.type(ServerSession.Types.ASCII));
            System.out.println(client.type(ServerSession.Types.CONTINUOUS));
            System.out.println(client.type(ServerSession.Types.BINARY));
            System.out.println(client.kill("fakefile"));
            System.out.println(client.kill("testfolder"));
            System.out.println(client.name("test"));
            System.out.println(client.toBe("testfolder"));
            System.out.println(client.changeDirectory("testfolder"));
            System.out.println(client.retrieveFile("test.txt"));
            System.out.println(client.storeFile("test2.txt", ServerSession.StoreModes.NEW));
            System.out.println(client.storeFile("test2.txt", ServerSession.StoreModes.OLD));
            System.out.println(client.storeFile("test2.txt", ServerSession.StoreModes.APP));
            System.out.println(client.storeFile("test2.txt", ServerSession.StoreModes.NEW));
            System.out.println(client.request("RETR " + "test.txt"));
            System.out.println(client.stop());
            System.out.println(client.changeDirectory(".."));
            System.out.println(client.kill("testfolder"));
            System.out.println(client.list("F ./"));
            System.out.println(client.list("V ./"));
            System.out.println(client.list("F"));
            System.out.println(client.changeDirectory("src"));
            System.out.println(client.list("F"));
            System.out.println(client.changeDirectory(".."));
            System.out.println(client.list("F"));
            System.out.println(client.changeDirectory("/"));
            System.out.println(client.list("F"));
            System.out.println(client.done());

            client.closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
