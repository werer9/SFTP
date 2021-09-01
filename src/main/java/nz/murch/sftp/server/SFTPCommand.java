package nz.murch.sftp.server;

import java.io.IOException;

public abstract class SFTPCommand {
    protected String name;
    protected String error;
    protected String success;
    protected String login;
    protected SFTPResponses response;
    protected ServerSession session;

    protected boolean isError;

    public SFTPCommand(ServerSession session) {
        this.session = session;
        this.isError = false;
    }

    public String toString() {
        return this.name;
    }

    public abstract SFTPResponses executeCommand(String[] args) throws IOException;

    public String getResponseData() {
        return switch (this.response) {
            case SUCCESS -> this.success;
            case LOGIN -> this.login;
            default -> this.error;
        };
    }

    public void setError(String msg) {
        this.error = SFTPResponses.ERR + msg;
        this.response = SFTPResponses.ERR;
        this.isError = true;
    }


}
