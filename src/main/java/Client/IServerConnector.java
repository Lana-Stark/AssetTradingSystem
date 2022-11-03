package Client;

import java.io.IOException;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Interface for the server connector. This ensures that methods between the real server connector and
 * mock server connector are consistent for unit testing.
 */
public interface IServerConnector {


    HashMap<String, Object> Login_Request(String username, String password) throws NoSuchAlgorithmException, IOException;

    HashMap<String, Object> History_Request(Integer assetID) throws IOException;

    HashMap<String, Object> getUnits_Request() throws  IOException;

    HashMap<String, Object> addUser_Request(Integer unitId, String username, String firstName, String lastName, String password, Boolean administrator) throws IOException, NoSuchAlgorithmException;

    HashMap<String, Object> getCredits_Request(Integer unitId) throws  IOException;
    HashMap<String, Object> getAssetCount_Request(Integer unitId, Integer assetId) throws  IOException;

    HashMap<String, Object> setCredits_Request(Integer unitId, Integer credits) throws  IOException;

    HashMap<String, Object> getUserInfo_Request(String sessionId) throws  IOException;

    HashMap<String, Object> addUnit_Request(String unitName, Integer credits) throws  IOException;

    HashMap<String, Object> addAsset_Request(String assetName) throws  IOException;

    HashMap<String, Object> addOffer_Request(String offerType, Integer quantity, Integer price, Integer assetId, Integer unitId) throws  IOException;

    HashMap<String, Object> updateOUAssets_Request(Integer unitId, HashMap<Integer,Integer> assets) throws  IOException;

    HashMap<String, Object> changePassword_Request(String sessionId, String password) throws IOException, NoSuchAlgorithmException;

    HashMap<String, Object> getAssets_Request()throws IOException;

    HashMap<String, Object> logOut_Request() throws IOException;

    HashMap<String, Object> getUpdateLogs(String sessionId) throws IOException;
}
