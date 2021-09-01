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
        String[] arguments = this.session.getArguments();
        String[] accountData = this.session.getAccountData();

        // check there is account data in database
        if (accountData[0].equals("null")) {
            this.response = SFTPResponses.ERR;
        } else {
            // handle account data, login if no password, account
            if (accountData.length == 1 && arguments[0].equals(accountData[0])) {
                this.response = SFTPResponses.LOGIN;
            } else if (accountData.length > 1 && arguments[0].equals(accountData[0])) { // ask for account
                this.response = SFTPResponses.SUCCESS;
            } else { // user doesn't exists
                this.response = SFTPResponses.ERR;
            }
        }

        if (response == SFTPResponses.SUCCESS) { // change server FSM state to accept account command
            this.session.setServerState(ServerSession.ServerStates.ACCOUNT);
        } else if (response == SFTPResponses.LOGIN) { // change server FSM state to accept non-login commands
            this.session.setServerState(ServerSession.ServerStates.COMMAND);
        }

        // tell client which user has been logged in
        String user = arguments[0];
        this.login = SFTPResponses.LOGIN + "" + user + " logged in";

        return response;
    }

}
