package nz.murch.sftp.server;

public class Password extends SFTPCommand {
    public Password(ServerSession session) {
        super(session);
        this.name = "PASS";
        this.login = SFTPResponses.LOGIN + " Logged in";
        this.success = SFTPResponses.SUCCESS + "Send account";
        this.error = SFTPResponses.ERR + "Wrong password, try again";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        // get session info
        String[] accountData = this.session.getAccountData();
        String[] arguments = this.session.getArguments();

        if (accountData[accountData.length-1].equals(arguments[0])) { // password matches
            this.response = SFTPResponses.LOGIN;
        } else { // password doesn't match
            this.response = SFTPResponses.ERR;
        }

        if (response == SFTPResponses.SUCCESS) { // usage not implemented yet
            this.session.setServerState(ServerSession.ServerStates.ACCOUNT);
        } else if (response == SFTPResponses.LOGIN) {
            this.session.setServerState(ServerSession.ServerStates.COMMAND);
        }

        return this.response;
    }
}
