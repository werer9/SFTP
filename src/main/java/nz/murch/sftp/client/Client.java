package nz.murch.sftp.client;

import nz.murch.sftp.server.Server;

import java.io.*;
import java.net.Socket;
import java.nio.CharBuffer;

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
        this.output.writeUTF(requestData);
        String response = this.input.readUTF();

        return response;
    }

    public void closeSocket() {
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client();
            System.out.println(client.request("USER caelan\0"));
            client.closeSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
