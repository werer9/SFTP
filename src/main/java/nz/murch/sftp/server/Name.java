package nz.murch.sftp.server;

public class Name extends SFTPCommand {

    public Name() {
        super();
        this.name = "NAME";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        return null;
    }
}
