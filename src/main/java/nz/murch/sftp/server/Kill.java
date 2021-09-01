package nz.murch.sftp.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

public class Kill extends SFTPCommand{

    private String[] arguments;

    public Kill(ServerSession session) {
        super(session);
        this.name = "KILL";
        this.error = SFTPResponses.ERR + "Not deleted because file not found or insufficient privileges";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        this.arguments = this.session.getArguments();
        Path cwd = this.session.getCurrentWorkingDirectory();
        Path file;

        if (arguments.length >= 1) {
            file = cwd.resolve(arguments[0]).toAbsolutePath();
            arguments[0] = file.toString();
            deleteFile(file);
        } else {
            setError("Not deleted because no path specified");
            return this.response;
        }



        return this.response;
    }

    private void deleteFile(Path file) {
        if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(file)) {
                for (Path entry : entries) {
                    this.deleteFile(entry);
                }

                Files.delete(file);
            } catch (IOException e) {
                e.printStackTrace();
                this.response = SFTPResponses.ERR;
            }

        } else if (Files.exists(file)) {
            try {
                Files.delete(file);
                this.response = SFTPResponses.SUCCESS;
            } catch (IOException e) {
                e.printStackTrace();
                this.response = SFTPResponses.ERR;
            }
        } else {
            this.error = SFTPResponses.ERR + "Not deleted because file not found";
            this.response = SFTPResponses.ERR;
            return;
        }

        this.success = SFTPResponses.SUCCESS + this.arguments[0] + " deleted";
    }


}
