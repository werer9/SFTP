package nz.murch.sftp;

public enum SFTPResponses {
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

