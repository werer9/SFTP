package nz.murch.sftp.server;

import java.io.File;

public class ToBe extends SFTPCommand {

    public ToBe() {
        super();
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
