package nz.murch.sftp.server;

import static org.junit.jupiter.api.Assertions.*;

class ServerConnectionTest {


    @org.junit.jupiter.api.Test
    void getCommandArguments() {
        String[] actual = ServerConnection.getCommandArguments("USER werer9");
        String[] expected = {"werer9"};
        assertArrayEquals(expected, actual);

        actual = ServerConnection.getCommandArguments("ACCT account1 account2");
        expected = new String[]{"account1", "account2"};
        assertArrayEquals(expected, actual);
    }
}