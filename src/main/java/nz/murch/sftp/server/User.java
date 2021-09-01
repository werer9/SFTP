package nz.murch.sftp.server;

public class User extends SFTPCommand {

    public User(ServerSession session) {
        super(session);
        this.name = "USER";
        this.success = SFTPResponses.SUCCESS + "User-id valid, send account and password";
        this.error = SFTPResponses.ERR + "Invalid user-id, try again";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        SFTPCommand presentCommand = this.session.getPresentCommand();
        String[] arguments = this.session.getArguments();
        String[] accountData = this.session.getAccountData();

        if (accountData[0].equals("null")) {
            this.response = SFTPResponses.ERR;
        } else {
            if (accountData.length == 1 && arguments[0].equals(accountData[0])) {
                this.response = SFTPResponses.LOGIN;
            } else if (accountData.length > 1 && arguments[0].equals(accountData[0])) {
                this.response = SFTPResponses.SUCCESS;
            } else {
                this.response = SFTPResponses.ERR;
            }
        }

        if (response == SFTPResponses.SUCCESS) {
            this.session.setServerState(ServerSession.ServerStates.ACCOUNT);
        } else if (response == SFTPResponses.LOGIN) {
            this.session.setServerState(ServerSession.ServerStates.COMMAND);
        }

        String user = arguments[0];
        this.login = SFTPResponses.LOGIN + "" + user + " logged in";

        return response;
    }

}
