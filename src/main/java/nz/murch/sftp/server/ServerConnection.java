package nz.murch.sftp.server;


import java.io.*;
import java.net.Socket;

public class ServerConnection {
    private final Socket connection;
    private final DataInputStream fromClient;
    private final DataOutputStream toClient;


    public ServerConnection(Socket connection) throws IOException {
        // get connection socket and create input and output streams with socket
        this.connection = connection;
        this.toClient = new DataOutputStream(this.connection.getOutputStream());
        this.fromClient = new DataInputStream(this.connection.getInputStream());
    }

    public void writeToClient(String data) throws IOException {
        // write ascii message to the client
        this.toClient.writeUTF(data + "\0");
        this.toClient.flush();
    }

    public DataOutputStream getOutputStream() {
        // get socket output stream
        return this.toClient;
    }

    public DataInputStream getInputStream() {
        // get socket input stream
        return this.fromClient;
    }

    public String readFromClient() throws IOException {
        // read ascii message from client
        return this.fromClient.readUTF();
    }

    public void closeConnection()  {
        // close the socket and its associated streams
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
