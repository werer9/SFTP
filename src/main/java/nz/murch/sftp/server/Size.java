package nz.murch.sftp.server;

import java.io.File;
import java.io.IOException;

public class Size extends SFTPCommand {

    public Size() {
        super();
        this.name = "SIZE";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        if (args.length >= 1) {
            long size = Integer.parseInt(args[0].substring(0, args[0].length()-1));
            long usableSpace = new File("/").getUsableSpace();
            if (usableSpace > size) {
                this.success = SFTPResponses.SUCCESS + "ok, waiting for file";
                this.response = SFTPResponses.SUCCESS;
            } else {
                this.error = SFTPResponses.ERR + "Not enough room, don't send it";
                this.response = SFTPResponses.ERR;
            }

        } else {
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }

}
