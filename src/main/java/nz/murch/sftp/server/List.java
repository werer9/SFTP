package nz.murch.sftp.server;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

public class List extends SFTPCommand {

    public List() {
        this.name = "LIST";
    }


    @Override
    public SFTPResponses executeCommand(String[] args) {
        if (args[1] != null) {
            Path directory = Paths.get(args[1]);
            ArrayList<Path> fileList = new ArrayList<>();
            try (DirectoryStream<Path> paths = Files.newDirectoryStream(directory)){
                paths.forEach(fileList::add);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if (args[0].equals("F")) {
                this.success = SFTPResponses.SUCCESS + directory.toString() + "\r\n";
                for (Path file : fileList) {
                    this.success = String.format("%s%s\r\n", this.success, file.getFileName());
                }
                this.response = SFTPResponses.SUCCESS;
            } else if (args[0].equals("V")) {
                this.success = SFTPResponses.SUCCESS + directory.toString() + "\r\n";
                for (Path file : fileList) {
                    try {
                        BasicFileAttributes attributes = Files.readAttributes(file, BasicFileAttributes.class);
                        this.success = String.format("%s%s\t\t %s\t\t %s\r\n", this.success, file.getFileName(),
                                attributes.size(), attributes.lastModifiedTime());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                this.response = SFTPResponses.SUCCESS;
            } else {
                this.error = SFTPResponses.ERR + "List argument invalid";
                this.response = SFTPResponses.ERR;
            }
        } else {
            this.error = SFTPResponses.ERR + "Directory not specified or present";
            this.response = SFTPResponses.ERR;
        }


        return this.response;
    }
}
