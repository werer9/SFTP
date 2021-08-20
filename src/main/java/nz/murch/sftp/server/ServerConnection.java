package nz.murch.sftp.server;


import java.io.*;
import java.net.Socket;

public class ServerConnection {
    private Socket connection;
    private DataInputStream fromClient;
    private DataOutputStream toClient;

    public ServerConnection(Socket connection) throws IOException {
        this.connection = connection;
        this.toClient = new DataOutputStream(this.connection.getOutputStream());
        this.fromClient = new DataInputStream(this.connection.getInputStream());
    }

    public void writeToClient(String data) throws IOException {
        this.toClient.writeUTF(data + '\0');
    }

    public String readFromClient() throws IOException {
        return this.fromClient.readLine();
    }

    public void closeConnection()  {
        try {
            this.toClient.close();
            this.fromClient.close();
            this.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Connection closed with" + this.connection.getInetAddress() + ":" +
                this.connection.getPort());
    }


 }
