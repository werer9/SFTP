package nz.murch.sftp.server;

public class ToBe extends SFTPCommand {

    public ToBe(ServerSession session) {
        super(session);
        this.name = "TOBE";
        this.response = SFTPResponses.SUCCESS;
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        if (this.response != SFTPResponses.ERR) {
            this.response = SFTPResponses.SUCCESS;
            this.success = SFTPResponses.SUCCESS + args[0];
        }

        return this.response;
    }
}
