package nz.murch.sftp.server;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Name extends SFTPCommand {

    public Name(ServerSession session) {
        super(session);
        this.name = "NAME";
        this.success = SFTPResponses.SUCCESS + "File exists";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        Path file = Paths.get(args[0]);
        if (Files.exists(file)) {
            this.response = SFTPResponses.SUCCESS;
        } else {
            this.error = SFTPResponses.ERR + "Can't find " + args[0];
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
