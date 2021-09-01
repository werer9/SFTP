package nz.murch.sftp.server;

public abstract class SFTPCommand {
    protected String name;
    protected String error;
    protected String success;
    protected String login;
    protected SFTPResponses response;

    protected boolean isError;

    public SFTPCommand() {
        this.isError = false;
    }

    public String toString() {
        return this.name;
    }

    public abstract SFTPResponses executeCommand(String[] args);

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
