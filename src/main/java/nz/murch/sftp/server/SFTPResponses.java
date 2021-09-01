package nz.murch.sftp.server;

public enum SFTPResponses {
    // response enum to make it easy to change response codes and handle return data
    LOGIN("!"),
    SUCCESS("+"),
    ERR("-");

    private final String name;

    SFTPResponses(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}

