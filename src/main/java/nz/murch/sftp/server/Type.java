package nz.murch.sftp.server;

public class Type extends SFTPCommand {

    public Type(ServerSession session) {
        super(session);
        this.name = "TYPE";
        this.success = SFTPResponses.SUCCESS + "Using { Ascii | Binary | Continuous } mode";
        this.error = SFTPResponses.ERR + "Type not valid";
    }

    @Override
    public SFTPResponses executeCommand(String[] args) {
        String[] arguments = this.session.getArguments();

        if (arguments[0] != null) {
            // check argument from client and tell client that stream mode is being used
            switch (args[0]) {
                case "A" -> {
                    this.success = SFTPResponses.SUCCESS + "Using Ascii mode";
                    this.session.setStreamType(ServerSession.Types.ASCII);
                    this.response = SFTPResponses.SUCCESS;
                }
                case "B" -> {
                    this.success = SFTPResponses.SUCCESS + "Using Binary mode";
                    this.session.setStreamType(ServerSession.Types.BINARY);
                    this.response = SFTPResponses.SUCCESS;
                }
                case "C" -> {
                    this.success = SFTPResponses.SUCCESS + "Using Continuous mode";
                    this.session.setStreamType(ServerSession.Types.CONTINUOUS);
                    this.response = SFTPResponses.SUCCESS;
                }
                default -> this.response = SFTPResponses.ERR; // argument not valid
            }
        }


        return this.response;
    }
}
