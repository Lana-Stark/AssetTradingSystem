package Client;

import Server.SessionHandler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MockServerConnector imitates the ServerConnector class for unit testing purposes
 */
public class MockServerConnector implements IServerConnector {

    private ArrayList<HashMap<String, Object>> Users;
    private ArrayList<HashMap<String, Object>> OrganisationUnits;
    private ArrayList<HashMap<String, Object>> Assets;
    private ArrayList<HashMap<String, Object>> Offers;
    private ArrayList<HashMap<String, Object>> OrganisationalUnit_Assets;
    private SessionHandler sessionHandler;

    private int nextUnitId=1;
    private int nextAssetId=1;
    private int nextOfferId=1;

    /**
     * MockServerConnector constructor builds the mock class
     */
    public MockServerConnector() {
        Users = new ArrayList<>();
        OrganisationUnits = new ArrayList<>();
        Assets = new ArrayList<>();
        Offers = new ArrayList<>();
        OrganisationalUnit_Assets = new ArrayList<>();
        sessionHandler = new SessionHandler();

        // add organisational unit details
        try {
            this.addUnit_Request("Test Unit",50);
        } catch (IOException e) { }

        // add user details
        try {
            this.addUser_Request(1,"test","Test","User","1234",true);
        } catch (NoSuchAlgorithmException | IOException e) { }

        // add asset details
        try {
            this.addAsset_Request("Test Asset");
        } catch (IOException e) { }

        HashMap<Integer,Integer> OUUpdateDetails = new HashMap<>();
        OUUpdateDetails.put(1,50);
        try {
            this.updateOUAssets_Request(1, OUUpdateDetails);
        } catch (IOException e) { }

        // add offer details
        HashMap<String,Object> offer = new HashMap<>();
        offer.put("offerId",1);
        offer.put("offerType","BUY");
        offer.put("unitId",1);
        offer.put("assetId",1);
        offer.put("assetQuantity",10);
        offer.put("date",new Timestamp(System.currentTimeMillis()));
        offer.put("price",5);
        offer.put("status","Pending");
        offer.put("removed","N");
        Offers.add(offer);
    }

    /**
     * get current buy offers returns response of querying for buy offer
     * @param unitId the id of the unit to find the buy offer credits
     * @return the response of mock
     */
    public int getCurrentBuyOfferCredits(int unitId) {
        int total=0;
        for (HashMap<String,Object> offer : Offers) {
            if (offer.get("removed").equals("N") && offer.get("status").equals("Completed") && offer.get("offerType").equals("BUY") && (Integer)offer.get("unitId")==unitId) {
                total+=(Integer)offer.get("price");
            }
        }
        return total;
    }

    /**
     * log in request takes username and password and attempts to log a user in
     * @param username username of user
     * @param password password of user
     * @return request response
     * @throws NoSuchAlgorithmException thrown by password hashing
     * @throws IOException general exception
     */
    public HashMap<String,Object> Login_Request(String username, String password) throws NoSuchAlgorithmException, IOException {
        HashMap<String,Object> response = new HashMap<>();
        for (HashMap<String,Object> user : Users) {
            if (user.get("username").equals(username)) {
                int unitId = (Integer)user.get("unitId");
                String sessionId = sessionHandler.newSession(username,unitId);
                response.put("status",200);
                response.put("sessionId",sessionId);
                sessionHandler.addUpdateLog(1,"sold","2nd Test Unit", "Test Asset", 30, 1);
                return response;
            }
        }
        response.put("status",400);
        String error = "Invalid credentials provided";
        response.put("error",error);

        return response;
    }

    /**
     * history request fetches the history of asset with given id
     * @param assetID id of asset
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> History_Request(Integer assetID) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        HashMap<LocalDateTime,Integer> history = new HashMap<>();
        for (HashMap<String,Object> offer : Offers) {
            if (offer.get("assetId").equals(assetID) && offer.get("removed").equals("N") && offer.get("status").equals("Completed") && offer.get("offerType").equals("BUY")) {
                history.put(((Timestamp)offer.get("date")).toLocalDateTime(), (Integer)offer.get("price"));
            }
        }
        response.put("history",history);
        response.put("status",200);
        return response;
    }

    /**
     * get units, fetches all units in database
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> getUnits_Request() throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        HashMap<Integer,String> units = new HashMap<>();
        for (HashMap<String,Object> unit : OrganisationUnits) {
            units.put((Integer)unit.get("unitId"), (String)unit.get("unitName"));
        }
        response.put("units",units);
        response.put("status",200);

        return response;
    }

    /**
     * add a user, adds a user with given parameters to the database
     * @param unitId the unit id of new user
     * @param username the username of new user
     * @param firstName the first name of new user
     * @param lastName the last name of new user
     * @param password the password of new user
     * @param administrator admin value
     * @return request response
     * @throws IOException general exception
     * @throws NoSuchAlgorithmException password hash exception
     */
    public HashMap<String,Object> addUser_Request(Integer unitId, String username, String firstName, String lastName, String password, Boolean administrator) throws IOException, NoSuchAlgorithmException {
        HashMap<String,Object> response = new HashMap<>();
        for (HashMap<String, Object> user : Users) {
            if (user.get("username").equals(username)) {
                response.put("error","Failed to add user");
                response.put("status",400);
                return response;
            }
        }

        // add user details
        HashMap<String,Object> user = new HashMap<>();
        user.put("username",username);
        user.put("firstName",firstName);
        user.put("lastName",lastName);
        user.put("password",password);
        user.put("unitId",unitId);
        if(administrator) {
            user.put("accountType","Admin");
        } else {
            user.put("accountType","User");
        }
        Users.add(user);
        response.put("status",200);
        return response;
    }

    /**
     * get credits, gets the amount of credits for a given unit
     * @param unitId the id of the unit
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> getCredits_Request(Integer unitId) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        for (HashMap<String,Object> unit : OrganisationUnits) {
            if(unit.get("unitId").equals(unitId)) {
                Integer credits = (Integer)unit.get("numCredits");
                response.put("credits",credits);
                response.put("status",200);
                return response;
            }
        }
        response.put("error","Cannot find credits for unitId");
        response.put("status",400);
        return response;
    }

    /**
     * get asset count, fetches the amount of an asset a unit has
     * @param unitId the unit to search
     * @param assetId the id to search
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> getAssetCount_Request(Integer unitId, Integer assetId) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        for (HashMap<String,Object> asset : OrganisationalUnit_Assets) {
            if (asset.get("unitId").equals(unitId) && asset.get("assetId").equals(assetId)) {
                response.put("assetCount",asset.get("Count"));
                response.put("status",200);
                return response;
            }
        }
        // if you cant find it, they have 0
        response.put("assetCount",0);
        response.put("status",200);
        return response;
    }

    /**
     * set credits, update the credit amount for a given unit
     * @param unitId the unit id
     * @param credits the amount of credits to update
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> setCredits_Request(Integer unitId, Integer credits) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        if (unitId != null && credits>=getCurrentBuyOfferCredits(unitId)) {
            for (HashMap<String,Object> unit : OrganisationUnits) {
                if (unit.get("unitId").equals(unitId)) {
                    unit.put("credits",credits);
                    response.put("status",200);
                    return response;
                }
            }
        }
        response.put("status",400);
        response.put("error","Failed to set credits. New value is too low or the unit does not exist.");
        return response;
    }

    /**
     * get user, gets the details of user logged in
     * @param sessionId the session id of current session
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> getUserInfo_Request(String sessionId) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        String username = sessionHandler.validateSession(sessionId);
        if(username != null) {
            for (HashMap<String, Object> user : Users) {
                if (user.get("username").equals(username)) {
                    user.put("status",200);
                    return user;
                }
            }
            response.put("status",400);
            response.put("error","User does not exist");
            return response;
        }
        response.put("status",401);
        response.put("error","Session is not valid");
        return response;
    }

    /**
     * add unit, adds a unit with a given name and amount of credits
     * @param unitName the name of the unit to be added
     * @param credits the amount of credits to give unit
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> addUnit_Request(String unitName, Integer credits) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        HashMap<String,Object> newOrganisationalUnit = new HashMap<>();

        newOrganisationalUnit.put("unitName",unitName);
        newOrganisationalUnit.put("numCredits",credits);
        newOrganisationalUnit.put("unitId",nextUnitId);
        nextUnitId++;
        OrganisationUnits.add(newOrganisationalUnit);

        response.put("status",200);
        return response;
    }

    /**
     * add asset, creates a new asset with a given name
     * @param assetName the name of the new asset
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> addAsset_Request(String assetName) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        HashMap<String,Object> newAsset = new HashMap<>();

        newAsset.put("assetName",assetName);
        newAsset.put("assetId",nextAssetId);
        nextAssetId++;
        Assets.add(newAsset);

        response.put("status",200);
        return response;
    }

    /**
     * add an offer with given parameters
     * @param offerType the type of offer (buy/sell)
     * @param quantity the quantity to sell/buy
     * @param price the price of asset
     * @param assetId the id of asset
     * @param unitId the unit id of seller/buyer
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> addOffer_Request(String offerType, Integer quantity, Integer price, Integer assetId, Integer unitId) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        HashMap<String,Object> newOffer = new HashMap<>();

        newOffer.put("offerId",nextOfferId);
        nextOfferId++;
        newOffer.put("offerType",offerType);
        newOffer.put("unitId",unitId);
        newOffer.put("assetId",assetId);
        newOffer.put("assetQuantity",quantity);
        newOffer.put("date",new Timestamp(System.currentTimeMillis()));
        newOffer.put("price",price);
        newOffer.put("status","Pending");
        newOffer.put("removed","N");
        Offers.add(newOffer);

        response.put("status",200);
        return response;
    }

    /**
     * update ou assets, updates the assets and amount of each a unit has
     * @param unitId the unit id of unit
     * @param assets hashmap of asset id and asset amount
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> updateOUAssets_Request(Integer unitId, HashMap<Integer,Integer> assets) throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        for (Map.Entry<Integer,Integer> asset : assets.entrySet()) {
            Boolean updated=false;
            for (HashMap<String,Object> unitAsset : OrganisationalUnit_Assets) {
                if (unitAsset.get("assetId").equals(unitId) && unitAsset.get("assetId").equals(asset.getKey())) {
                    unitAsset.put("Count", asset.getValue());
                    updated=true;
                }
            }
            if(!updated) {
                HashMap<String,Object> newUnitAsset = new HashMap<>();
                newUnitAsset.put("unitId",unitId);
                newUnitAsset.put("assetId",asset.getKey());
                newUnitAsset.put("Count",asset.getValue());
                OrganisationalUnit_Assets.add(newUnitAsset);
            }
        }
        response.put("status",200);
        return response;
    }

    /**
     * change password, updates the password of a user logged in
     * @param sessionId the id of the current session
     * @param password the new password
     * @return request response
     * @throws IOException general exception
     * @throws NoSuchAlgorithmException hashing exception
     */
    public HashMap<String,Object> changePassword_Request(String sessionId, String password) throws IOException, NoSuchAlgorithmException {
        HashMap<String,Object> response = new HashMap<>();
        String username = sessionHandler.validateSession(sessionId);
        if(username != null) {
            for (HashMap<String,Object> user : Users) {
                if (user.get("username").equals(username)) {
                    user.put("password",password);
                    response.put("status",200);
                    return response;
                }
            }
            response.put("status",400);
            response.put("error","Failed to change password for username: " + username);
            return response;
        }
        response.put("status",401);
        response.put("error","Session is not valid");
        return response;
    }

    /**
     * get assets returns all asset names and ids
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> getAssets_Request() throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        HashMap<Integer,String> assets = new HashMap<>();
        for (HashMap<String, Object> asset : Assets) {
            System.out.println(asset);
            assets.put((Integer)asset.get("assetId"), (String)asset.get("assetName"));
        }
        response.put("assets",assets);
        response.put("status",200);

        return response;
    }

    /**
     * log out, logs the current user out and ends sesssion
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> logOut_Request() throws IOException {
        HashMap<String,Object> response = new HashMap<>();
        response.put("status",200);
        return response;
    }

    /**
     * get updates, gets updates of offers matched
     * @param sessionId the id of current session
     * @return request response
     * @throws IOException general exception
     */
    public HashMap<String,Object> getUpdateLogs(String sessionId) throws IOException {
        HashMap<String,Object> response = new HashMap<>();

        response.put("updates",sessionHandler.getUpdateLog(sessionId));
        response.put("status",200);
        return response;
    }
}
