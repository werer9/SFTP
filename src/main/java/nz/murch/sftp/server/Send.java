package nz.murch.sftp.server;

public class Send extends SFTPCommand {
    public Send(ServerSession session) { // send command data, this does not interact with socket
        super(session);
        this.name = "SEND";
        this.success = "";
        this.response = SFTPResponses.SUCCESS;
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        return this.response;
    }


}
