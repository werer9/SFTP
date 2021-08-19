package nz.murch.sftp.client;

import nz.murch.sftp.server.Server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.CharBuffer;

public class Client {

    private BufferedReader input;
    private DataOutputStream output;

    private Socket clientSocket;

    public Client() throws IOException {
        this("localhost", 8080);
    }

    public Client(String host, int port) throws IOException {
        this.clientSocket = new Socket(host, port);
        this.output = new DataOutputStream(clientSocket.getOutputStream());
        this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println(this.input.readLine());
    }

    public String request(String requestData) throws IOException {
        this.output.writeBytes(requestData);
        String response = this.input.readLine();

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
