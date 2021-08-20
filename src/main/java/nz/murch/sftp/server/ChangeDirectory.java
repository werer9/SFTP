package nz.murch.sftp.server;

public class ChangeDirectory extends SFTPCommand {

    public ChangeDirectory() {
        this.name = "CDIR";
        this.error = SFTPResponses.ERR + "Can't connect to directory because: ";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        if (!args[1].equals("")) {
            this.login = SFTPResponses.LOGIN + "Changed working dir to " + args[1];
            this.response = SFTPResponses.LOGIN;
        } else {
            this.error += "No path specified or path specified is invalid";
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
