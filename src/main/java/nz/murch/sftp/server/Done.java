package nz.murch.sftp.server;

public class Done extends SFTPCommand {

    public Done(ServerSession session) {
        super(session);
        this.name = "DONE";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        this.success = SFTPResponses.SUCCESS + args[0] + " Closing connection. Thanks :)";
        this.response = SFTPResponses.SUCCESS;
        return this.response;
    }
}
