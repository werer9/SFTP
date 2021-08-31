package nz.murch.sftp.server;

public class Password extends SFTPCommand {
    public Password() {
        this.name = "PASS";
        this.login = SFTPResponses.LOGIN + " Logged in";
        this.success = SFTPResponses.SUCCESS + "Send account";
        this.error = SFTPResponses.ERR + "Wrong password, try again";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        String password = "password";
        if (args[0].equals(password)) {
            this.response = SFTPResponses.LOGIN;
        } else {
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
