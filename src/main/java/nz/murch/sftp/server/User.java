package nz.murch.sftp.server;

public class User extends SFTPCommand {

    public User() {
        this.name = "USER";
        this.success = SFTPResponses.SUCCESS + "User-id valid, send account and password";
        this.error = SFTPResponses.ERR + "Invalid user-id, try again";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        String user2 = "user2";
        String user1 = "user1";
        if (args[0].equals(user1)) {
            this.response = SFTPResponses.SUCCESS;
        } else if (args[0].equals(user2)) {
            this.response = SFTPResponses.LOGIN;
        } else {
            this.response = SFTPResponses.ERR;
        }
        String user = args[0];
        this.login = SFTPResponses.LOGIN + "" + user + " logged in";

        return response;
    }

}
