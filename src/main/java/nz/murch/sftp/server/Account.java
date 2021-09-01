package nz.murch.sftp.server;

public class Account extends SFTPCommand {

    public Account(ServerSession session) {
        // set name of command and default responses
        super(session);
        this.name = "ACCT";
        this.login = SFTPResponses.LOGIN + " Account valid, logged-in";
        this.success = SFTPResponses.SUCCESS + "Account valid, send password";
        this.error = SFTPResponses.ERR + "Invalid account, try again";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        // load data from server session
        String[] accountData = session.getAccountData();
        String[] arguments = session.getArguments();
        
        // lookup account names for user and check if they match arguments
        if (accountData.length == 2 && accountData[1].equals(arguments[0])) {
            // no password needed
            this.response = SFTPResponses.LOGIN;
        } else {
            this.response = SFTPResponses.ERR;
            // check for multiple accounts
            if (accountData.length > 2) {
                for (int i = 1; i < accountData.length-1; i++) {
                    if (accountData[i].equals(arguments[0])) {
                        this.response = SFTPResponses.SUCCESS;
                        break;
                    }
                }
            }
        }

        // change server state depending on response of command
        if (response == SFTPResponses.SUCCESS) {
            session.setServerState(ServerSession.ServerStates.PASSWORD);
        } else if (response == SFTPResponses.LOGIN) {
            session.setServerState(ServerSession.ServerStates.COMMAND);
        }

        return this.response;
    }
}
