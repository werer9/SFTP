package nz.murch.sftp.client;

import nz.murch.sftp.server.Server;
import nz.murch.sftp.server.ServerSession;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {
    private Client client;

    private void login() throws IOException {
        client.user("user1");
        client.account("account1");
        client.password("abc123");
    }

    @BeforeEach
    void setUp() {
        try {
            this.client = new Client();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        this.client.closeSocket();
    }

    @Test
    @DisplayName("RETR")
    void retrieveFile() {
        try {
            this.login();
            Server.generateTestFile();
            Files.delete(Paths.get("client/test.txt"));
            assertTrue(Files.exists(Paths.get("test")));
            assertEquals("-", this.client.retrieveFile("notreal").substring(0, 1));
            this.client.retrieveFile("test.txt");
            assertTrue(Files.exists(Paths.get("client/test.txt")));
            assertEquals("+",
                    this.client.kill("test").substring(0, 1));
            assertFalse(Files.exists(Paths.get("test")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("STOP")
    void stop() {
        try {
            this.login();
            assertNotEquals("-", this.client.request("RETR test/test.txt").substring(0, 1));
            assertEquals("+", this.client.stop().substring(0, 1));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("STOR")
    void storeFile() {
        try {
            this.login();
            Server.generateTestFile();
            Files.delete(Paths.get("client/test.txt"));
            assertTrue(Files.exists(Paths.get("test")));
            assertEquals("-", this.client.storeFile("test2.txt",
                    ServerSession.StoreModes.NEW).substring(0, 1));
            client.storeFile("test2.txt", ServerSession.StoreModes.NEW);
            assertTrue(Files.exists(Paths.get("test/test2.txt")));
            client.storeFile("test2.txt", ServerSession.StoreModes.OLD);
            assertTrue(Files.exists(Paths.get("test/test2.txt")));
            assertFalse(Files.exists(Paths.get("test/test2(0).txt")));
            client.storeFile("test2.txt", ServerSession.StoreModes.APP);
            assertTrue(Files.exists(Paths.get("test/test2.txt")));
            assertFalse(Files.exists(Paths.get("test/test2(0).txt")));
            client.storeFile("test2.txt", ServerSession.StoreModes.NEW);
            assertTrue(Files.exists(Paths.get("test/test2.txt")));
            assertTrue(Files.exists(Paths.get("test/test2(0).txt")));
            assertEquals("+",
                    this.client.kill("test").substring(0, 1));
            assertFalse(Files.exists(Paths.get("test")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("USER")
    void user() {
        try {
            assertEquals("-Invalid user-id, try again\0", this.client.user("user"));
            assertEquals("+User-id valid, send account and password\0", this.client.user("user1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("ACCT")
    void account() {
        try {
            assertEquals("+User-id valid, send account and password\0", this.client.user("user3"));
            assertEquals("-Invalid account, try again\0", this.client.account("account2"));
            assertEquals("! Account valid, logged-in\0", this.client.account("account1"));
            this.client.done();
            this.client.closeSocket();
            this.client = new Client();
            assertEquals("+User-id valid, send account and password\0", this.client.user("user1"));
            assertEquals("+Account valid, send password\0", this.client.account("account1"));
            this.client.closeSocket();
            this.client = new Client();
            assertEquals("+User-id valid, send account and password\0", this.client.user("user2"));
            assertEquals("+Account valid, send password\0", this.client.account("account2"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("PASS")
    void password() {
        try {
            assertEquals("+User-id valid, send account and password\0", this.client.user("user1"));
            assertEquals("+Account valid, send password\0", this.client.account("account1"));
            assertEquals("-Wrong password, try again\0", this.client.password("abc12"));
            assertEquals("! Logged in\0", this.client.password("abc123"));
            this.client.closeSocket();
            this.client = new Client();
            assertEquals("+User-id valid, send account and password\0", this.client.user("user2"));
            assertEquals("+Account valid, send password\0", this.client.account("account2"));
            assertEquals("-Wrong password, try again\0", this.client.password("abc12"));
            assertEquals("! Logged in\0", this.client.password("p4ssw0rd"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("TYPE")
    void type() {
        try {
            this.login();
            assertEquals("+Using Ascii mode\0", this.client.type(ServerSession.Types.ASCII));
            assertEquals("+Using Binary mode\0", this.client.type(ServerSession.Types.BINARY));
            assertEquals("+Using Continuous mode\0", this.client.type(ServerSession.Types.CONTINUOUS));
            assertEquals("-Type not valid\0", this.client.request("TYPE Z"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("KILL")
    void kill() {
        try {
            this.login();
            Server.generateTestFile();
            assertTrue(Files.exists(Paths.get("test")));
            assertEquals("-",
                    this.client.kill("notreal").substring(0, 1));
            assertEquals("+",
                    this.client.kill("test").substring(0, 1));
            assertFalse(Files.exists(Paths.get("test")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("NAME")
    void name() {
        try {
            this.login();
            Server.generateTestFile();
            assertTrue(Files.exists(Paths.get("test")));
            assertEquals("-", this.client.name("fake").substring(0, 1));
            assertEquals("+", this.client.name("test").substring(0, 1));
            assertEquals("+", this.client.toBe("new_test"));
            assertFalse(Files.exists(Paths.get("test")));
            assertEquals("+",
                    this.client.kill("new_test").substring(0, 1));
            assertFalse(Files.exists(Paths.get("new_test")));
            assertFalse(Files.exists(Paths.get("test")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("CDIR")
    void changeDirectory() {
        try {
            this.login();
            Server.generateTestFile();
            assertTrue(Files.exists(Paths.get("test")));
            assertEquals("-", this.client.changeDirectory("fake").substring(0, 1));
            assertEquals("!", this.client.changeDirectory("test").substring(0, 1));
            assertEquals("!", this.client.changeDirectory("..").substring(0, 1));
            assertEquals("+",
                    this.client.kill("test").substring(0, 1));
            assertFalse(Files.exists(Paths.get("test")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("LIST")
    void list() {
        try {
            this.login();
            Server.generateTestFile();
            assertTrue(Files.exists(Paths.get("test")));
            assertEquals("!", this.client.changeDirectory("test").substring(0, 1));
            assertEquals("+", this.client.list("V .").substring(0, 1));
            assertEquals("+", this.client.list("F .").substring(0, 1));
            assertEquals("!", this.client.changeDirectory("..").substring(0, 1));
            assertEquals("+", this.client.list("V .").substring(0, 1));
            assertEquals("+", this.client.list("F .").substring(0, 1));
            assertEquals("+",
                    this.client.kill("test").substring(0, 1));
            assertFalse(Files.exists(Paths.get("test")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @DisplayName("DONE")
    void done() {
        try {
            this.login();
            assertEquals("+localhost Closing connection. Thanks :)\0", this.client.done());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}