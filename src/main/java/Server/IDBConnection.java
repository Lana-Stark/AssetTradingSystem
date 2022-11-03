package Server;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Interface for the database connection. This ensures that methods between the real database connection and
 * mock database connection are consistent for unit testing.
 */
public interface IDBConnection {

    boolean checkPassword(String username, String password) throws SQLException;

    boolean addUser(String username, String firstName, String lastName, String password, Integer unitId, String administrator);

    HashMap<String,Object> getUser(String username);

    boolean changePassword(String newPassword, String username);

    int getCredits(int unitId);

    int getCurrentBuyOfferCredits(int unitId);

    boolean setCredits(int unitId, int numCredits);

    boolean addOrganisationalUnit(String unitName, Integer credits);

    HashMap<Integer,String> getOrganisationalUnits() throws SQLException;

    HashMap<Integer,String> getAssets() throws SQLException;

    Integer getOUAssetCount(Integer unitId, Integer assetId) throws SQLException;

    Boolean addAsset(String assetName);

    HashMap<LocalDateTime,Integer> getHistory(Integer assetId) throws SQLException;

    boolean addOffer(String offerType, int unitId, int assetId, int assetQuantity, int price);

    HashMap<Integer, HashMap<String,Object>> viewOffers();

    HashMap<Integer, HashMap<String,Object>> viewOffers(int unitId);

    HashMap<Integer, HashMap<String,Object>> viewAssetOffers(int assetId);

    boolean removeOffer(int offerId, int unitId);

    String updateOUAssets(Integer unitId, HashMap<Integer, Integer> assets) throws SQLException;

    boolean Reconcile() throws SQLException;
}
