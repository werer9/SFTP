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
        // Send request as ascii to server
        this.output.writeUTF(requestData + "\0");
        this.output.flush();

        // return the server response
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
        // see if file exists in server
        String response = this.request("RETR " + filename);
        if (response.charAt(0) != '-') {
            // get size
            int size = Integer.parseInt(response.substring(0, response.length()-1));
            // save file in client folder
            Path file = Paths.get("client/" + filename);
            // tell server to send the file
            this.output.writeUTF("SEND" + "\0");
            this.output.flush();
            // copy the data from the input stream from the socket to a file output stream/stream file data from server
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

        // return final response from server
        return response;
    }

    public String stop() throws IOException {
        // stop retrieve command
        return this.request("STOP");
    }

    public String storeFile(String filename, ServerSession.StoreModes mode) throws IOException {
        // send file to server request
        String response = this.request("STOR " + mode.toString() + " " + filename);

        // check if server accepts this request
        if (response.charAt(0) != '-') {
            // get file from client folder and send its size in bytes to server
            Path file = Paths.get("client/" + filename);
            long size = Files.size(file);
            response = this.request("SIZE " + size + "\0");
            // check if server accepts file size
            if (response.charAt(0) != '-') {
                // copy data from file input stream to socket output stream - stream file data to server
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
        // user request
        return this.request("USER " + username);
    }

    public String account(String account) throws IOException {
        // account request
        return this.request("ACCT " + account);
    }

    public String password(String password) throws IOException {
        // password request
        return this.request("PASS " + password);
    }

    public String type(ServerSession.Types type) throws IOException {
        // set data type request
        String typeString = switch (type) {
            case ASCII -> "A";
            case BINARY -> "B";
            case CONTINUOUS -> "C";
        };
        return this.request("TYPE " + typeString);
    }

    public String kill(String path) throws IOException {
        // delete file request
        return this.request("KILL " + path);
    }

    public String name(String path) throws IOException {
        // rename file request
        return this.request("NAME " + path);
    }

    public String toBe(String path) throws IOException {
        // rename file request tobe filename
        return this.request("TOBE " + path);
    }

    public String changeDirectory(String path) throws IOException {
        // change working directory of server
        return this.request("CDIR " + path);
    }

    public String list(String args) throws IOException {
        // list contents of a directory on server
        return this.request("LIST " + args);
    }

    public String done() throws IOException {
        // tell server session is done
        return this.request("DONE");
    }

    public static void main(String[] args) {
        try {
            // basic tests of functionality
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
