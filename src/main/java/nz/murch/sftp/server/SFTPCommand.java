package nz.murch.sftp.server;

public abstract class SFTPCommand {
//    TYPE,
//    LIST,
//    CDIR,
//    KILL,
//    NAME,
//    DONE,
//    RETR,
//    STOR
    protected String name;
    protected String error;
    protected String success;
    protected String login;
    protected SFTPResponses response;


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
}
