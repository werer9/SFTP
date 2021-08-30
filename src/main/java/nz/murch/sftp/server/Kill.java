package nz.murch.sftp.server;

import java.io.File;

public class Kill extends SFTPCommand{
    public Kill() {
        super();
        this.name = "KILL";
        this.error = SFTPResponses.ERR + "Not deleted because file not found or insufficient privileges";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        File file = new File(args[0]);
        if (file.exists()) {
            this.success = SFTPResponses.SUCCESS + args[0] + " deleted";
            this.response = file.delete() ? SFTPResponses.SUCCESS : SFTPResponses.ERR;
        } else {
            this.error = SFTPResponses.ERR + "Not deleted because file not found";
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
