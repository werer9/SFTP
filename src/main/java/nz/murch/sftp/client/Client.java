package nz.murch.sftp.client;

import nz.murch.sftp.server.ServerSession;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Client {

    private DataInputStream input;
    private DataOutputStream output;

    private Socket clientSocket;

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

    public static void main(String[] args) {
        try {
            Client client = new Client();
            System.out.println(client.request("USER caelan"));
            System.out.println(client.request("USER user1"));
            System.out.println(client.request("ACCT account2"));
            System.out.println(client.request("PASS abc123"));
            System.out.println(client.request("PASS password"));
            System.out.println(client.request("TYPE A"));
            System.out.println(client.request("TYPE C"));
            System.out.println(client.request("TYPE B"));
            System.out.println(client.request("KILL fakefile"));
            System.out.println(client.request("KILL testfolder"));
            System.out.println(client.request("NAME test"));
            System.out.println(client.request("TOBE testfolder"));
            System.out.println(client.request("CDIR testfolder"));
            System.out.println(client.retrieveFile("test.txt"));
            System.out.println(client.storeFile("test2.txt", ServerSession.StoreModes.NEW));
            System.out.println(client.storeFile("test2.txt", ServerSession.StoreModes.APP));
            System.out.println(client.request("RETR " + "test.txt"));
            System.out.println(client.request("STOP"));
            System.out.println(client.request("CDIR .."));
            System.out.println(client.request("KILL testfolder"));
            System.out.println(client.request("LIST F ./"));
            System.out.println(client.request("LIST V ./"));
            System.out.println(client.request("LIST F"));
            System.out.println(client.request("CDIR src"));
            System.out.println(client.request("LIST F"));
            System.out.println(client.request("CDIR .."));
            System.out.println(client.request("LIST F"));
            System.out.println(client.request("CDIR /"));
            System.out.println(client.request("LIST F"));
            System.out.println(client.request("DONE"));

            client.closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
