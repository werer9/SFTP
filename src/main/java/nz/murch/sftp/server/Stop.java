package nz.murch.sftp.server;

public class Stop extends SFTPCommand {
    public Stop() {
        super();
        this.name = "STOP";
        this.success = SFTPResponses.SUCCESS + "ok, RETR aborted";
        this.response = SFTPResponses.SUCCESS;
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        return this.response;
    }
}
