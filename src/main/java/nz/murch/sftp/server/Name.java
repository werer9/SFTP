package nz.murch.sftp.server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Name extends SFTPCommand {

    public Name(ServerSession session) { // rename file
        super(session);
        this.name = "NAME";
        this.success = SFTPResponses.SUCCESS + "File exists";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        // get filename
        Path file = Paths.get(args[0]);
        if (Files.exists(file)) { // tell client file found
            this.response = SFTPResponses.SUCCESS;
        } else { // file not found
            this.error = SFTPResponses.ERR + "Can't find " + args[0];
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
