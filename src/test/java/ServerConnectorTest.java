import Client.IServerConnector;
import Client.MockServerConnector;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test class for the ServerConnector class. A MockServerConnector is used to replace
 * the real ServerConnector. This class tests that a request between the client and server
 * are carried out as expected.
 */
public class ServerConnectorTest {
    // initialise a serverConnector and sessionId
    public IServerConnector serverConnector = null;
    public String sessionId = null;

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

    // login a user before each test to allow requests to be carried out
    @BeforeEach
    public void loginSession() throws IOException, NoSuchAlgorithmException {
        // connect to the MockServerConnector
        serverConnector = new MockServerConnector();

        // create and execute a login request
        HashMap<String,Object> response = serverConnector.Login_Request("test","1234");

        // retrieve sessionId from the response
        sessionId = (String)response.get("sessionId");
    }

    @Test
    public void testIncorrectCredentials() throws IOException, NoSuchAlgorithmException {
        // logout current user
        serverConnector.logOut_Request();

        // login request with incorrect password
        HashMap<String,Object> response = serverConnector.Login_Request("testUser","98765");

        // expected error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid credentials provided");

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testHistory() throws IOException {
        // request
        HashMap<String,Object> response = serverConnector.History_Request(1);

        // expect successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        HashMap<Integer,Integer> history = new HashMap<>(); // manually create expected history map
        expectedResponse.put("history",history);
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetUnits() throws IOException {
        // request
        HashMap<String,Object> response = serverConnector.getUnits_Request();

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        HashMap<Integer,String> units = new HashMap<>();
        units.put(1,"Test Unit");
        expectedResponse.put("units",units);
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUser() throws IOException, NoSuchAlgorithmException {
        // request
        HashMap<String, Object> response = serverConnector.addUser_Request(1,"testNewUser","Test","NewUser","1234",false);

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUserDuplicate() throws IOException, NoSuchAlgorithmException {
        // request but with existing username
        HashMap<String, Object> response = serverConnector.addUser_Request(1,"test","Test","ExistingUser","1234",false);

        // expect a failed response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error", "Failed to add user");

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetCreditsValidUnit() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.getCredits_Request(1);

        // expect a successful response and number of credits
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("credits",50);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetCreditsInvalidUnit() throws IOException {
        // request with a non-existent unitId
        HashMap<String, Object> response = serverConnector.getCredits_Request(100);

        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Cannot find credits for unitId");

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCount() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.getAssetCount_Request(1,1);

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("assetCount",50);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountInvalidUnit() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.getAssetCount_Request(100,1);

        // expect count of 0 as no unit exists
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("assetCount",0);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountInvalidAsset() throws IOException {
        // request for a non-existent assetId
        HashMap<String, Object> response = serverConnector.getAssetCount_Request(1,5);

        // expect a count of 0 as no asset exists
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("assetCount",0);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountNegativeUnit() throws IOException {
        // request for a non-existent assetId
        HashMap<String, Object> response = serverConnector.getAssetCount_Request(-1,5);

        // expect a count of 0 as no unit exists
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("assetCount",0);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountNegativeAsset() throws IOException {
        // request for a negative assetId
        HashMap<String, Object> response = serverConnector.getAssetCount_Request(1,-5);

        // expect a count of 0 as no asset exists
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("assetCount",0);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testSetCredits() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.setCredits_Request(1,60);

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testSetCreditsInvalidUnit() throws IOException {
        // request with non-existent unitId
        HashMap<String, Object> response = serverConnector.setCredits_Request(60,60);

        // expect failed response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error", "Failed to set credits. New value is too low or the unit does not exist.");

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testSetCreditsNegative() throws IOException {
        // request with non-existent unitId
        HashMap<String, Object> response = serverConnector.setCredits_Request(1,-60);

        // expect failed response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error", "Failed to set credits. New value is too low or the unit does not exist.");

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetUserInfo() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.getUserInfo_Request(sessionId);

        // expect a successful response and user details
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("firstName","Test");
        expectedResponse.put("lastName","User");
        expectedResponse.put("password","1234");
        expectedResponse.put("username","test");
        expectedResponse.put("accountType","Admin");
        expectedResponse.put("unitId",1);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetUserInfoInvalidSession() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.getUserInfo_Request("1234");

        // expect a failed response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",401);
        expectedResponse.put("error","Session is not valid");

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUnit() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.addUnit_Request("2nd Test Unit",30);

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddAsset() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.addAsset_Request("2nd Test Asset");

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddOffer() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.addOffer_Request("BUY",30,30,1,1);

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testUpdateOUAssets() throws IOException {
        // new (assetId,quantity) map for change of details
        HashMap<Integer,Integer> OUUpdateAssets = new HashMap<>();
        OUUpdateAssets.put(1,50);
        OUUpdateAssets.put(2,50);

        // request
        HashMap<String, Object> response = serverConnector.updateOUAssets_Request(1,OUUpdateAssets);

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testUpdateOUAssetsInvalidAsset() throws IOException {
        // new (assetId,quantity) map for change of details
        HashMap<Integer,Integer> OUUpdateAssets = new HashMap<>();
        OUUpdateAssets.put(60,50);

        // request
        HashMap<String, Object> response = serverConnector.updateOUAssets_Request(1,OUUpdateAssets);

        // error handling is performed on the GUI
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testUpdateOUAssetsInvalidQuantity() throws IOException {
        // new (assetId,quantity) map for change of details
        HashMap<Integer,Integer> OUUpdateAssets = new HashMap<>();
        OUUpdateAssets.put(1,-50);

        // request
        HashMap<String, Object> response = serverConnector.updateOUAssets_Request(1,OUUpdateAssets);

        // error handling is performed on the GUI
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testChangePassword() throws IOException, NoSuchAlgorithmException {
        // request
        HashMap<String, Object> response = serverConnector.changePassword_Request(sessionId,"4567");

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testChangePasswordInvalidSession() throws IOException, NoSuchAlgorithmException {
        // request
        HashMap<String, Object> response = serverConnector.changePassword_Request("badSessionId","4567");

        // expect a failed response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",401);
        expectedResponse.put("error","Session is not valid");

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssets() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.getAssets_Request();

        // expect a successful response and map of assets
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        HashMap<Integer,String> assets = new HashMap<>(); // (assetId, assetName) map
        assets.put(1, "Test Asset");
        expectedResponse.put("assets",assets);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testLogOut() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.logOut_Request();

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetUpdateLogs() throws IOException {
        // request
        HashMap<String, Object> response = serverConnector.getUpdateLogs(sessionId);

        // full map of all updates
        HashMap<LocalDateTime,Object> updateLogs = new HashMap<>();
        LocalDateTime logTime = (LocalDateTime)((HashMap)response.get("updates")).keySet().toArray()[0];

        // only one existing update in mock, details of action taken
        HashMap<String,Object> updateLog = new HashMap<>();
        updateLog.put("quantity",1);
        updateLog.put("yourAction","sold");
        updateLog.put("unitName","2nd Test Unit");
        updateLog.put("credits",30);
        updateLog.put("assetName","Test Asset");

        updateLogs.put(logTime,updateLog);

        // expect a successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("updates",updateLogs);
        assertEquals(expectedResponse,response);
    }
}
