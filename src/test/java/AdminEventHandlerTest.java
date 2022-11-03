import Server.EventHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * A test class for the EventHandler class with admin privileges. This class
 * establishes a connection to the Mock Database and tests actions in EventHandler
 * that only administrators can perform.
 */
public class AdminEventHandlerTest {
    // initialise eventHandler and sessionId
    private EventHandler eventHandler = null;
    private String sessionId = null;

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

    // login an admin user before each test to ensure the correct permissions are enabled
    @BeforeEach
    public void loginSession() throws SQLException {
        // create an eventHandler object with no props
        // connects to the MockDB by default
        eventHandler = new EventHandler();

        // create a login request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","login");
        request.put("username","admin");
        request.put("password","67890");

        // process the login request and retrieve the sessionId
        HashMap<String,Object> response = eventHandler.processRequest(request);
        sessionId = (String)response.get("sessionId");
    }

    @Test
    public void testAddUser() throws SQLException {
        // request "addUser" with appropriate details
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUser");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("username","testNewUser");
        request.put("firstName","Test");
        request.put("lastName","User");
        request.put("password","54321");
        request.put("administrator",false);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 200, successful SQL update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddAdmin() throws SQLException {
        // request "addUser" with admin privileges
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUser");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("username","adminTest");
        request.put("firstName","Test");
        request.put("lastName","Admin");
        request.put("password","09876");
        request.put("administrator",true);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 200, successful SQL update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUserDuplicate() throws SQLException {
        // request "addUser" but details of a user that already exists
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUser");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("username","admin");
        request.put("firstName","Test");
        request.put("lastName","Admin");
        request.put("password","67890");
        request.put("administrator",true);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400 and error message
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to add user");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUserInvalidUnit() throws SQLException {
        // request "addUser"
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUser");
        request.put("session",sessionId);
        request.put("unitId",-1); // invalid
        request.put("username","newUser");
        request.put("firstName","New");
        request.put("lastName","User");
        request.put("password","12345");
        request.put("administrator",false);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400 and error message
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to add user");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUserNoUnit() throws SQLException {
        // request "addUser"
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUser");
        request.put("session",sessionId);
        request.put("unitId",100); // non-existent unit
        request.put("username","newUser");
        request.put("firstName","New");
        request.put("lastName","User");
        request.put("password","12345");
        request.put("administrator",false);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400 and error message
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to add user");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUserNullUsername() throws SQLException {
        // request "addUser"
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUser");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("username",null); // invalid
        request.put("firstName","New");
        request.put("lastName","User");
        request.put("password","12345");
        request.put("administrator",false);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400 and error message
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to add user");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testSetCredits() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","setCredits");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("credits",500);

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 200, admin only
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testSetCreditsNoUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","setCredits");
        request.put("session",sessionId);
        request.put("unitId",100); // non-existent unitId
        request.put("credits",10);

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400, failed SQL update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to set credits. New value is too low or the unit does not exist.");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testSetCreditsTooLow() throws SQLException {
        // add a buy offer
        HashMap<String,Object> newOfferRequest = new HashMap<>();
        newOfferRequest.put("type","addOffer");
        newOfferRequest.put("session",sessionId);
        newOfferRequest.put("offerType","BUY");
        newOfferRequest.put("price",50);
        newOfferRequest.put("assetId",1);
        newOfferRequest.put("unitId",1);
        newOfferRequest.put("quantity",5);

        eventHandler.processRequest(newOfferRequest);

        HashMap<String,Object> request = new HashMap<>();
        request.put("type","setCredits");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("credits",5);

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to set credits. New value is too low or the unit does not exist.");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testSetCreditsNegative() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","setCredits");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("credits",-1); // invalid

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400, failed SQL update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUnit");
        request.put("session",sessionId);
        request.put("unitName","ICT Department");
        request.put("credits",50);

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 200, successful SQL update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUnitDuplicate() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUnit");
        request.put("session",sessionId);
        request.put("unitName","Test Unit");
        request.put("credits",50);

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400, failed SQL update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to add Organisational Unit");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUnitNull() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUnit");
        request.put("session",sessionId);
        request.put("unitName",null);
        request.put("credits",50);

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUnitInvalidCredits() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUnit");
        request.put("session",sessionId);
        request.put("unitName","ICT Department");
        request.put("credits","50"); // wrong type

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUnitNegativeCredits() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUnit");
        request.put("session",sessionId);
        request.put("unitName","ICT Department");
        request.put("credits",-1); // invalid

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddAsset() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addAsset");
        request.put("session",sessionId);
        request.put("assetName","CPU Hours");

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 200, successful SQL update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddAssetDuplicate() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addAsset");
        request.put("session",sessionId);
        request.put("assetName","Test Asset"); // asset already exists

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400, failed SQL update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to add Asset");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddAssetNull() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addAsset");
        request.put("session",sessionId);
        request.put("assetName",null);

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400, invalid parameters
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testUpdateOUAssets() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","updateOUAssets");
        request.put("session",sessionId);

        // create (assetId, quantity) hashmap to complete the request
        HashMap<Integer, Integer> assetAdded = new HashMap<>();
        assetAdded.put(1, 500);
        request.put("assets",assetAdded);
        request.put("unitId",1);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 200, successful update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testUpdateOUAssetsInvalidAsset() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","updateOUAssets");
        request.put("session",sessionId);

        // create (assetId, quantity) hashmap to complete the request
        HashMap<Integer, Integer> assetAdded = new HashMap<>();
        assetAdded.put(100, 500); // assetId does not exist
        request.put("assets",assetAdded);
        request.put("unitId",1);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 400, failed update
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Asset does not exist: 100\r\n");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testUpdateOUAssetsNegativeQuantity() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","updateOUAssets");
        request.put("session",sessionId);

        // create (assetId, quantity) hashmap to complete the request
        HashMap<Integer, Integer> assetAdded = new HashMap<>();
        assetAdded.put(1, -1); // negative quantity
        request.put("assets",assetAdded);

        // process the request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }
}

