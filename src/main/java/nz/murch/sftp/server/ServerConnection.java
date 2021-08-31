package nz.murch.sftp.server;


import java.io.*;
import java.net.Socket;

public class ServerConnection {
    private final Socket connection;
    private final DataInputStream fromClient;
    private final DataOutputStream toClient;


    public ServerConnection(Socket connection) throws IOException {
        this.connection = connection;
        this.toClient = new DataOutputStream(this.connection.getOutputStream());
        this.fromClient = new DataInputStream(this.connection.getInputStream());
    }

    public void writeToClient(String data) throws IOException {
        this.toClient.writeUTF(data + "\0");
        this.toClient.flush();
    }

    public DataOutputStream getOutputStream() {
        return this.toClient;
    }

    public DataInputStream getInputStream() {
        return this.fromClient;
    }

    public String readFromClient() throws IOException {
        return this.fromClient.readUTF();
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
