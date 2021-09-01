package nz.murch.sftp.server;

public class ChangeDirectory extends SFTPCommand {

    public ChangeDirectory(ServerSession session) {
        super(session);
        this.name = "CDIR";
        this.error = SFTPResponses.ERR + "Can't connect to directory because: ";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        // reset error flag
        if (this.isError) {
            this.isError = false;
            return this.response;
        }

        if (!args[0].equals("")) { // if there is a path specified
            this.login = SFTPResponses.LOGIN + "Changed working dir to " + args[0];
            this.response = SFTPResponses.LOGIN;
        } else {
            this.error += "No path specified or path specified is invalid";
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
