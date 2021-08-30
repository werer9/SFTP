package nz.murch.sftp.server;

import java.io.File;

public class Name extends SFTPCommand {

    public Name() {
        super();
        this.name = "NAME";
        this.success = SFTPResponses.SUCCESS + "File exists";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        File file = new File(args[0]);
        if (file.exists()) {
            this.response = SFTPResponses.SUCCESS;
        } else {
            this.error = SFTPResponses.ERR + "Can't find " + args[0];
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
