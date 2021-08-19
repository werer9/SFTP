package nz.murch.sftp.server;

public class Account extends SFTPCommand {

    public Account() {
        this.name = "ACCT";
        this.login = SFTPResponses.LOGIN + " Account valid, logged-in";
        this.success = SFTPResponses.SUCCESS + "Account valid, send password";
        this.error = SFTPResponses.ERR + "Invalid account, try again";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        String account1 = "account1";
        String account2 = "account2";

        if (args[0].equals(account1)) {
            this.response = SFTPResponses.LOGIN;
        } else if (args[0].equals(account2)) {
            this.response = SFTPResponses.SUCCESS;
        } else {
            this.response = SFTPResponses.ERR;
        }

        return this.response;
    }
}
