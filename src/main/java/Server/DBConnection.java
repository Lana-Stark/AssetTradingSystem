package Server;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A class containing methods to connect to and interact with the
 * database. This class is used by EventHandler to form SQL queries
 * from the requests.
 */
public class DBConnection implements IDBConnection {

    // prepared statements for writing SQL queries
    private static final String GET_PASSWORD = "SELECT password FROM users WHERE username=?";

    private static final String INSERT_USER = "INSERT INTO users (username, firstName, lastName, password, "
                                                + "unitId, accountType) VALUES (?, ?, ?, ? , ?, ?);";
    private static final String GET_USER = "SELECT Username, firstName, lastName, accountType, o.unitName, o.numCredits, o.unitId, u.accountType " +
            "FROM users u LEFT JOIN organisationalunits o ON u.unitId = o.unitId WHERE username=?";
    private static final String UPDATE_PASSWORD = "UPDATE users SET password=? WHERE username=?";
    private static final String GET_CREDITS = "SELECT numCredits FROM organisationalunits WHERE unitId=?";
    private static final String SET_CREDITS = "UPDATE organisationalunits SET numCredits=? WHERE unitId=?";
    private static final String INSERT_ORGANISATIONAL_UNIT = "INSERT INTO organisationalunits (unitName, "
                                                            + "numCredits) VALUES (?, ?);";
    private static final String INSERT_ASSET = "INSERT INTO assets (assetName) VALUES (?);";
    private static final String GET_HISTORY = "SELECT date, price FROM offers WHERE assetId = ? AND status='Completed' AND removed='N' AND offerType='BUY'";
    private static final String INSERT_OFFER = "INSERT INTO offers (offerType, unitId, assetId, assetQuantity, price) VALUES (?, ?, ?, ?, ?);";
    private static final String VIEW_OFFERS = "SELECT o.offerId, a.assetName, o.assetQuantity, o.price, o.offerType, ou.unitName FROM offers o LEFT JOIN assets a ON o.assetId = a.assetId LEFT JOIN organisationalunits ou ON o.unitId=ou.unitId WHERE status='Pending' and removed='N'";
    private static final String VIEW_OU_OFFERS = "SELECT o.offerId, a.assetName, o.assetQuantity, o.price, o.offerType, ou.unitName FROM offers o LEFT JOIN assets a ON o.assetId = a.assetId LEFT JOIN organisationalunits ou ON o.unitId=ou.unitId WHERE status='Pending' and removed='N' and o.unitId=?";
    private static final String VIEW_ASSET_OFFERS = "SELECT o.offerId, a.assetName, o.assetQuantity, o.price, o.offerType, ou.unitName FROM offers o LEFT JOIN assets a ON o.assetId = a.assetId LEFT JOIN organisationalunits ou ON o.unitId=ou.unitId WHERE status='Pending' and removed='N' and o.assetId=?";
    private static final String REMOVE_OFFER = "UPDATE offers SET removed=1 WHERE offerId=?";
    private static final String GET_OUS = "SELECT unitId, unitName from organisationalunits";
    private static final String GET_ASSETS = "SELECT assetId, assetName FROM assets";

    private static final String SELL_OFFERS = "SELECT * FROM offers WHERE offerType='SELL' AND removed='N' AND status='Pending'";

    private static final String RELATED_BUY_OFFERS = "SELECT * FROM offers WHERE offerType='BUY' AND removed='N' AND status='Pending' AND assetId=? AND assetQuantity<=? AND price>=?";
    private static final String UPDATE_SELL_OFFER = "UPDATE offers SET assetQuantity=?, status=? WHERE offerId=?";
    private static final String UPDATE_BUY_OFFER = "UPDATE offers SET status=? WHERE offerId=?";

    private static final String UPDATE_OU_ASSETS = "REPLACE into organisationalunit_assets (unitId, assetId, count) values (?, ?, ?)";
    private static final String GET_OU_ASSET_COUNT = "SELECT Count from organisationalunit_assets where unitId=? AND assetId=?";

    public static final String GET_SPENDABLE_CREDITS = "SELECT COALESCE(numCredits-SUM(price*assetQuantity), numCredits) FROM offers o INNER JOIN organisationalunits ou ON ou.unitId = o.unitId WHERE offerType='BUY' AND o.unitId=? AND STATUS='Pending' AND removed='N'";

    public static final String GET_CURRENT_BUY_OFFER_CREDITS = "SELECT SUM(price*assetQuantity) FROM offers WHERE offerType='BUY' AND unitId=? AND STATUS='Pending' AND removed='N'";

    private static final String GET_OFFER = "SELECT offerId, offerType, unitId, assetId, assetQuantity, date, price, status, removed FROM offers WHERE offerId=?";

    private Connection connection=null;

    /**
     * Constructor which initialises the database connection to MariaDB.
     *
     * @param props properties file for SQL connection
     * @throws SQLException
     */
    public DBConnection(Properties props) throws SQLException {
            // specify the data source, username and password
            String ip = props.getProperty("jdbc.ip");
            String port = props.getProperty("jdbc.port");
            String username = props.getProperty("jdbc.username");
            String password = props.getProperty("jdbc.password");
            String schema = props.getProperty("jdbc.schema");

            // get a connection
            System.out.println("Attempting database connection...");
            connection = DriverManager.getConnection("jdbc:mariadb://"+ip + ":"+port+ "/" + schema, username,
                password);
            System.out.println("Connected to database");
    }

    /**
     * Closes the database connection.
     */
    public void close() {
        System.out.println("Closing database connection");
        try {
            connection.close();
            System.out.println("Closed");
        } catch (SQLException e) {
            System.out.println("Connection was already closed");
        }
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
        String dbPassword;
        PreparedStatement getPassword;
        try {
            //Get the stored password hash from the database
            getPassword = connection.prepareStatement(GET_PASSWORD);
            getPassword.setString(1, username);

            ResultSet result = getPassword.executeQuery();
            if(result.next()) {
                dbPassword = result.getString(1);
            } else return false;
            //Check if the password hash matches the one supplied
            return password.equals(dbPassword);
        } catch (SQLException e) {
            return false;
        }
    }

    /* * * * * * * * * * * * * * * * * * * * users table methods * * * * * * * * * * * * * * * * * * * */

    /**
     * Inserts a new user to the users table with the provided details.
     *
     * @param username              the username of the new user
     * @param firstName             the user's first name
     * @param lastName              the user's last name
     * @param password              a password for login authentication
     * @param unitId                the organisational unit of the user
     * @param administrator         "Admin" for administrator, "User" for user
     */
    public boolean addUser(String username, String firstName, String lastName, String password, Integer unitId, String administrator) {
        try {
            PreparedStatement addUser = connection.prepareStatement(INSERT_USER);

            // filling the prepared statement
            addUser.setString(1, username);
            addUser.setString(2, firstName);
            addUser.setString(3, lastName);
            addUser.setString(4, password);
            addUser.setInt(5, unitId);
            addUser.setString(6, administrator);
            addUser.executeUpdate(); // execute update as no data is being retrieved
            return true;

        } catch (SQLException e) {
            //Something went wrong, probably a duplicate user. Error out
            return false;
        }
    }

    /**
     * Retrieves all the details of a user based on their username.
     *
     * @param username  the username of the user to search for
     * @return          the details of the user as a HashMap
     */
    public HashMap<String,Object> getUser(String username) {
        HashMap<String,Object> user = new HashMap<>();
        try {
            PreparedStatement getUser = connection.prepareStatement(GET_USER);

            // send query requesting user by username
            getUser.setString(1, username);

            // data returned as a ResultSet
            ResultSet rs = getUser.executeQuery();

            // if a person with the username does not exist, return null
            if (!rs.next()) {
                user.put("username", null);
                return user;
            }

            // otherwise, construct a new user with the details from the row
            // will need to handle incoming JSON here
            user.put("username",rs.getString(1));
            //Users name
            String fullName = rs.getString(2)+" "+rs.getString(3);
            user.put("name",fullName);
            //Unit Name
            user.put("unit",rs.getString(5));
            //Credits of unit
            user.put("credits",rs.getInt(6));
            user.put("unitId",rs.getInt(7));
            user.put("accountType",rs.getString(8));
            return user;

        } catch (SQLException e) {
            //Something went wrong, assume user does not exist
            user.put("username", null);
            return user;
        }
    }

    /**
     * Changes a users' password.
     *
     * @param newPassword   the new password for the user
     * @param username      the username to change for
     * @return              true or false to indicate whether or not the password was successfully changed
     */
    public boolean changePassword(String newPassword, String username) {
        try {
            //Fill in the prepared statement
            PreparedStatement changePassword = connection.prepareStatement(UPDATE_PASSWORD);

            changePassword.setString(1, newPassword);
            changePassword.setString(2, username);

            //execute update
            changePassword.executeUpdate();
            //if all went well, return true
            return true;

        } catch (SQLException e) {
            //Something went wrong, return false
            return false;
        }
    }

    /* * * * * * * * * * * * * * * * * * * * organisationalunits table methods * * * * * * * * * * * * * * * * * * * */

    /**
     * Retrieves the amount of credits an organisational unit currently has.
     *
     * @param unitId    the unitID of the organisational unit to search for
     * @return          the current credit balance of the unit
     */
    public int getCredits(int unitId) {
        try {
            //Fill out the prepared statement
            PreparedStatement getCredits = connection.prepareStatement(GET_CREDITS);

            getCredits.setInt(1, unitId);

            //Initialise to -1 and assume something didnt work by default
            int credits=-1;
            ResultSet rs = getCredits.executeQuery();
            //If we find the unit supplied, set credits to whatever it is
            if (rs.next()) {
                credits=rs.getInt(1);
            }
            return credits;
        } catch (SQLException e) {
            //Something went wrong. Return -1 to indicate cannot find unit
            return -1;
        }
    }


    /**
     * Gets credits associated with current buy offers for a unit.
     *
     * @param unitId    unitID to check credits for
     * @return          amount of credits currently associated with active buy offers
     */
    public int getCurrentBuyOfferCredits(int unitId) {
        try {
            //Get all current open buy offers for the unit
            PreparedStatement getCurrentBuyOfferCredits = connection.prepareStatement(GET_CURRENT_BUY_OFFER_CREDITS);

            getCurrentBuyOfferCredits.setInt(1, unitId);

            int credits=0;
            //Get result from SQL (precompiled result)
            ResultSet rs = getCurrentBuyOfferCredits.executeQuery();
            if (rs.next()) {
                credits=rs.getInt(1);
            }
            //Return to user
            return credits;
        } catch (SQLException e) {
            //Something went wrong, return -1 to indicate error
            return -1;
        }
    }

    /**
     * Sets a specified amount of credits to the Organisational Unit.
     *
     * @param unitId        the unitID of the organisational unit to update
     * @param numCredits    the new amount of credits to set
     * @return              true if the value was updated, false if the unit does not exist or amount was invalid
     */
    public boolean setCredits(int unitId, int numCredits) {
        //Check if the unit has enough credits
        if(numCredits < getCurrentBuyOfferCredits(unitId)) {
            // error, new amount will place into bankruptcy. Please cancel trades
            return false;
        }
        try {
            //Prepare update statement
            PreparedStatement setCredits = connection.prepareStatement(SET_CREDITS);

            setCredits.setInt(1, numCredits);
            setCredits.setInt(2, unitId);

            setCredits.executeUpdate();
            return true;

        } catch (SQLException e) {
            //Something went wrong, return false
            return false;
        }
    }

    /**
     * Inserts an organisational unit to the relevant database table.
     *
     * @param unitName              the name of the unit to be added
     * @param credits               the starting number of credits for the unit
     * @return                      true or false to indicate whether the addition was successful
     */
    public boolean addOrganisationalUnit(String unitName, Integer credits) {
        try {
            if(getOrganisationalUnits().containsValue(unitName)) {
                //Unit already exists
                return false;
            }
            //Prepare addition statement
            PreparedStatement addOrganisationalUnit = connection.prepareStatement(INSERT_ORGANISATIONAL_UNIT);

            addOrganisationalUnit.setString(1, unitName);
            addOrganisationalUnit.setInt(2, credits);

            addOrganisationalUnit.executeUpdate(); // execute update as no data is being retrieved

            return true;

        } catch (SQLException e) {
            //Something went wrong, return false
            return false;
        }
    }

    /**
     * Gets a list of OrganisationalUnits and their IDs.
     *
     * @return  HashMap of (unitId, unitName)
     */
    public HashMap<Integer,String> getOrganisationalUnits() {
        HashMap<Integer,String> units = new HashMap<>();

        //Get the list of organisational units
        PreparedStatement getOUs;
        try {
            getOUs = connection.prepareStatement(GET_OUS);

            // data returned as a ResultSet
            ResultSet rs = getOUs.executeQuery();

            // if a person with the username does not exist, return null
            while (rs.next()) {
                units.put(rs.getInt(1), rs.getString(2));
            }
        } catch (SQLException e) {

        }
        //return the list
        return units;
    }


    /* * * * * * * * * * * * * * * * * * * * assets table methods * * * * * * * * * * * * * * * * * * * */

    /**
     * Gets a list of all assets.
     *
     * @return  HashMap of (assetID, assetName)
     */
    public HashMap<Integer,String> getAssets() {
        HashMap<Integer,String> assets = new HashMap<>();
        PreparedStatement getAssets;
        //Get the list of all assets
        try {
            getAssets = connection.prepareStatement(GET_ASSETS);
            ResultSet rs = getAssets.executeQuery();
            while(rs.next()){
                Integer assetId = rs.getInt(1);
                String assetName = rs.getString(2);

                assets.put(assetId,assetName);
            }
        } catch (SQLException e) { }

        //return the list of assets
        return assets;
    }

    /**
     * Gets the number of an asset that an organisational unit currently has.
     *
     * @param unitId            the ID of the unit
     * @param assetId           the ID of the asset to count
     * @return                  how many items of that asset that the unit has
     */
    public Integer getOUAssetCount(Integer unitId, Integer assetId) {
        try {
            //Prepare query to get count of asset for unit
            PreparedStatement getOUAssetCount = connection.prepareStatement(GET_OU_ASSET_COUNT);
            getOUAssetCount.setInt(1, unitId);
            getOUAssetCount.setInt(2, assetId);

            int count=0;
            ResultSet rs = getOUAssetCount.executeQuery();
            //Get the result and return it
            if (rs.next()) {
                count=rs.getInt(1);
            }
            return count;
        } catch (SQLException e) {
            //Something went wrong, assume they have 0
            return 0;
        }
    }

    /**
     * Creates a new asset.
     *
     * @param assetName  the name of the new asset to add
     * @return           true or false to indicate whether the addition was successful
     */
    public Boolean addAsset(String assetName) {
        try {
            //Check if asset already exists
            if(getAssets().containsValue(assetName)) {
                return false;
            }
            //Prepare query to add asset
             PreparedStatement addAsset = connection.prepareStatement(INSERT_ASSET);

            addAsset.setString(1, assetName);

            addAsset.executeUpdate();
            //Return asset id
            return true;

        } catch (SQLException e) {
            //Something went wrong
            return false;
        }
    }

    /**
     * Calculates the history of sales of an item.
     *
     * @param assetId   the ID of the asset
     * @return          a map of (date, price) for this particular asset
     */
    public HashMap<LocalDateTime,Integer> getHistory(Integer assetId) {
        HashMap<LocalDateTime,Integer> history = new HashMap<>();
        //Get history from SQL - date = sell date, Integer = cost in credits
        PreparedStatement getHistory;
        try {
            getHistory = connection.prepareStatement(GET_HISTORY);
            getHistory.setInt(1,assetId);

            // data returned as a ResultSet
            ResultSet rs = getHistory.executeQuery();

            // if a person with the username does not exist, return null
            while (rs.next()) {
                history.put(rs.getTimestamp(1).toLocalDateTime(), rs.getInt(2));
            }
        } catch (SQLException e) {
            //Something went wrong
        }
        return history;
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
        try {
            //If this is a sell offer, prepare check if there are enough to sell
            if(offerType.equals("SELL")) {
                PreparedStatement getOUAssetCount = connection.prepareStatement(GET_OU_ASSET_COUNT);
                getOUAssetCount.setInt(1, unitId);
                getOUAssetCount.setInt(2, assetId);
                int count=0;
                ResultSet rs = getOUAssetCount.executeQuery();
                if (rs.next()) {
                    count=rs.getInt(1);
                }

                if(count<assetQuantity) {
                    //You do not have enough assets to buy this
                    return false;
                }
            } else if(offerType.equals("BUY")) {
                //If this is a buy offer, check you have enough spendable credits to buy this
                PreparedStatement getSpendableCredits = connection.prepareStatement(GET_SPENDABLE_CREDITS);
                getSpendableCredits.setInt(1,unitId);
                int spendableCredits=0;
                ResultSet rs = getSpendableCredits.executeQuery();
                if (rs.next()) {
                    spendableCredits=rs.getInt(1);
                }

                if(spendableCredits<assetQuantity*price) {
                    //You do not have enough credits to buy this
                    return false;
                }
            }

            //If it made it this far, there are enough spendable credits and enough assets
            //Prepare the offer insert
            PreparedStatement offer = connection.prepareStatement(INSERT_OFFER);
            offer.setString(1, offerType);
            offer.setInt(2, unitId);
            offer.setInt(3, assetId);
            offer.setInt(4, assetQuantity);
            offer.setInt(5, price);

            offer.executeUpdate();
            return true;


        } catch (SQLException e) {
            //Something went wrong, return false
            return false;
        }
    }

    // view all current BUY and SELL offers
    // currently very similar to the method below

    /**
     * Shows all active offers.
     *
     * @return  an (Integer, HashMap) HashMap of offers in the form of (offerId, offerDetails)
     */
    public HashMap<Integer, HashMap<String,Object>> viewOffers() {
        try {
            //Prepare query to view offers
            HashMap<Integer, HashMap<String, Object>> offers = new HashMap<>();
            PreparedStatement viewOffers = connection.prepareStatement(VIEW_OFFERS);
            // no parameters as it is a SELECT * statement with no filtering

            // data returned as a ResultSet
            ResultSet rs = viewOffers.executeQuery();

            // construct an offer with the details from the row
            while (rs.next()) {
                HashMap<String,Object> offer = new HashMap<>();

                offer.put("Asset Name", rs.getString(2));
                offer.put("Quantity", rs.getInt(3));
                offer.put("Price",rs.getInt(4));
                offer.put("Type",rs.getString(5));
                offer.put("Organisational Unit",rs.getString(6));

                offers.put(rs.getInt(1),offer);
            }

            //Return offers
            return offers;

        } catch (SQLException e) {
            //Something went wrong, return null
            return null;
        }

    }

    /**
     * Shows all active offers for a particular unit.
     *
     * @param unitId    the ID of the unit
     * @return          an (Integer, HashMap) HashMap of offers in the form of (offerId, offerDetails)
     */
    public HashMap<Integer, HashMap<String,Object>> viewOffers(int unitId) {
        try {
            //Prepare the view statement for this unit
            HashMap<Integer, HashMap<String, Object>> offers = new HashMap<>();
            PreparedStatement viewOffers = connection.prepareStatement(VIEW_OU_OFFERS);
            viewOffers.setInt(1,unitId);
            // data returned as a ResultSet
            ResultSet rs = viewOffers.executeQuery();

            // construct an offer with the details from the row
            while (rs.next()) {
                HashMap<String,Object> offer = new HashMap<>();

                offer.put("Asset Name", rs.getString(2));
                offer.put("Quantity", rs.getInt(3));
                offer.put("Price",rs.getInt(4));
                offer.put("Type",rs.getString(5));
                offer.put("Organisational Unit",rs.getString(6));

                offers.put(rs.getInt(1),offer);
            }

            //Return the offers
            return offers;

        } catch (SQLException e) {
            //Something went wrong
            return null;
        }

    }

    /**
     * Gets active offers for a particular asset id.
     *
     * @param assetId   the asset id to get the offers for
     * @return          an (Integer, HashMap) HashMap of offers in the form of (offerId, offerDetails)
     */
    public HashMap<Integer, HashMap<String,Object>> viewAssetOffers(int assetId) {
        try {
            //Prepare statement to view all offers for this asset
            HashMap<Integer, HashMap<String, Object>> offers = new HashMap<>();
            PreparedStatement viewOffers = connection.prepareStatement(VIEW_ASSET_OFFERS);
            viewOffers.setInt(1,assetId);

            // data returned as a ResultSet
            ResultSet rs = viewOffers.executeQuery();

            // construct an offer with the details from the row
            while (rs.next()) {
                HashMap<String,Object> offer = new HashMap<>();

                offer.put("Asset Name", rs.getString(2));
                offer.put("Quantity", rs.getInt(3));
                offer.put("Price",rs.getInt(4));
                offer.put("Type",rs.getString(5));
                offer.put("Organisational Unit",rs.getString(6));

                offers.put(rs.getInt(1),offer);
            }

            //Return the offers
            return offers;

        } catch (SQLException e) {
            //Something went wrong, return null
            return null;
        }

    }

    /**
     * Removes an offer related to an asset by setting the field to 1.
     *
     * @param offerId   the ID of the offer to be removed
     * @param unitId    the ID of the unit owning the offer
     * @return          true or false to indicate whether the removal was successful
     */
    public boolean removeOffer(int offerId, int unitId) {
        try {
            //Prepare the statement to remove the current offer. First, check the offer is for the right unit
            PreparedStatement getOffer = connection.prepareStatement(GET_OFFER);
            getOffer.setInt(1,offerId);
            ResultSet rs = getOffer.executeQuery();
            int offerUnitId=-1;
            if(rs.next()){
                System.out.println(rs);
                offerUnitId = rs.getInt("unitId");
            }
            //If the offer is for the wrong unit, return false. They do not have the correct permissions to  view this asset
            if(offerUnitId!=unitId) return false;

            //it is valid given it made it this far
            PreparedStatement removeOffer = connection.prepareStatement(REMOVE_OFFER);
            removeOffer.setInt(1, offerId);

            removeOffer.executeUpdate();
            //return successful
            return true;

        } catch (SQLException e) {
            //Something went wrong, return false
            return false;
        }
    }


    /**
     * Updates the organisationalunit_assets table to the respective amount of values for each asset.
     *
     * @param unitId    unit to update assets for
     * @param assets    a HashMap (assetId, count) of assets
     * @return          a HashMap containing any errors encountered
     */
    public String updateOUAssets(Integer unitId, HashMap<Integer, Integer> assets) {
        String errors="";
        //Iterate through each asset in the supplied list
        for(Map.Entry<Integer, Integer> asset : assets.entrySet()) {
            Integer assetId = asset.getKey();
            Integer value = asset.getValue();
            //If this is a valid asset id
            if(getAssets().containsKey(assetId)) {
                try{
                    //Check that we're updating to more than 0
                    if(value<0){
                        throw new IllegalArgumentException("Amount must be greater or equal to 0");
                    }
                    //Prepare update statement
                    PreparedStatement updateOUAssets = connection.prepareStatement(UPDATE_OU_ASSETS);
                    updateOUAssets.setInt(1, unitId);
                    updateOUAssets.setInt(2,assetId);
                    updateOUAssets.setInt(3,value);
                    updateOUAssets.executeUpdate();
                } catch (SQLException | IllegalArgumentException e) {
                    //Something went wrong, provide an error
                    errors = errors.concat("Unable to update unitId "+unitId+", assetId "+assetId+", quantity "+value+"\r\n");
                }
            } else {
                //Cannot find the asset, provide an error
                errors = errors.concat("Asset does not exist: "+assetId+"\r\n");
            }
        }

        //return any errors
        return errors;
    }

    /**
     * Processes periodic reconciliation of all active offers.
     *
     * @return      true or false to indicate whether a reconciliation was able to be made
     */
    public boolean Reconcile() {
        System.out.println("Reconciling Trades");
        PreparedStatement sellOffers;
        try {
            //Check all active sell offers
            sellOffers = connection.prepareStatement(SELL_OFFERS);
            ResultSet offers = sellOffers.executeQuery();
            //foreach active sell offer
            while(offers.next()){
                int sellOfferId = offers.getInt(1);
                int sellUnitId = offers.getInt(3);
                int sellAssetId = offers.getInt(4);
                int sellAssetQuantity = offers.getInt(5);
                int sellPrice = offers.getInt(7);

                //Find for any related buy offers
                PreparedStatement relatedBuyOffers = connection.prepareStatement(RELATED_BUY_OFFERS);
                // send query requesting user by assetId
                relatedBuyOffers.setString(1, String.valueOf(sellAssetId));
                relatedBuyOffers.setString(2, String.valueOf(sellAssetQuantity));
                relatedBuyOffers.setString(3, String.valueOf(sellPrice));

                // data returned as a ResultSet
                ResultSet buyOffers = relatedBuyOffers.executeQuery();

                //For each buy offer
                if(buyOffers.next()) {
                    //Begin preparing statement to update the buy and sell offers
                    PreparedStatement updateSellOffer = connection.prepareStatement(UPDATE_SELL_OFFER);
                    PreparedStatement updateBuyOffer = connection.prepareStatement(UPDATE_BUY_OFFER);

                    int buyOfferId = buyOffers.getInt(1);
                    int buyUnitId = buyOffers.getInt(3);
                    int buyAssetQuantity = buyOffers.getInt(5);

                    // calculate amount left in sellers stock
                    int newSellQuantity = sellAssetQuantity - buyAssetQuantity;

                    // calculate cost of transaction
                    int cost = buyAssetQuantity*sellPrice;

                    //If the selling quantity is now 0, complete the order
                    if(newSellQuantity==0) {
                        // no assets remain, set status to completed
                        updateSellOffer.setInt(1,newSellQuantity);
                        updateSellOffer.setString(2,"Completed");
                        updateSellOffer.setInt(3,sellOfferId);
                    } else {
                        // assets still remain, set status to Pending
                        updateSellOffer.setInt(1,newSellQuantity);
                        updateSellOffer.setString(2,"Pending");
                        updateSellOffer.setInt(3,sellOfferId);
                    }
                    // set BUY offer to completed
                    updateBuyOffer.setString(1,"Completed");
                    updateBuyOffer.setInt(2,buyOfferId);

                    //If the seller isn't the buyer, then do some financial stuff. If they're the same unit then no money needs to be moved nor assets
                    //This saves a whole bunch of headaches by just skipping it, as its not necessary
                    if(sellUnitId!=buyUnitId) {
                        // get seller credits
                        PreparedStatement getSellerCredits = connection.prepareStatement(GET_CREDITS);
                        getSellerCredits.setInt(1, sellUnitId);
                        ResultSet sellerCreditsResult = getSellerCredits.executeQuery();
                        int sellerCredits;
                        if (sellerCreditsResult.next()) {
                            sellerCredits = sellerCreditsResult.getInt(1);
                        } else {
                            return false;
                        }
                        // update seller credits
                        sellerCredits += cost;
                        PreparedStatement setSellerCredits = connection.prepareStatement(SET_CREDITS);
                        setSellerCredits.setInt(1, sellerCredits);
                        setSellerCredits.setInt(2, sellUnitId);

                        // get buyer credits
                        PreparedStatement getBuyerCredits = connection.prepareStatement(GET_CREDITS);
                        getBuyerCredits.setInt(1, buyUnitId);
                        ResultSet buyerCreditsResult = getBuyerCredits.executeQuery();
                        int buyerCredits;
                        if (buyerCreditsResult.next()) {
                            buyerCredits = buyerCreditsResult.getInt(1);
                        } else {
                            return false;
                        }

                        // update buyer credits
                        buyerCredits -= cost;
                        PreparedStatement setBuyerCredits = connection.prepareStatement(SET_CREDITS);
                        setBuyerCredits.setInt(1, buyerCredits);
                        setBuyerCredits.setInt(2, buyUnitId);

                        // get seller asset count
                        PreparedStatement getSellerOUAssetCount = connection.prepareStatement(GET_OU_ASSET_COUNT);
                        getSellerOUAssetCount.setInt(1, sellUnitId);
                        getSellerOUAssetCount.setInt(2, sellAssetId);
                        ResultSet sellerAssetCountResult = getSellerOUAssetCount.executeQuery();
                        int sellerAssetCount;
                        if (!sellerAssetCountResult.next()) {
                            return false;
                        } else {
                            sellerAssetCount = sellerAssetCountResult.getInt(1);
                        }
                        sellerAssetCount -= buyAssetQuantity;
                        // set seller asset count
                        PreparedStatement updateSellerOUAssets = connection.prepareStatement(UPDATE_OU_ASSETS);
                        updateSellerOUAssets.setInt(1, sellUnitId);
                        updateSellerOUAssets.setInt(2, sellAssetId);
                        updateSellerOUAssets.setInt(3, sellerAssetCount);

                        // get buyer asset count
                        PreparedStatement getBuyerOUAssetCount = connection.prepareStatement(GET_OU_ASSET_COUNT);
                        getBuyerOUAssetCount.setInt(1, buyUnitId);
                        getBuyerOUAssetCount.setInt(2, sellAssetId);

                        ResultSet buyerAssetCountResult = getBuyerOUAssetCount.executeQuery();
                        int buyerAssetCount = 0;
                        if (buyerAssetCountResult.next()) {
                            buyerAssetCount = buyerAssetCountResult.getInt(1);
                        }
                        buyerAssetCount += buyAssetQuantity;
                        //Set buyer asset count
                        PreparedStatement updateBuyerOUAssets = connection.prepareStatement(UPDATE_OU_ASSETS);
                        updateBuyerOUAssets.setInt(1, buyUnitId);
                        updateBuyerOUAssets.setInt(2, sellAssetId);
                        updateBuyerOUAssets.setInt(3, buyerAssetCount);

                        //Execute update to seller and buyer asset counts
                        updateSellerOUAssets.executeUpdate();
                        updateBuyerOUAssets.executeUpdate();
                        //Execute updates to seller and buyer credits
                        setSellerCredits.executeUpdate();
                        setBuyerCredits.executeUpdate();
                    }

                    //Prepare the notifications to end users
                    HashMap<Integer,String> units = getOrganisationalUnits();
                    HashMap<Integer,String> assets = getAssets();

                    String buyUnitName = units.get(buyUnitId);
                    String sellUnitName = units.get(sellUnitId);
                    String sellAssetName = assets.get(sellAssetId);

                    //tell the sessionHandler to add update logs
                    Server.EventHandler.sessionHandler.addUpdateLog(sellUnitId,"Sold",buyUnitName,sellAssetName,cost,buyAssetQuantity);
                    Server.EventHandler.sessionHandler.addUpdateLog(buyUnitId,"Purchased",sellUnitName,sellAssetName,cost,buyAssetQuantity);

                    //Update the orders
                    updateBuyOffer.executeUpdate();
                    updateSellOffer.executeUpdate();

                    //As a successful offer was completed, restart so that the latest data can be pulled
                    return true;
                }

            }
        } catch (SQLException e) {
            // unable to reconcile trades
            return false;
        }
        // no valid trades were able to complete, end this function
        return false;
    }
}
