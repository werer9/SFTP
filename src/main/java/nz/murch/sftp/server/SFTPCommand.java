package nz.murch.sftp.server;

import java.io.IOException;

public abstract class SFTPCommand {
    // SFTP command super class

    protected String name;
    protected String error;
    protected String success;
    protected String login;
    protected SFTPResponses response;
    protected ServerSession session;

    protected boolean isError;

    public SFTPCommand(ServerSession session) {
        // link session to command
        this.session = session;
        this.isError = false;
    }

    // get command name in SFTP format
    public String toString() {
        return this.name;
    }

    // abstract method to be implemented by child class
    public abstract SFTPResponses executeCommand(String[] args) throws IOException;

    // return the appropriate response message depending on outcome of executeCommand
    public String getResponseData() {
        return switch (this.response) {
            case SUCCESS -> this.success;
            case LOGIN -> this.login;
            default -> this.error;
        };
    }

    // set response to error and set the error message
    public void setError(String msg) {
        this.error = SFTPResponses.ERR + msg;
        this.response = SFTPResponses.ERR;
        this.isError = true;
    }


}
