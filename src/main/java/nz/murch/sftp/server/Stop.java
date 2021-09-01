package nz.murch.sftp.server;

public class Stop extends SFTPCommand {
    public Stop(ServerSession session) {
        super(session);
        this.name = "STOP";
        this.success = SFTPResponses.SUCCESS + "ok, RETR aborted";
        this.response = SFTPResponses.SUCCESS;
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        // tell client retrieve is aborted
        return this.response;
    }
}
