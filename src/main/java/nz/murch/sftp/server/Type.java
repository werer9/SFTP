package nz.murch.sftp.server;

public class Type extends SFTPCommand {

    public Type() {
        this.name = "TYPE";
        this.success = SFTPResponses.SUCCESS + "Using { Ascii | Binary | Continuous } mode\0";
        this.error = SFTPResponses.ERR + "Type not valid\0";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        switch (args[0]) {
            case "A":
                this.success = SFTPResponses.SUCCESS + "Using Ascii mode\0";
                this.response = SFTPResponses.SUCCESS;
            case "B":
                this.success = SFTPResponses.SUCCESS + "Using Binary mode\0";
                this.response = SFTPResponses.SUCCESS;
            case "C":
                this.success = SFTPResponses.SUCCESS + "Using Continuous mode\0";
                this.response = SFTPResponses.SUCCESS;
            default:
                this.response = SFTPResponses.ERR;

        }

        return this.response;
    }
}
