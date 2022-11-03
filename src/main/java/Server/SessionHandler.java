package Server;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.time.LocalDateTime;
import java.util.*;

/**
 * A class which deals with the user authentication and ensuring that only valid sessions are able to execute queries.
 */
public class SessionHandler {

    //Contains a list of all active sessions, their organisational unit, and the time of expiry and the username associated with the session
    public Table<String,Integer,String[]> sessions = HashBasedTable.create();
    //A HashMap of all session ids, and any updates that may have occured since their clients last queried the server
    public HashMap<String, HashMap<LocalDateTime, HashMap<String,Object>>> sessionUpdates = new HashMap<>();
    /**
     * Generates a random session ID that expires after 24 hours.
     *
     * @param username  The username of the user to authenticate.
     * @param unitId    The unitId of the user being logged into.
     * @return          Session token.
     */
    public String newSession(String username, Integer unitId) {
        //Generate a new GUID for the session id. This is very very unlikely to ever duplicate
        String guid = UUID.randomUUID().toString();
        //Generate the expiry time. By default a session lasts 24 hours
        LocalDateTime time = LocalDateTime.now().plusHours(24);

        //Construct the array of username and session expiry time
        String[] usernameTime = {username, time.toString()};
        sessions.put(guid,unitId, usernameTime);

        // initialise the updates for this session
        HashMap<LocalDateTime, HashMap<String,Object>> updates = new HashMap<>();
        sessionUpdates.put(guid,updates);

        //Return the session id so the user can authenticate with this going forward.
        return guid;
    }


    /**
     * Validates whether a session ID belongs to a user. If it does, the username of the current
     * user is returned. If not valid, returns null.
     * @param sessionToken  The session token to validate.
     * @return              The username of the session.
     */
    public String validateSession(String sessionToken) {
        try {
            //Get the rows where this session id is being used (should only have one)
            Map<Integer,String[]> session = sessions.row(sessionToken);
            //Check there are results
            if(session.size()>0) {
                //Get the username and expiry time from the first entry
                String[] usernameTime = session.entrySet().iterator().next().getValue();

                String username = usernameTime[0];
                LocalDateTime time = LocalDateTime.parse(usernameTime[1]);

                //Check that the session hasn't expired. If it has, delete the session and return null
                if (time.isBefore(LocalDateTime.now())) {
                    System.out.println("Session has expired "+sessionToken);
                    deleteSession(sessionToken);
                    return null;
                } else {
                    //The session is valid, return the username
                    return username;
                }
            }
            //There were 0 sessions found. Return null
            return null;
        }catch (NullPointerException e) {
            //Something went wrong. Default to deny access in the event someone is trying to break in
            return null;
        }
    }

    /**
     * Removes the specified session token.
     * @param sessionToken  The Session Token to delete.
     */
    public void deleteSession(String sessionToken) {
        //Remove the updates for this token
        sessionUpdates.remove(sessionToken);

        //Get all rows for this session id
        Map<Integer, String[]> unitMap = sessions.row(sessionToken);

        //For each session row, remove the row
        for (Map.Entry<Integer,String[]> entry : unitMap.entrySet()) {
            sessions.remove(sessionToken,entry.getKey());
        }
    }

    /**
     * Adds an update to the updates log for the current session.
     * @param organisationalUnitId  unitId of the users to notify.
     * @param yourAction            Action being performed "SOLD" or "BOUGHT".
     * @param otherUnitName         Name of the other unit that made the purchase.
     * @param assetName             Name of the asset that was purchased.
     * @param credits               Amount of credits that it was purchased for.
     * @param quantity              Quantity purchased.
     */
    public void addUpdateLog(int organisationalUnitId, String yourAction, String otherUnitName, String assetName, int credits, int quantity) {

        // get the sessions in the organisational unit affected
        Map<String,String[]> map = sessions.column(organisationalUnitId);

        // for each session
        for (Map.Entry<String,String[]> entry : map.entrySet()) {
            // get the session id
            String sessionId = entry.getKey();

            // get the update logs associated with this session
            HashMap<LocalDateTime,HashMap<String,Object>> updates = sessionUpdates.get(sessionId);
            // compose a new update log
            HashMap<String,Object> update = new HashMap<>();
            update.put("unitName",otherUnitName);
            update.put("yourAction",yourAction);
            update.put("assetName",assetName);
            update.put("credits",credits);
            update.put("quantity",quantity);

            // put the current update log and the time associated as its identifier
            LocalDateTime time = LocalDateTime.now();
            updates.put(time,update);

            //Replace the old update log
            sessionUpdates.put(sessionId,updates);
        }
    }

    /**
     * Gets the update logs for a session id.
     * @param sessionId Session ID to get the logs for.
     * @return          A HashMap of Purchase DateTime, and updates as HashMap (String, Object).
     */
    public HashMap<LocalDateTime,HashMap<String,Object>> getUpdateLog(String sessionId) {
        //Get all updates
        HashMap<LocalDateTime,HashMap<String,Object>> updates = sessionUpdates.get(sessionId);

        //Construct these into a HashMap
        HashMap<LocalDateTime, HashMap<String,Object>> empty = new HashMap<>();
        sessionUpdates.put(sessionId,empty);
        //Return the HashMap
        return updates;
    }
}