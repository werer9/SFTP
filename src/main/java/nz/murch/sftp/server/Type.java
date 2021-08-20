package nz.murch.sftp.server;

public class Type extends SFTPCommand {

    public Type() {
        this.name = "TYPE";
        this.success = SFTPResponses.SUCCESS + "Using { Ascii | Binary | Continuous } mode";
        this.error = SFTPResponses.ERR + "Type not valid";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        switch (args[0]) {
            case "A":
                this.success = SFTPResponses.SUCCESS + "Using Ascii mode";
                this.response = SFTPResponses.SUCCESS;
            case "B":
                this.success = SFTPResponses.SUCCESS + "Using Binary mode";
                this.response = SFTPResponses.SUCCESS;
            case "C":
                this.success = SFTPResponses.SUCCESS + "Using Continuous mode";
                this.response = SFTPResponses.SUCCESS;
            default:
                this.response = SFTPResponses.ERR;

        }

        return this.response;
    }
}
