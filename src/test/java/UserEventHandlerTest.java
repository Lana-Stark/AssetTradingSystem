import Server.EventHandler;
import org.junit.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A test class for the EventHandler class. This class establishes a
 * connection to the Mock Database and tests actions in EventHandler
 * that all users can perform.
 */
public class UserEventHandlerTest {
    // initialise the eventHandler and sessionId
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

    // login a user before each test to ensure requests can be received
    @BeforeEach
    public void loginSession() throws SQLException {
        // create an eventHandler object with no props
        // connects to the MockDB by default
        eventHandler = new EventHandler();

        // create a login request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","login");
        request.put("username","test");
        request.put("password","12345");

        // process the login request and retrieve the sessionId
        HashMap<String,Object> response = eventHandler.processRequest(request);
        sessionId = (String)response.get("sessionId");
    }

    @Test
    public void testLoginNullUsername() throws SQLException {
        // login request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","login");
        request.put("username",null);
        request.put("password","1234");

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid credentials provided");

        // assertEquals
        assertEquals(expectedResponse,response);
    }
    @Test
    public void testLoginInvalidUsername() throws SQLException {
        // process a login request with a nonexistent user
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","login");
        request.put("username","fakeUser");
        request.put("password","12345");

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid credentials provided");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testLoginIncorrectPassword() throws SQLException {
        // process a login request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","login");
        request.put("username","test");
        request.put("password","12"); // incorrect password

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid credentials provided");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRequestHistory() throws SQLException {
        // request with valid assetId
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","requestHistory");
        request.put("session",sessionId);
        request.put("assetId",1);

        // process request in EventHandler
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response
        HashMap<LocalDateTime,Integer> history = new HashMap<>();
        history.put(LocalDateTime.of(2021, 5, 17, 11, 37, 30), 50);

        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("history",history);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRequestHistoryInvalidAsset() throws SQLException, NumberFormatException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","requestHistory");
        request.put("session",sessionId);
        request.put("assetId",null); // invalid type

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response, invalid parameters
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRequestHistoryNegativeAsset() throws SQLException, NumberFormatException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","requestHistory");
        request.put("session",sessionId);
        request.put("assetId",-1);

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response, invalid parameters
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRequestHistoryNoAsset() throws SQLException, NumberFormatException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","requestHistory");
        request.put("session",sessionId);
        request.put("assetId",100); // non-existent asset

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect empty result
        HashMap<LocalDateTime,Integer> history = new HashMap<>();
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("history",history);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetUnits() throws SQLException {
        // request of "getUnits"
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getUnits");
        request.put("session",sessionId);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response and hashmap of units
        HashMap<Integer,String> units = new HashMap<>();
        units.put(1,"Test Unit");
        units.put(2,"Test Unit2");

        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("units", units);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssets() throws SQLException {
        // request of type "getAssets"
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssets");
        request.put("session",sessionId);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response and (assetId, assetName) map of assets
        HashMap<Integer,String> assets = new HashMap<>();
        assets.put(1,"Test Asset");

        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("assets",assets);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUserDeny() throws SQLException {
        // request "addUser" with user details
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUser");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("username","newUser");
        request.put("firstName","New");
        request.put("lastName","User");
        request.put("password","03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4");
        request.put("administrator",false);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 403 as the logged in user is not an admin
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",403);
        expectedResponse.put("error","Forbidden: You are not authorised to commit this action");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetCredits() throws SQLException {
        // request with valid unitId
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getCredits");
        request.put("session",sessionId);
        request.put("unitId",1);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response and number of credits
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("credits",500);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetCreditsInvalidUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getCredits");
        request.put("session",sessionId);
        request.put("unitId","stringUnitId"); // invalid type

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetCreditsNegativeUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getCredits");
        request.put("session",sessionId);
        request.put("unitId",-1); // negative unitId

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response, invalid parameters
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetCreditsNoUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getCredits");
        request.put("session",sessionId);
        request.put("unitId",10); // non-existent unitId

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response, failed SQL query
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Cannot find credits for unitId");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testSetCreditsDeny() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","setCredits");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("credits",500);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 403, admin only
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",403);
        expectedResponse.put("error","Forbidden: You are not authorised to commit this action");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetUserInfo() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getUserInfo");
        request.put("session",sessionId);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response and user details
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("username","test");
        expectedResponse.put("name","Test User");
        expectedResponse.put("unit","Test Unit");
        expectedResponse.put("unitId",1);
        expectedResponse.put("credits",500);
        expectedResponse.put("accountType","User");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddUnitDeny() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUnit");
        request.put("session",sessionId);
        request.put("unitName","ICT Department");
        request.put("credits",50);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 403, admin only
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",403);
        expectedResponse.put("error","Forbidden: You are not authorised to commit this action");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddAssetDeny() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addAsset");
        request.put("session",sessionId);
        request.put("assetName","CPU Hours");

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response status 403, admin only
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",403);
        expectedResponse.put("error","Forbidden: You are not authorised to commit this action");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddOffer() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addOffer");
        request.put("session",sessionId);
        request.put("offerType","SELL");
        request.put("price",50);
        request.put("assetId",1);
        request.put("unitId",1);
        request.put("quantity",5);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddOfferInvalidType() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addOffer");
        request.put("session",sessionId);
        request.put("offerType","invalidOfferType"); // must be "BUY" or "SELL"
        request.put("price",50);
        request.put("assetId",1);
        request.put("unitId",1);
        request.put("quantity",5);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddOfferInvalidPrice() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addOffer");
        request.put("session",sessionId);
        request.put("offerType","BUY");
        request.put("price",-1); // invalid price
        request.put("assetId",1);
        request.put("unitId",1);
        request.put("quantity",5);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddOfferInvalidAsset() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addOffer");
        request.put("session",sessionId);
        request.put("offerType","BUY");
        request.put("price",10);
        request.put("assetId",-1); // non-existent assetId
        request.put("unitId",1);
        request.put("quantity",5);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddOfferInvalidUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addOffer");
        request.put("session",sessionId);
        request.put("offerType","BUY");
        request.put("price",10);
        request.put("assetId",1);
        request.put("unitId",-1); // non-existent unitId
        request.put("quantity",5);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testAddOfferInvalidQuantity() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addOffer");
        request.put("session",sessionId);
        request.put("offerType","SELL");
        request.put("price",10);
        request.put("assetId",1);
        request.put("unitId",1);
        request.put("quantity",800); // unit does not have enough of the asset

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response, SQL update failure
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Failed to add Offer");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testUpdateOUAssetsDeny() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","updateOUAssets");
        request.put("session",sessionId);
        HashMap<Integer, Integer> assetAdded = new HashMap<>();
        assetAdded.put(1, 100);
        request.put("assets",assetAdded);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect response status 403, admin only
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",403);
        expectedResponse.put("error","Forbidden: You are not authorised to commit this action");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testChangePassword() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","changePassword");
        request.put("session",sessionId);
        request.put("password","newPassword");

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testChangePasswordFail() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","changePassword");
        request.put("session",sessionId);
        request.put("password",123456); // string only

        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect response status 400, SQL failure
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testLogout() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","logout");
        request.put("session",sessionId);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetUpdateLogs() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getUpdateLogs");
        request.put("session",sessionId);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response and hashmap of updates
        HashMap<String,Object> updates = new HashMap<>();

        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("updates",updates);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCount() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssetCount");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("assetId",1);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response and assetCount
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("assetCount",100);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountInvalidUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssetCount");
        request.put("session",sessionId);
        request.put("unitId","stringUnitType"); // wrong type
        request.put("assetId",1);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountNegativeUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssetCount");
        request.put("session",sessionId);
        request.put("unitId",-1); // invalid unitId
        request.put("assetId",1);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountNoUnit() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssetCount");
        request.put("session",sessionId);
        request.put("unitId",100); // non-existent unit
        request.put("assetId",1);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountInvalidAsset() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssetCount");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("assetId","1"); // wrong type

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountNegativeAsset() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssetCount");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("assetId",-1); // invalid
        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testGetAssetCountNoAsset() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssetCount");
        request.put("session",sessionId);
        request.put("unitId",1);
        request.put("assetId",100); // non-existent assetId

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }


    @Test
    public void testViewOffers() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","viewOffers");
        request.put("session",sessionId);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response and all active offer details
        HashMap<String,Object> offer = new HashMap<>(); // individual offer
        offer.put("id",2);
        offer.put("offerType","SELL");
        offer.put("unitId",1);
        offer.put("assetId",1);
        offer.put("assetQuantity",10);
        offer.put("price",10);

        HashMap<Integer, HashMap<String,Object>>  offers = new HashMap<>(); // full response
        offers.put(2,offer);

        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("offers",offers);

        // assertEquals
        assertEquals(expectedResponse,response);
    }


    @Test
    public void testViewMyOffers() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","viewMyOffers");
        request.put("session",sessionId);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response and only offers of user's unit
        HashMap<String,Object> offer = new HashMap<>(); // individual offer
        offer.put("id",2);
        offer.put("offerType","SELL");
        offer.put("unitId",1);
        offer.put("assetId",1);
        offer.put("assetQuantity",10);
        offer.put("price",10);

        HashMap<Integer, HashMap<String,Object>>  offers = new HashMap<>(); // full response
        offers.put(2,offer);

        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("offers",offers);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRemoveOffer() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","removeOffer");
        request.put("session",sessionId);
        request.put("offerId",1);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRemoveOfferInvalidOffer() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","removeOffer");
        request.put("session",sessionId);
        request.put("offerId","1"); // wrong type

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: Invalid parameters sent");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRemoveOfferNegativeOffer() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","removeOffer");
        request.put("session",sessionId);
        request.put("offerId",-1); // invalid

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: That offer is under another unit, or does not exist");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRemoveOfferNoOffer() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","removeOffer");
        request.put("session",sessionId);
        request.put("offerId",100); // non-existent offerId

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expected response with status 403, offer is from a different unit
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",400);
        expectedResponse.put("error","Invalid Request: That offer is under another unit, or does not exist");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testViewAssetOffers() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","viewAssetOffers");
        request.put("session",sessionId);
        request.put("assetId",1);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect successful response
        HashMap<String,Object> offer = new HashMap<>(); // individual offer
        offer.put("id",2);
        offer.put("offerType","SELL");
        offer.put("unitId",1);
        offer.put("assetId",1);
        offer.put("assetQuantity",10);
        offer.put("price",10);

        HashMap<Integer, HashMap<String,Object>>  offers = new HashMap<>(); // full response
        offers.put(2,offer);

        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",200);
        expectedResponse.put("offers",offers);

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testRequestNotFound() throws SQLException {
        // request
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","randomRequest");
        request.put("session",sessionId);

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response 404
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",404);
        expectedResponse.put("error","Request not found");

        // assertEquals
        assertEquals(expectedResponse,response);
    }

    @Test
    public void testInvalidSession() throws SQLException {
        // request with invalid sessionId
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getUserInfo");
        request.put("session","123456789");

        // process request
        HashMap<String,Object> response = eventHandler.processRequest(request);

        // expect error response 401
        HashMap<String,Object> expectedResponse = new HashMap<>();
        expectedResponse.put("status",401);
        expectedResponse.put("error","Unauthorised: Session is not valid");

        // assertEquals
        assertEquals(expectedResponse,response);
    }
}

