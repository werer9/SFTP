package nz.murch.sftp.server;

public class Send extends SFTPCommand {
    public Send() {
        super();
        this.name = "SEND";
        this.success = "";
        this.response = SFTPResponses.SUCCESS;
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        return this.response;
    }


}
