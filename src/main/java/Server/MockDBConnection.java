package Server;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * A replacement of the real database for testing. This class is used by the
 * EventHandler class to execute requests.
 */
public class MockDBConnection implements IDBConnection {

    private final ArrayList<HashMap<String, Object>> Users;
    private final ArrayList<HashMap<String, Object>> OrganisationUnits;
    private final ArrayList<HashMap<String, Object>> OrganisationalUnitAssets;
    private final ArrayList<HashMap<String, Object>> Assets;
    private final ArrayList<HashMap<String, Object>> Offers;

    // initialise ID's to start at 1
    private int nextUnitId=1;
    private int nextAssetId=1;
    private int nextOfferId=1;

    /**
     * Constructor which initialises the mock database connection.
     */
    public MockDBConnection() throws SQLException {
        // Setup Mock Array Lists
        Users = new ArrayList<>();
        OrganisationUnits = new ArrayList<>();
        OrganisationalUnitAssets = new ArrayList<>();
        Assets = new ArrayList<>();
        Offers = new ArrayList<>();

        // default organisational unit
        this.addOrganisationalUnit("Test Unit", 500);
        this.addOrganisationalUnit("Test Unit2", 500);

        int defaultOUID = (int) OrganisationUnits.get(0).get("id");

        // default user
        this.addUser("test", "Test", "User", "12345", defaultOUID, "User");
        this.addUser("admin", "Test", "Admin", "67890", defaultOUID, "Admin");

        // default asset
        this.addAsset("Test Asset");

        // give assets to default units
        HashMap<Integer, Integer> assetAdded = new HashMap<>();
        assetAdded.put(1, 100);
        assetAdded.put(2, 100);
        this.updateOUAssets(1, assetAdded);
        this.updateOUAssets(2, assetAdded);

        // default offers to create history
        this.addCompletedOffer("BUY",1,1,20,50);
        this.addOffer("SELL", 1, 1, 10, 10);

        System.out.println("Users: " + Users);
        System.out.println("Org Units: " + OrganisationUnits);
        System.out.println("Org Unit Assets: " + OrganisationalUnitAssets);
        System.out.println("Assets: " + Assets);
        System.out.println("Offers: " + Offers);
    }

    /* MOCK DB HELPER METHODS*/

    /**
     * Searches through array list and finds the user by username.
     *
     * @param userName  username to find
     * @return          user object
     */
    private HashMap<String, Object> findUserByUsername(String userName) {
        //Iterate through the users until the correct username is found
        for (HashMap<String, Object> user : Users) {
            if (user.get("username").equals(userName)) {
                return user;
            }
        }

        // return null user if user not found
        HashMap<String, Object> nullUser = new HashMap<>();
        nullUser.put("username", "null");
        return nullUser;
    }

    /**
     * Searches through array list and finds organisationalUnit by ID.
     *
     * @param unitId    unitId to find the organisational unit by
     * @return          unit object as a hashmap
     */
    private HashMap<String, Object> findOrganisationalUnitById(int unitId) {
        //Iterate through the units until the correct unit is found
        for (HashMap<String, Object> organisationalUnit : OrganisationUnits) {
            if ((int)organisationalUnit.get("id") == unitId) {
                return organisationalUnit;
            }
        }

        // return null organisationalUnit if unit not found
        HashMap<String, Object> nullOrganisationalUnit = new HashMap<>();
        nullOrganisationalUnit .put("id", "null");
        return nullOrganisationalUnit;
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        // nothing to implement as this is just a mock.
    }

    /**
     * Helper Method for testing - inserts a completed offer to the table.
     *
     * @param offerType             "BUY" or "SELL" offer
     * @param organisationalUnit    the ID of the organisational unit owning the asset
     * @param assetId               the ID of the asset being offered
     * @param assetQuantity         the quantity of the asset in the offer
     * @param price                 the price the asset is being offered for
     */
    public void addCompletedOffer(String offerType, int organisationalUnit, int assetId, int assetQuantity, int price) {
        //Create a new hashmap for the offer to put in
        HashMap<String, Object> offer = new HashMap<>();
        offer.put("offerType", offerType);
        offer.put("unitId", organisationalUnit);
        offer.put("assetId", assetId);
        offer.put("assetQuantity", assetQuantity);
        offer.put("price", price);
        offer.put("date",LocalDateTime.of(2021, 5, 17, 11, 37, 30));
        offer.put("id", nextOfferId);
        nextOfferId++;
        offer.put("status","Completed");
        offer.put("removed","N");

        Offers.add(offer);
    }

    /* * * * * * * * * * * * * * * * * * * * authentication methods * * * * * * * * * * * * * * * * * * * */

    /**
     * Checks whether a password matches the stored value for a username.
     *
     * @param username  username to authenticate
     * @param password  password to authenticate with
     * @return          true if valid, false if invalid
     */
    public boolean checkPassword(String username, String password) {
        //Find the user
        HashMap<String, Object> User = this.findUserByUsername(username);
        User.get("password");
        // user doesn't exist
        if (User.get("username") == null) {
            return false;
        }

        // check password
        return User.get("password") == password;
    }

    /* * * * * * * * * * * * * * * * * * * * users table methods * * * * * * * * * * * * * * * * * * * */

    /**
     * Inserts a new user to the users table with the provided details.
     *
     * @param username              the username of the new user
     * @param firstName             the user's first name
     * @param lastName              the user's last name
     * @param password              a password for login authentication
     * @param organisationalUnit    the organisational unit ID of the user
     * @param administrator         "Admin" for administrator, "User" for user
     * @return                      true if the addition was successful, false if not
     */
    public boolean addUser(String username, String firstName, String lastName, String password, Integer organisationalUnit, String administrator) {
        //Check if the user exists
        if(getUser(username).get("username").equals("null")) {
            //User doesn't exist, enter their details into the database
            HashMap<String, Object> User = new HashMap<>();
            User.put("username", username);
            User.put("firstname", firstName);
            User.put("lastname", lastName);
            User.put("password", password);
            User.put("unitId", organisationalUnit);
            User.put("accountType", administrator);
            Users.add(User);

            return true;
        }
        //User already exists, return false
        return false;
    }

    /**
     * Retrieves all the details of a user based on their username.
     *
     * @param username  the username of the user to search for
     * @return          the details of the user as a HashMap
     */
    public HashMap<String,Object> getUser(String username) {
        //Find the user details
        HashMap<String, Object> User = this.findUserByUsername(username);
        if (User.get("username") != "null") {
            //If the user exists, get their unit details
            HashMap<String, Object> UserOU = this.findOrganisationalUnitById((int) User.get("unitId"));
            // construct user response object
            HashMap<String, Object> UserResponse = new HashMap<>();
            UserResponse.put("username", User.get("username"));
            UserResponse.put("name", User.get("firstname") + " " + User.get("lastname"));
            UserResponse.put("unit", UserOU.get("name"));
            UserResponse.put("credits", UserOU.get("credits"));
            UserResponse.put("unitId", User.get("unitId"));
            UserResponse.put("accountType", User.get("accountType"));

            return UserResponse;
        }

        // return null user if user not found
        HashMap<String, Object> nullUser = new HashMap<>();
        nullUser.put("username", "null");

        return nullUser;
    }

    /**
     * Changes a users' password.
     *
     * @param newPassword   the new password for the user
     * @param username      the username to change for
     * @return              true or false to indicate whether or not the password was successfully changed
     */
    public boolean changePassword(String newPassword, String username) {
        //Find the user
        HashMap<String, Object> User = this.findUserByUsername(username);

        // should mutate the hashmap in memory
        //If the user exists, set the new password and return true
        if (User.get("username") != null) {
            User.put("password", newPassword);
            return true;
        }

        // can't find user
        return false;
    }

    /* * * * * * * * * * * * * * * * * * * * organisationalunit table methods * * * * * * * * * * * * * * * * * * * */

    /**
     * Retrieves the amount of credits an organisational unit currently has.
     *
     * @param unitId    the unitID of the organisational unit to search for
     * @return          the current credit balance of the unit
     */
    public int getCredits(int unitId) {
        //Find the organisational unit
        HashMap<String, Object> organisationalUnit = this.findOrganisationalUnitById(unitId);

        //If the unit exists, return the credits
        if (organisationalUnit.get("id") != "null") {
            return (int)organisationalUnit.get("credits");
        }
        //Return -1 if not found
        return -1;
    }

    /**
     * Gets credits associated with current buy offers for a unit.
     *
     * @param unitId    unitID to check credits for
     * @return          amount of credits currently associated with active buy offers
     */
    public int getCurrentBuyOfferCredits(int unitId) {
        //Get all offers for the unit
        HashMap<Integer, HashMap<String,Object>> offers = viewOffers(unitId);
        int total=0;
        //If the unit is a buy offer, add to the total credits currently being consumed
        for(Map.Entry<Integer,HashMap<String,Object>> offer : offers.entrySet()){
            if(offer.getValue().get("offerType").equals("BUY")){
                total+=(Integer)offer.getValue().get("price");
            }
        }

        //Return the total amount of credits currently being consumed
        return total;
    }

    /**
     * Sets a specified amount of credits to the Organisational Unit.
     *
     * @param unitId        the unitID of the organisational unit to update
     * @param numCredits    the new amount of credits to set
     * @return              true if the value was updated, false if the unit does not exist or amount was invalid
     */
    public boolean setCredits(int unitId, int numCredits) {
        //Get the unit to set credits for
        HashMap<String, Object> organisationalUnit = this.findOrganisationalUnitById(unitId);

        //If the unit exists, and the number of credits to set wont place the unit into bankruptcy, set the credits
        if (organisationalUnit.get("id") != "null" && numCredits>=getCurrentBuyOfferCredits(unitId)) {
            organisationalUnit.put("credits", numCredits);
            return true;
        }

        //The unit doesn't exist, or the new amount of credits is too low
        return false;
    }

    /**
     * Inserts an organisational unit to the relevant database table.
     *
     * @param organisationalUnit    the name of the unit to be added
     * @param credits               the starting number of credits for the unit
     * @return                      true or false to indicate whether the addition was successful
     */
    public boolean addOrganisationalUnit(String organisationalUnit, Integer credits) {
        HashMap<String, Object> newOrganisationalUnit = new HashMap<>();
        //Check if the organisational unit already exists
        try {
            if(getOrganisationalUnits().containsValue(organisationalUnit)){
                return false;
            }
        } catch (SQLException ignored) { }

        //Proceed with adding the unit
        newOrganisationalUnit.put("name", organisationalUnit);
        newOrganisationalUnit.put("credits", credits);
        newOrganisationalUnit.put("id", nextUnitId);
        nextUnitId++;

        OrganisationUnits.add(newOrganisationalUnit);

        return true;
    }

    /**
     * Gets a list of OrganisationalUnits and their IDs.
     *
     * @return  HashMap of (unitId, unitName)
     * @throws SQLException
     */
    public HashMap<Integer,String> getOrganisationalUnits() throws SQLException {
        HashMap<Integer, String> organisationalUnits = new HashMap<>();

        //Iterate through the database and construct a hashmap of unitId and name
        for (HashMap<String, Object> organisationalUnit : OrganisationUnits) {
            organisationalUnits.put((int)organisationalUnit.get("id"), (String)organisationalUnit.get("name"));
        }

        //return the hashmap
        return organisationalUnits;
    }


    /* * * * * * * * * * * * * * * * * * * * assets table methods * * * * * * * * * * * * * * * * * * * */

    /**
     * Gets a list of all assets.
     *
     * @return  HashMap of (assetID, assetName)
     * @throws SQLException
     */
    public HashMap<Integer,String> getAssets() throws SQLException {
        HashMap<Integer, String> assets = new HashMap<>();

        //Iterate through the assets and construct a hashmap of assetId and assetName
        for (HashMap<String, Object> asset : Assets) {
            assets.put((int)asset.get("id"), (String)asset.get("name"));
        }

        //return hashmap
        return assets;
    }

    /**
     * Gets the number of an asset that an organisational unit currently has.
     *
     * @param unitId            the ID of the unit
     * @param assetId           the ID of the asset to count
     * @return                  how many items of that asset that the unit has
     * @throws SQLException
     */
    public Integer getOUAssetCount(Integer unitId, Integer assetId) throws SQLException {
        //Iterate through the organisational unit asset assignments
        for (HashMap<String, Object> organisationalUnitAsset : OrganisationalUnitAssets) {
            //If the unit is correct and the asset id is correct
            if(organisationalUnitAsset.get("unitId")==unitId && organisationalUnitAsset.get("assetId")==assetId) {
                //return teh count
                return (Integer)organisationalUnitAsset.get("Count");
            }
        }

        //The unit asset combination doesn't exist, which just means that they've never had one of these
        //return that they have 0
        return 0;
    }

    /**
     * Creates a new asset.
     *
     * @param assetName  the name of the new asset to add
     * @return           true or false to indicate whether the addition was successful
     */
    public Boolean addAsset(String assetName) {
        HashMap<String, Object> asset = new HashMap<>();

        //See if the asset already exists
        try {
            if(getAssets().containsValue(assetName)){
                return false;
            }
        } catch (SQLException ignored) { }

        //The asset doesn't already exist, proceed with adding it
        asset.put("name", assetName);
        asset.put("id", nextAssetId);
        nextAssetId++;

        Assets.add(asset);

        return true;
    }

    /**
     * Calculates the history of sales of an item.
     *
     * @param assetId   the ID of the asset
     * @return          a map of (date, price) for this particular asset
     * @throws SQLException
     */
    public HashMap<LocalDateTime,Integer> getHistory(Integer assetId) throws SQLException {

        HashMap<LocalDateTime, Integer> datePrice = new HashMap<>();

        // loop through all trades
        for (HashMap<String, Object> trade : Offers) {
            // check that trade is complete and is the correct asset
            if (assetId == (int)trade.get("id") && trade.get("status").equals("Completed")) {
                datePrice.put((LocalDateTime)trade.get("date"), (int)trade.get("price"));
            }
        }

        //return hashmap of trade time and price
        return datePrice;
    }

    /* * * * * * * * * * * * * * * * * * * * offers table methods * * * * * * * * * * * * * * * * * * * */


    // organisational units can add orders to the offers table
    // new orders will have pending status

    /**
     * Inserts a offer to the table.
     *
     * @param offerType             "BUY" or "SELL" offer
     * @param unitId                the ID of the organisational unit owning the asset
     * @param assetId               the ID of the asset being offered
     * @param assetQuantity         the quantity of the asset in the offer
     * @param price                 the price the asset is being offered for
     */
    public boolean addOffer(String offerType, int unitId, int assetId, int assetQuantity, int price) {
        HashMap<String, Object> offer = new HashMap<>();
        //if you have enough assets to place this offer, and enough credits to place this offer
        try {
            if(assetQuantity>getOUAssetCount(unitId,assetId) || (assetQuantity*price)>getCredits(unitId)){
                return false;
            }
        } catch (SQLException e) {
            return false;
        }
        //add the offer into the database
        offer.put("offerType", offerType);
        offer.put("unitId", unitId);
        offer.put("assetId", assetId);
        offer.put("assetQuantity", assetQuantity);
        offer.put("price", price);
        offer.put("id", nextOfferId);
        nextOfferId++;
        offer.put("status","Pending");
        offer.put("removed","N");
        Offers.add(offer);

        return true;
    }

    /**
     * Shows all active offers.
     *
     * @return  an (Integer, HashMap) HashMap of offers in the form of (offerId, offerDetails)
     */
    public HashMap<Integer, HashMap<String,Object>> viewOffers() {
        HashMap<Integer, HashMap<String,Object>> response = new HashMap<>();

        // search through all offers
        for (HashMap<String, Object> offer : Offers) {
            //If the offer hasnt been completed nor removed
            if (offer.get("status").equals("Pending") && offer.get("removed").equals("N")) {
                HashMap<String,Object> offerDetails = new HashMap<>();
                offerDetails.put("offerType",offer.get("offerType"));
                offerDetails.put("assetId", offer.get("assetId"));
                offerDetails.put("assetQuantity",offer.get("assetQuantity"));
                offerDetails.put("price",offer.get("price"));
                offerDetails.put("unitId",offer.get("unitId"));
                offerDetails.put("id",offer.get("id"));
                response.put((int)offer.get("id"), offerDetails);
            }
        }
        return response;
    }

    /**
     * Shows all existing offers in the offers table for a particular unit.
     *
     * @param unitId    the ID of the organisational unit
     * @return          all rows in the offers table where the offer is active
     */
    public HashMap<Integer, HashMap<String,Object>> viewOffers(int unitId) {
        HashMap<Integer, HashMap<String,Object>> response = new HashMap<>();

        // search through all offers
        for (HashMap<String, Object> offer : Offers) {
            //If the unit id matches, the offer isn't completed, and hasn't been removed
            if ((int)offer.get("unitId") == unitId && offer.get("status").equals("Pending") && offer.get("removed").equals("N")) {
                HashMap<String,Object> offerDetails = new HashMap<>();
                offerDetails.put("offerType",offer.get("offerType"));
                offerDetails.put("assetId", offer.get("assetId"));
                offerDetails.put("assetQuantity",offer.get("assetQuantity"));
                offerDetails.put("price",offer.get("price"));
                offerDetails.put("unitId",offer.get("unitId"));
                offerDetails.put("id",offer.get("id"));
                response.put((int)offer.get("id"), offerDetails);
            }
        }
        //return the constructed details
        return response;
    }

    /**
     * Gets active offers for a particular asset id.
     *
     * @param assetId   the asset id to get the offers for
     * @return          an (Integer, HashMap) HashMap of offers in the form of (offerId, offerDetails)
     */
    public HashMap<Integer, HashMap<String,Object>> viewAssetOffers(int assetId) {
        HashMap<Integer, HashMap<String,Object>> response = new HashMap<>();
        // search through all offers
        for (HashMap<String, Object> offer : Offers) {
            //If the assetId is correct, and the offer has been hasn't been completed or removed or removed
            if ((int)offer.get("assetId") == assetId && offer.get("status").equals("Pending")  && offer.get("removed").equals("N")) {
                HashMap<String,Object> offerDetails = new HashMap<>();
                offerDetails.put("offerType",offer.get("offerType"));
                offerDetails.put("assetId", offer.get("assetId"));
                offerDetails.put("assetQuantity",offer.get("assetQuantity"));
                offerDetails.put("price",offer.get("price"));
                offerDetails.put("unitId",offer.get("unitId"));
                offerDetails.put("id",offer.get("id"));
                response.put((int)offer.get("id"), offerDetails);
            }
        }
        return response;
    }

    /**
     * Removes an offer related to an asset by setting the field to 1.
     *
     * @param offerId   the ID of the offer to be removed
     * @param unitId    the ID of the unit owning the offer
     * @return          true or false to indicate whether the removal was successful
     */
    public boolean removeOffer(int offerId, int unitId) {
        // loop through offers and remove offer with offerId
        for (int i = 0 ; i < Offers.size() ; i++) {
            //If the offer id is correct, remove it and return true
            if ((int) Offers.get(i).get("id") == offerId) {
                Offers.remove(i);
                return true;
            }
        }

        //Couldn't find the offer to remove
        return false;
    }

    /**
     * Updates the organisationalunit_assets table to the respective amount of values for each asset.
     *
     * @param unitId    unit to update assets for
     * @param assets    a HashMap (assetId, count) of assets
     * @return          a HashMap containing any errors encountered
     * @throws SQLException
     */
    public String updateOUAssets(Integer unitId, HashMap<Integer, Integer> assets) throws SQLException {
        String errors="";

        //iterate through all the assets supplied
        for(Map.Entry<Integer,Integer> asset : assets.entrySet()){

            //If the asset id supplied is a real asset id
            if(getAssets().containsKey(asset.getKey())) {
                HashMap<String, Object> addedAsset = new HashMap<>();
                //Check if trying to set to a negative number, error if so
                if(asset.getValue()<0) {
                    throw new IllegalArgumentException("Amount must be greater or equal to 0");
                }

                //Construct the import details
                addedAsset.put("unitId", unitId);
                addedAsset.put("assetId", asset.getKey());
                addedAsset.put("Count", asset.getValue());

                boolean added = false;
                //Check if the asset already exists in the OUAssets database, if it does, update it
                for (int i = 0; i > OrganisationalUnitAssets.size(); i++) {
                    if (OrganisationalUnitAssets.get(i).get("unitId") == unitId && OrganisationalUnitAssets.get(i).get("assetId") == asset.getKey()) {
                        OrganisationalUnitAssets.add(i, addedAsset);
                        added = true;
                    }
                }
                //Otherwise, insert a new record (means theyve never had one of this asset before)
                if (!added) {
                    OrganisationalUnitAssets.add(addedAsset);
                }
                //Couldn't find the asset
            } else {
                errors = errors.concat("Asset does not exist: "+asset.getKey()+"\r\n");
            }
        }

        return errors;
    }

    /**
     * Processes periodic reconciliation of all active offers.
     *
     * @return  false for the MockDB as reconciliation is not tested
     */
    public boolean Reconcile() {
        return false;
    }
}