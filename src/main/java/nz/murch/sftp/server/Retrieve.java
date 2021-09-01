package nz.murch.sftp.server;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Retrieve extends SFTPCommand {

    public Retrieve(ServerSession session) {
        super(session);
        this.name = "RETR";
        this.error = SFTPResponses.ERR + "File doesn't exist";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        Path file = Paths.get(args[0]);
        if (Files.exists(file)) {
            try {
                this.success = "" + Files.size(file);
                this.response = SFTPResponses.SUCCESS;
            } catch (IOException e) {
                this.response = SFTPResponses.ERR;
                e.printStackTrace();
            }

        } else {
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
