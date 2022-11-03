package Server;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;
import java.util.Properties;

/**
 * A class which is designed to process requests from clients. This class consists of
 * a large switch statement which manages the processing and error handling of requests.
 */
public class EventHandler {

    public final IDBConnection connection;
    public static SessionHandler sessionHandler;

    /**
     * Establishes a new database connection and session for the logged in user.
     * The connection will exist for the duration of the user session.
     *
     * @param props Properties file containing:
     *              jdbc.username - sql username
     *              jdbc.password - sql password
     *              jdbc.ip       - sql server address
     *              jdbc.schema   - sql database name
     *              jdbc.port     - sql server port
     *              serverPort    - server client access port
     * @throws SQLException
     */
    public EventHandler(Properties props) throws SQLException {

        connection = new DBConnection(props);
        sessionHandler = new SessionHandler();

        Runnable reconcilerRunnable = new Reconciler((DBConnection) connection);
        Thread reconcilerThread = new Thread(reconcilerRunnable);
        System.out.println("Starting reconciler");
        reconcilerThread.start();
    }

    /**
     * A constructor which accepts no parameters. This constructor connects to
     * the mock database for unit testing.
     *
     * @throws SQLException
     */
    public EventHandler() throws SQLException {
        connection = new MockDBConnection();
        sessionHandler = new SessionHandler();
    }

    /**
     * Processes requests from Clients.
     *
     * Status codes:
     * 200 – Success
     * 400 – Invalid Request
     * 401 – Unauthorised
     * 403 – Forbidden
     * 404 – Request not found
     * Type:login – Validates users credentials, and if valid returns a session id
     * Inputs:
     * username:String – Username of user
     * password:String – Hashed password (no salt)
     * Outputs:
     * Status:Integer – 200,400 (invalid request), 401 (unauthorised [bad credentials])
     * Error:String – Error description
     * session:String – Session ID generated on successful login
     *
     * All following requests require a valid session id to be provided. Without a valid session, users will receive an error.
     *
     * Type:requestHistory
     * Inputs:
     * session:String – Existing Session ID
     * assetId:Integer – The Asset ID that the history is being requested for
     * Outputs:
     * status:Integer – 200,400 (invalid request), 401 (unauthorised)
     * error:String – Error description
     * history:HashMap(DateTime,Integer) – A HashMap of the date and price for each asset
     *
     * Type:getUnits
     * 	Inputs:
     * 		session:String – Existing Session ID
     * 	Outputs:
     * 		status: Integer – 200,400 (invalid request), 401 (unauthorised)
     * 		error:String – Error description
     * 		Units:HashMap(id,String)  – A hashmap of all unit id’s and corresponding names
     *
     * Type:getAssets
     * 	Inputs:
     * 		session:String – Existing Session ID
     * 	Outputs:
     * 		status: Integer – 200,400 (invalid request), 401 (unauthorised)
     * 		error:String – Error description
     * 		assets:HashMap(id,String)  – A hashmap of all asset id’s and corresponding names
     *
     * Type:addUser
     * 	Inputs:
     * 		session:String – Existing session id
     * 		unitId:Integer – The ID of the users organisational unit
     * 		username:String – The users username
     * 		firstName:String – The users first name
     * 		lastName:String – The users last name
     * 		password:String – sha256 hash of the users password
     * 		administrator:Boolean – whether the user is an administrator
     * 	Output
     * 		status:Integer – 200,400 (invalid request), 401 (unauthorised), 403 (forbidden)
     * 		error:String – error description
     *
     * Type:getCredits
     * 	Inputs:
     * 		session:String – Existing session id
     * 		unitId:Integer – The ID of the organisational unit
     * 	Outputs:
     * 		status:Integer – 200,400 (invalid request), 401 (unauthorised)
     * 		error:String – error description
     * 		credits:Integer – number of Credits
     *
     * Type:setCredits
     * 	Inputs:
     * 		session:String – Existing session id
     * 		unitId:Integer – The ID of the organisational unit
     * 		credits:Integer – Value of Credits to set
     * 	Outputs:
     * 		status:Integer – 200,400 (invalid request), 401 (unauthorised), 403 (forbidden)
     * 		error:String – error description
     *
     *
     * Type:getUserInfo
     * 	Inputs:
     * 		session:String – Existing session id
     * 	Outputs:
     * 		status:Integer – 200,400 (invalid request), 401 (unauthorised)
     * 		error:String – error description
     * 		username:String – Users username
     * 		name:String – Users name
     * 		unit:String – Name of the organisation unit of the user
     * 		unitId:Integer – ID of the organisational unit of the user
     * 		credits:Integer – Amount of credits the unit has
     * 		accountType:String – Admin for administrators, User for standard users
     *
     *
     * Type:addUnit
     * 	Inputs:
     * 		session:String – Existing session id
     * 		unitName:String – Organisational Unit Name
     * 		credits:Integer – Number of Credits
     * 	Outputs:
     * 		status:Integer – 200,400 (invalid request), 401 (unauthorised), 403 (forbidden)
     * 		error:String – error description
     *
     * Type:addAsset
     * 	Inputs:
     * 		session:String – Existing session id
     * 		assetName:String – Asset Name
     * 	Outputs:
     * 		status:Integer – 200,400 (invalid request), 401 (unauthorised), 403 (forbidden)
     * 		error:String – error description
     *
     * Type:addOffer
     * 	Inputs
     * 		session:String – Existing session id
     * 		offerType:String – Buy/Sell
     * 		price:Integer – Cost in credits for this offer
     * 		assetId:Integer – ID of the asset being sold
     * 		unitId:Integer – ID of the organisational unit
     * 		quantity:Integer – Amount of the asset being offered
     * 	Outputs:
     * 		status:Integer – 200,400 (invalid request), 401 (unauthorised)
     * 		error:String – error description
     *
     * Type:updateOUAssets
     * 	Inputs
     * 		session:String – Existing session id
     * 		unitId:Integer – Organisational Unit being updated
     * 		assets:HashMap(Integer, Integer) - HashMap of asset id to quantity
     * 	Outputs:
     * 		status:Integer – 200,400,401
     * 		error:String – error description
     * Type:changePassword
     * 	Inputs
     * 		session:String – Existing session id
     * 		password:String – Hash of password
     * 	Outputs
     * 		status:Integer – 200 (success), 400 (failed)
     * 		error:String – error description
     *
     * Type:getAssets
     * 	Inputs:
     * 		Session:String – Existing Session ID
     * 	Outputs:
     * 		Status: Integer – 200,400 (invalid request), 401 (unauthorised)
     * 		Error:String – Error description
     * 		Assets:HashMap(id,String)  – A hashmap of all asset ids and corresponding names
     *
     * Type:logout
     * 	Inputs:
     * 		session:String – existing  session id
     *
     * 	Outputs:
     * 		status: 200 – this can only return true
     *
     * Type:viewOffers
     * 	Inputs:
     *
     * 		session: String-  existing session id
     * 	Outputs:
     * 		offers: HashMap(Integer(OfferID), HashMap(String, Object)(Offer details))
     * Asset Name:String – Name of asset
     * Quantity:Integer – Amount being sold/bought
     * Price:Integer – Price of offer
     * Type:String – BUY or SELL
     * Organisational Unit:String – Unit Name
     *
     * Type:viewMyOffers
     * 	Inputs:
     *
     * 		session: String-  existing session id
     * 	Outputs:
     * 		offers: HashMap(Integer(OfferID), HashMap(String, Object)(Offer details))
     * Asset Name:String – Name of asset
     * Quantity:Integer – Amount being sold/bought
     * Price:Integer – Price of offer
     * Type:String – BUY or SELL
     * Organisational Unit:String – Unit Name
     *
     * Type: removeOffer
     * 	Inputs:
     * 		session: String – existing session id
     * 		offerId: Integer -  Existing offer id
     *
     * 	Outputs:
     * 		status:Integer - 200, 400
     * 		error:Integer – Error description
     *
     * Type: getUpdateLogs
     * 	Inputs:
     * 		session: String – existing session id
     * 	Outputs:
     * 		status:Integer - 200
     * 		updates:HashMap(LocalDateTime (Time that update was placed), HashMap(String,Object))
     * 			unitName:String – Name of the other unit that completed the purchase
     * 			yourAction:String - SOLD or BOUGHT
     * 			assetName:String – Name of asset sold
     * 			credits:Integer – Final cost of the transaction
     * 			quantity:Integer – Amount of assets purchased
     *
     * Type: getUpdateLogs
     * 	Inputs:
     * 		session: String – existing session id
     * 		unitId:Integer – Organisational Unit to get asset count for
     * 		assetId:Integer – Asset to get count for
     * 	Outputs:
     * 		status:Integer - 200
     * 		assetCount:Integer – amount of asset the unit has
     *
     *
     * Type: viewAssetOffers
     * 	Inputs:
     * 		session:String – existing session id
     * 		assetId:Integer – asset to get active offers for
     * 	Outputs:
     * 		status:Integer - 200
     * 		offers: HashMap(Integer(OfferID), HashMap(String, Object)(Offer details))
     * Asset Name:String – Name of asset
     * Quantity:Integer – Amount being sold/bought
     * Price:Integer – Price of offer
     * Type:String – BUY or SELL
     * Organisational Unit:String – Unit Name
     */
    public HashMap<String,Object> processRequest(HashMap<String,Object> request) throws SQLException {
        //Get request type for Switch statement
        String requestType= (String) request.get("type");
        //Prepare HashMap for putting response data into
        HashMap<String,Object> response = new HashMap<>();

        //Initialise variables used throughout switch case.
        int status;
        String session;
        Integer unitId;
        Integer assetId;
        Integer credits;
        String username;
        String firstName;
        String lastName;
        String password;
        boolean administrator;
        String unitName;
        String assetName;
        HashMap<String,Object> userInfo;
        String existingSession=null;

        //Check if the session is valid if one is provided
        if(request.containsKey("session")){
            existingSession = sessionHandler.validateSession((String) request.get("session"));
        }

        //If the request is a login, process the login
        if(requestType.equals("login")){
            username = (String)request.get("username");
            password = (String)request.get("password");

            boolean passwordValid = connection.checkPassword(username,password);
            if(passwordValid){
                userInfo = connection.getUser(username);
                String sessionId = sessionHandler.newSession(username, (Integer)userInfo.get("unitId"));
                response.put("sessionId",sessionId);
                status=200;
            }else{
                status=400;
                String error = "Invalid credentials provided";
                response.put("error", error);
            }
            //Otherwise, if the session is provided, and its valid, process the request
        }else if(!Objects.isNull(existingSession)){
            username = existingSession;
            userInfo = connection.getUser(username);
            String accountType = (String)userInfo.get("accountType");
            try {
                switch (requestType) {
                    case "requestHistory":
                        assetId = (Integer) request.get("assetId");
                        if(assetId==null||assetId <0) {
                            status=400;
                            response.put("error","Invalid Request: Invalid parameters sent");
                            break;
                        }
                        HashMap<LocalDateTime, Integer> history = connection.getHistory(assetId);

                        response.put("history", history);
                        status = 200;
                        break;

                    case "getUnits":
                        HashMap<Integer, String> units = connection.getOrganisationalUnits();
                        response.put("units", units);
                        status = 200;
                        break;

                    case "getAssets":
                        HashMap<Integer, String> assets = connection.getAssets();
                        response.put("assets", assets);
                        status = 200;
                        break;

                    case "addUser":
                        if(accountType.equals("Admin")){
                            unitId = (Integer) request.get("unitId");
                            username = (String) request.get("username");
                            firstName = (String) request.get("firstName");
                            lastName = (String) request.get("lastName");
                            password = (String) request.get("password");
                            if(unitId==null||unitId<=0||username==null||username.isEmpty()||firstName==null||firstName.isEmpty()||lastName==null||lastName.isEmpty()||password==null||password.isEmpty()||(!connection.getOrganisationalUnits().containsKey(unitId))){
                                status = 400;
                                response.put("error", "Invalid Request: Failed to add user");
                                break;
                            }
                            administrator = (Boolean) request.get("administrator");
                            String role="User";
                            if(administrator){
                                role="Admin";
                            }

                            boolean user = connection.addUser(username, firstName, lastName, password, unitId, role);

                            if (user) {
                                status = 200;
                            } else {
                                status = 400;
                                response.put("error", "Invalid Request: Failed to add user");
                            }
                        }else{
                            status=403;
                            response.put("error", "Forbidden: You are not authorised to commit this action");
                        }
                        break;

                    case "getCredits":
                        unitId = (Integer) request.get("unitId");
                        if(unitId==null||unitId <0) {
                            status=400;
                            response.put("error","Invalid Request: Invalid parameters sent");
                            break;
                        }
                        credits = connection.getCredits(unitId);
                        if(credits!=-1){
                            response.put("credits", credits);
                            status = 200;
                            break;
                        }
                        response.put("error", "Invalid Request: Cannot find credits for unitId");
                        status = 400;
                        break;

                    case "setCredits":
                        if(accountType.equals("Admin")){
                            unitId = (Integer) request.get("unitId");
                            credits = (Integer) request.get("credits");
                            if(unitId==null||unitId <0 || credits==null || credits<0) {
                                status=400;
                                response.put("error","Invalid Request: Invalid parameters sent");
                                break;
                            }
                            boolean creditsAdded = connection.setCredits(unitId, credits);
                            if (creditsAdded) {
                                status = 200;
                            } else {
                                status = 400;
                                response.put("error", "Invalid Request: Failed to set credits. New value is too low or the unit does not exist.");
                            }
                        }else{
                            status=403;
                            response.put("error", "Forbidden: You are not authorised to commit this action");
                        }
                        break;

                    case "getUserInfo":
                        session = (String) request.get("session");
                        // Convert session id to username
                        username = sessionHandler.validateSession(session);
                        response = connection.getUser(username);
                        status = 200;

                        break;

                    case "addUnit":
                        if(accountType.equals("Admin")){
                            unitName = (String) request.get("unitName");
                            credits = (Integer) request.get("credits");
                            if(unitName.isEmpty() || credits==null || credits<0) {
                                status=400;
                                response.put("error","Invalid Request: Invalid parameters sent");
                                break;
                            }
                            if(connection.addOrganisationalUnit(unitName, credits)){
                                status = 200;
                            } else {
                                status = 400;
                                response.put("error", "Invalid Request: Failed to add Organisational Unit");
                            }
                        }else{
                            status=403;
                            response.put("error", "Forbidden: You are not authorised to commit this action");
                        }
                        break;

                    case "addAsset":
                        if(accountType.equals("Admin")){
                            assetName = (String) request.get("assetName");
                            if(assetName.isEmpty()) {
                                status=400;
                                response.put("error","Invalid Request: Invalid parameters sent");
                                break;
                            }
                            if(connection.addAsset(assetName)){
                                status = 200;
                            }
                            else {
                                status = 400;
                                response.put("error", "Invalid Request: Failed to add Asset");
                            }
                        }else{
                            status=403;
                            response.put("error", "Forbidden: You are not authorised to commit this action");
                        }
                        break;

                    case "addOffer":
                        String offerType = (String) request.get("offerType");
                        Integer quantity = (Integer) request.get("quantity");
                        Integer price = (Integer) request.get("price");
                        assetId = (Integer) request.get("assetId");
                        unitId = (Integer) request.get("unitId");
                        if(quantity<=0 || price <=0 || assetId<=0 || unitId<=0){
                            status=400;
                            response.put("error","Invalid Request: Invalid parameters sent");
                            break;
                        }else if(!(offerType.equals("BUY")|| offerType.equals("SELL"))){
                            status=400;
                            response.put("error","Invalid Request: Invalid parameters sent");
                            break;
                        }
                        boolean offer = connection.addOffer(offerType, unitId, assetId, quantity, price);

                        if (offer) {
                            status = 200;
                        } else {
                            status = 400;
                            response.put("error", "Invalid Request: Failed to add Offer");
                        }
                        break;

                    case "updateOUAssets":
                        if(accountType.equals("Admin")){
                            unitId = (Integer) request.get("unitId");
                            if(unitId==null||unitId <0) {
                                status=400;
                                response.put("error","Invalid Request: Invalid parameters sent");
                                break;
                            }
                            @SuppressWarnings("unchecked") HashMap<Integer, Integer> assetsMap = (HashMap<Integer, Integer>) request.get("assets");
                            String errors = connection.updateOUAssets(unitId, assetsMap);
                            if(!errors.equals("")){
                                response.put("error",errors);
                                status=400;
                            }else{
                                status=200;
                            }
                        }else{
                            status=403;
                            response.put("error", "Forbidden: You are not authorised to commit this action");
                        }
                        break;

                    case "changePassword":
                        password = (String) request.get("password");
                        session = (String) request.get("session");
                        username = sessionHandler.validateSession(session);
                        connection.changePassword(password, username);
                        status = 200;

                        break;

                    case "logout":
                        session = (String) request.get("session");
                        sessionHandler.deleteSession(session);
                        status = 200;
                        break;

                    case "viewOffers":
                        HashMap<Integer, HashMap<String,Object>> offers = connection.viewOffers();
                        response.put("offers", offers);
                        status=200;
                        break;

                    case "viewMyOffers":
                        session = (String) request.get("session");
                        username = sessionHandler.validateSession(session);
                        userInfo = connection.getUser(username);
                        unitId = (int) userInfo.get("unitId");
                        if(unitId==null||unitId <0) {
                            status=400;
                            response.put("error","Invalid Request: Invalid parameters sent");
                            break;
                        }                        HashMap<Integer, HashMap<String,Object>> myOffers = connection.viewOffers(unitId);
                        response.put("offers", myOffers);
                        status=200;
                        break;

                    case "removeOffer":
                        session = (String) request.get("session");
                        username = sessionHandler.validateSession(session);
                        userInfo = connection.getUser(username);
                        unitId = (int) userInfo.get("unitId");
                        if(unitId==null||unitId <0) {
                            status=400;
                            response.put("error","Invalid Request: Invalid parameters sent");
                            break;
                        }
                        Integer offerId = (Integer) request.get("offerId");
                        if(connection.removeOffer(offerId, unitId)){
                            status=200;
                        }else{
                            status=400;
                            response.put("error","Invalid Request: That offer is under another unit, or does not exist");
                        }
                        break;

                    case "getUpdateLogs":
                        session = (String) request.get("session");
                        response.put("updates", sessionHandler.getUpdateLog(session));
                        status = 200;
                        break;

                    case "getAssetCount":
                        unitId = (Integer) request.get("unitId");
                        assetId = (Integer) request.get("assetId");
                        if(unitId==null||unitId <0 || assetId==null || assetId<0 || !connection.getAssets().containsKey(assetId) || !connection.getOrganisationalUnits().containsKey(unitId)) {
                            status=400;
                            response.put("error","Invalid Request: Invalid parameters sent");
                            break;
                        }
                        int assetCount = connection.getOUAssetCount(unitId, assetId);
                        response.put("assetCount", assetCount);
                        status = 200;
                        break;

                    case "viewAssetOffers":
                        assetId = (int)request.get("assetId");
                        if(assetId==null||assetId <0) {
                            status=400;
                            response.put("error","Invalid Request: Invalid parameters sent");
                            break;
                        }
                        offers = connection.viewAssetOffers(assetId);
                        response.put("offers",offers);
                        status=200;
                        break;

                    default:
                        String error = "Request not found";
                        status = 404;
                        response.put("error", error);
                        break;
                }
                //Something went wrong somewhere in processing, return an error to the user
            }catch (NullPointerException | ClassCastException | IllegalArgumentException e){
                status=400;
                response.put("error","Invalid Request: Invalid parameters sent");
            }
        }else{
            // if this is not a login request, and the session is invalid, return 401
            status=401;
            response.put("error","Unauthorised: Session is not valid");
        }

        // finally, return the response
        response.put("status",status);
        return response;
    }
}
