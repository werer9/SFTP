package nz.murch.sftp.server;

import java.io.File;

public class Size extends SFTPCommand {

    public Size(ServerSession session) {
        super(session);
        this.name = "SIZE";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        if (args.length >= 1) {
            // get size of file and file system
            long size = Integer.parseInt(args[0].substring(0, args[0].length()-1));
            long usableSpace = new File("/").getUsableSpace();
            if (usableSpace > size) { // check if there is enough space and tell client to send file
                this.success = SFTPResponses.SUCCESS + "ok, waiting for file";
                this.response = SFTPResponses.SUCCESS;
            } else { // not enough space, tell client don't send file
                this.error = SFTPResponses.ERR + "Not enough room, don't send it";
                this.response = SFTPResponses.ERR;
            }

        } else {
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }

}
