package nz.murch.sftp.server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Store extends SFTPCommand {

    public Store() {
        super();
        this.name = "STOR";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        if (args.length >= 2) {
            Path file = Paths.get(args[1]);
            boolean doesFileExist = Files.exists(file);
            switch (args[0]) {
                case "NEW" -> {
                    this.success = doesFileExist ? "File exists, will create new generation of file" :
                            "File does not exist, will create new file";
                    this.success = SFTPResponses.SUCCESS + this.success;
                    this.response = SFTPResponses.SUCCESS;
                }
                case "OLD" -> {
                    this.success = doesFileExist ? "Will write over old file" : "Will create new file";
                    this.success = SFTPResponses.SUCCESS + this.success;
                    this.response = SFTPResponses.SUCCESS;
                }
                case "APP" -> {
                    this.success = doesFileExist ? "Will append to file" : "Will create file";
                    this.success = SFTPResponses.SUCCESS + this.success;
                    this.response = SFTPResponses.SUCCESS;
                }
            }
        }

        return this.response;
    }

}
