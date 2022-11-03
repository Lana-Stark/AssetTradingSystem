
import Server.SessionHandler;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * A test class for SessionHandler. This class tests that session creation, validation
 * and deletion are carried out as expected.
 */
public class SessionHandlerTest {

    public String sessionId;
    public HashMap<String, String[]> Sessions = new HashMap<>();
    public Table<String,Integer,String[]> sessions = HashBasedTable.create();
    public HashMap<String, HashMap<LocalDateTime, HashMap<String,Object>>> sessionUpdates = new HashMap<>();

    private SessionHandler sessionHandler;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setStreams() {
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
    }

    @After
    public void restoreInitialStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @BeforeEach
    public void createNewSession() {
        // create a new session with SessionHandler and retrieve the sessionId of the current user
        sessionHandler = new SessionHandler();
        sessionId = sessionHandler.newSession("user",1);
    }

    @Test
    public void testValidateSession() {
        // validate session with current sessionId
        String response = sessionHandler.validateSession(sessionId);

        // valid session returns current username
        String expectedResponse = "user";

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testValidateSessionInvalidId() {
        // validate with invalid sessionId
        String response = sessionHandler.validateSession("12345");

        // expect null username returned
        String expectedResponse = null;

        assertEquals(expectedResponse, response);
    }

    @Test
    public void testDeleteSession() {
        // delete session with current sessionId
        sessionHandler.deleteSession(sessionId);
        String response = sessionHandler.validateSession(sessionId);

        // successful deletion returns null
        assertEquals(null,response);
    }

    @Test
    public void testDeleteSessionInvalidId() {
        sessionHandler.deleteSession("123456");
        String response = sessionHandler.validateSession(sessionId);

        // failed deletion means the username will still return
        assertEquals("user",response);
    }

    @Test
    public void testGetUpdateLogs() {
        // add an update log to get
        sessionHandler.addUpdateLog(1,"sold","2nd Test Unit", "Test Asset", 30, 1);

        HashMap<LocalDateTime,HashMap<String,Object>> response = sessionHandler.getUpdateLog(sessionId);

        // manually set DateTime as exact datetime cannot be replicated
        LocalDateTime logTime = (LocalDateTime)response.keySet().toArray()[0];

        // expected details of current user's actions
        HashMap<LocalDateTime,HashMap<String,Object>> expectedResponse = new HashMap<>();
        HashMap<String,Object> updateLog = new HashMap<>();
        updateLog.put("quantity",1);
        updateLog.put("yourAction","sold");
        updateLog.put("unitName","2nd Test Unit");
        updateLog.put("credits",30);
        updateLog.put("assetName","Test Asset");

        expectedResponse.put(logTime,updateLog);

        assertEquals(expectedResponse,response);
    }
}
