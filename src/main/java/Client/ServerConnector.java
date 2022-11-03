package Client;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Properties;

/**
 * ServerConnector class establishes a connection to server from client and handles queries.
 */
public class ServerConnector implements IServerConnector {
    private final String serverAddress;
    private final Integer port;

    /**
     * Constructor ServerConnector given ipAddress and port number
     * @param props
     */
    public ServerConnector(Properties props) {
        serverAddress = props.getProperty("serverAddress");
        port = Integer.parseInt(props.getProperty("port"));
    }

    /**
     * Function to query server with any request
     * @param request the request details, type and any parameters
     * @return the response of the request
     * @throws IOException thrown by socket.
     */
    public HashMap<String,Object> queryServer(HashMap<String,Object> request) throws IOException {
        request.put("session",Main.sessionId);
        HashMap<String, Object> response = null;
        try{
            Socket socket = new Socket(serverAddress, port);
            // compose the Object Integer to a stream and send
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(request);
            objectOutputStream.flush();

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            try {
                response = (HashMap<String,Object>) objectInputStream.readObject();
            } catch (ClassNotFoundException e) {
                // invalid response
            }
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();

        }
        catch (IllegalArgumentException | SocketException e){
            JOptionPane.showMessageDialog(Main.jPanel, "Could not log in. Please check your config file.");
        }
        return response;
    }

    /**
     * Login Request
     * @param username the username of user
     * @param password the password of user
     * @return result of request
     * @throws NoSuchAlgorithmException from hash
     * @throws IOException from queryServer
     */
    public HashMap<String,Object> Login_Request(String username, String password) throws NoSuchAlgorithmException, IOException {

        HashMap<String,Object> request = new HashMap<>();
        request.put("type","login");
        request.put("username",username);

        // code borrowed from https://www.baeldung.com/sha-256-hashing-java //
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = messageDigest.digest(password.getBytes());

        StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
        for (byte hash : encodedHash) {
            String hex = Integer.toHexString(0xff & hash);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        String hashedPassword = hexString.toString();
        request.put("password",hashedPassword);

        return queryServer(request);
    }

    /**
     * History Request
     * @param assetID the asset id that history is to be fetched for
     * @return the request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> History_Request(Integer assetID) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","requestHistory");
        request.put("assetId",assetID);

        return queryServer(request);
    }

    /**
     * getUnits returns the units in database
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> getUnits_Request() throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getUnits");

        return queryServer(request);
    }

    /**
     * add User, adds a user to the system
     * @param unitId id of user
     * @param username username of user
     * @param firstName first name of user
     * @param lastName lastname of user
     * @param password password of user
     * @param administrator if they are admin or not
     * @return request response
     * @throws IOException thrown in query server
     * @throws NoSuchAlgorithmException thrown in message digest
     */
    public HashMap<String,Object> addUser_Request(Integer unitId, String username, String firstName, String lastName, String password, Boolean administrator) throws IOException, NoSuchAlgorithmException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type", "addUser");
        request.put("unitId", unitId);
        request.put("username", username);
        request.put("firstName", firstName);
        request.put("lastName", lastName);
        // code borrowed from https://www.baeldung.com/sha-256-hashing-java //
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = messageDigest.digest(password.getBytes());

        StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
        for (byte hash : encodedHash) {
            String hex = Integer.toHexString(0xff & hash);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        String hashedPassword = hexString.toString();
        request.put("password", hashedPassword);
        request.put("administrator", administrator);

        return queryServer(request);
    }

    /**
     * getCredit returns credits for a unit
     * @param unitId unitId of unit to be queried
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> getCredits_Request(Integer unitId) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getCredits");
        request.put("unitId",unitId);

        return queryServer(request);
    }

    /**
     * getAssetCount returns the amount of an asset a unit has
     * @param unitId the unit to search
     * @param assetId the asset to search
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> getAssetCount_Request(Integer unitId, Integer assetId) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssetCount");
        request.put("unitId",unitId);
        request.put("assetId",assetId);

        return queryServer(request);
    }

    /**
     * set credits, changes the amount of credits a unit has
     * @param unitId the id of the unit
     * @param credits the number of credits to update to
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> setCredits_Request(Integer unitId, Integer credits) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","setCredits");
        request.put("unitId",unitId);
        request.put("credits",credits);

        return queryServer(request);
    }

    /**
     * getUserInfo
     * @param sessionId the current session id
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> getUserInfo_Request(String sessionId) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getUserInfo");
        request.put("session",sessionId);

        return queryServer(request);
    }

    /**
     * add unit, adds a new unit to the database
     * @param unitName the name of the unit to add
     * @param credits the amount of credits for the unit to have
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> addUnit_Request(String unitName, Integer credits) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addUnit");
        request.put("unitName",unitName);
        request.put("credits",credits);

        return queryServer(request);
    }

    /**
     * add asset, adds a new asset to the database
     * @param assetName the name of the new asset
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> addAsset_Request(String assetName) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addAsset");
        request.put("assetName",assetName);

        return queryServer(request);
    }

    /**
     * adds an offer to the database
     * @param offerType the type of offer
     * @param quantity the quantity to be sold/bought
     * @param price the price of the asset
     * @param assetId the id of the asset
     * @param unitId the unit of the seller/buyer
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> addOffer_Request(String offerType, Integer quantity, Integer price, Integer assetId, Integer unitId) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","addOffer");
        request.put("offerType",offerType);
        request.put("quantity",quantity);
        request.put("price",price);
        request.put("assetId",assetId);
        request.put("unitId",unitId);

        return queryServer(request);
    }

    /**
     * update ou assets, updates the amount of a particular asset a unit has
     * @param unitId the id of the unit
     * @param assets hashmap of asset id and asset amount
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> updateOUAssets_Request(Integer unitId, HashMap<Integer,Integer> assets) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","updateOUAssets");
        request.put("unitId",unitId);
        request.put("assets",assets);

        return queryServer(request);
    }

    /**
     * change password, changes the password of a user logged in
     * @param sessionId the id of the users current session
     * @param password the new password value
     * @return request response
     * @throws IOException thrown in query server
     * @throws NoSuchAlgorithmException thrown by message digest
     */
    public HashMap<String,Object> changePassword_Request(String sessionId, String password) throws IOException, NoSuchAlgorithmException {
        HashMap<String,Object> request = new HashMap<>();
        // code borrowed from https://www.baeldung.com/sha-256-hashing-java //
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = messageDigest.digest(password.getBytes());

        StringBuilder hexString = new StringBuilder(2 * encodedHash.length);
        for (byte hash : encodedHash) {
            String hex = Integer.toHexString(0xff & hash);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        String hashedPassword = hexString.toString();
        request.put("password",hashedPassword);

        request.put("type","changePassword");
        request.put("session",sessionId);

        return queryServer(request);
    }

    /**
     * get assets, returns all assets in database
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> getAssets_Request() throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getAssets");

        return queryServer(request);
    }

    /**
     * log out request logs a user out
     * @return request response
     * @throws IOException thrown in query server
     */
    public HashMap<String,Object> logOut_Request() throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","logout");

        return queryServer(request);
    }

    /**
     * get update logs, updates of offers matched
     * @param sessionId the current session id
     * @return request response
     * @throws IOException query server
     */
    public HashMap<String,Object> getUpdateLogs(String sessionId) throws IOException {
        HashMap<String,Object> request = new HashMap<>();
        request.put("type","getUpdateLogs");
        request.put("session",sessionId);
        return queryServer(request);
    }
}
