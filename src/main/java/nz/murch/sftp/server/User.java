package nz.murch.sftp.server;

public class User extends SFTPCommand {

    public User() {
        this.name = "USER";
        this.success = SFTPResponses.SUCCESS + "User-id valid, send account and password\0";
        this.error = SFTPResponses.ERR + "Invalid user-id, try again\0";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        SFTPResponses response;
        String user2 = "user2";
        String user1 = "user1";
        if (args[0].equals(user1)) {
            response = SFTPResponses.SUCCESS;
        } else if (args[0].equals(user2)) {
            response = SFTPResponses.LOGIN;
        } else {
            response = SFTPResponses.ERR;
        }
        String user = args[0];
        this.login = SFTPResponses.LOGIN + "" + user + " logged in\0";

        return response;
    }

}
