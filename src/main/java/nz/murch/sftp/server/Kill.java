package nz.murch.sftp.server;

public class Kill extends SFTPCommand{
    public Kill() {
        super();
        this.name = "KILL";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        return this.response;
    }
}
