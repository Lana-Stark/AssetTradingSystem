package Client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

/**
 * Client Main implements functions to initialise all the pages and switch between pages.
 * Stores variables such as the current page, authorisation, logged in, session id.
 */

public class Main extends JFrame implements Runnable{

    static JFrame jFrame;
    static CardLayout cards;
    private static ServerConnector serverConnector;
    public static JPanel jPanel;
    public static JPanel jUserPanel;
    // variable to change the display for authorised vs non authorised users
    public static boolean authorised = false;
    // variable to change the asset on Asset History page
    public static Integer assetHistoryToDisplay;
    // variable to get name of current card
    public static String cardName;
    public static boolean loggedIn;
    public static String sessionId;

    static boolean fileException;

    // initialise the page displayed

    private static Page_Login login;
    private static JPanel panel_Login;

    private static Page_GetFile getFile;
    private static JPanel panel_getFile;

    /**
     *
     * @param panelName the name of the page to switch to.
     * @return the jPanel of the card that will be changed to.
     * @throws IOException throw in creation of some pages.
     */

    private static JPanel createPanels(String panelName) throws IOException {
        //large switch statement to choose which card to return and create.
        switch (panelName) {
            case "Add User":
                Page_AddUser addUser = new Page_AddUser(serverConnector);
                return addUser.create();

            case "Reset Password":
                Page_ResetPassword resetPassword = new Page_ResetPassword(serverConnector);
                return resetPassword.create();

            case "Manage Units":
                Page_ManageUnit manageUnits = new Page_ManageUnit(serverConnector);
                return manageUnits.create();

            case "Main":
                Page_Main main = new Page_Main(serverConnector);
                return main.create();

            case "Add Unit":
                Page_AddOrganisationalUnit addOrganisationalUnit = new Page_AddOrganisationalUnit(serverConnector);
                return addOrganisationalUnit.create();

            case "Add Asset":
                Page_AddAsset addAsset = new Page_AddAsset(serverConnector);
                return addAsset.create();

            case "View Assets":
                Page_ViewAssets viewAssets = new Page_ViewAssets(serverConnector);
                return viewAssets.create();

            case "View Active Offers":
                Page_ViewActiveOffers viewActiveOffers = new Page_ViewActiveOffers(serverConnector);
                return viewActiveOffers.create();

            case "New Offer":
                Page_NewOffer newOffer = new Page_NewOffer(serverConnector);
                return newOffer.create();

            case "Asset History":
                Page_AssetHistory assetHistory = new Page_AssetHistory(serverConnector);
                return assetHistory.create();

            case "User Border":
                Panel_UserBorder userBorder = new Panel_UserBorder(serverConnector);
                return userBorder.create();

            case "Current Asset Offers":
                Page_CurrentAssetOffers currentAssetOffers = new Page_CurrentAssetOffers(serverConnector);
                return currentAssetOffers.create();

            case "Login":
                login = new Page_Login(serverConnector);
                panel_Login = login.create();
                return panel_Login;

            case "Get File":
                getFile = new Page_GetFile();
                panel_getFile = getFile.create();
                return panel_getFile;

            default:
                return panel_Login;
        }
    }

    /**
     * init function establishes a server connection, in the case there is no client.conf file it sets the value of fileException to true.
     * @throws IOException exception with props.load(configFile)
     */
    public static void init() throws IOException {
        Properties props = new Properties();
        File file = new File(Paths.get(System.getProperty("user.dir"), "client.conf").toAbsolutePath().toString());
        try {
            InputStream configFile = new FileInputStream(file);
            props.load(configFile);
            serverConnector = new ServerConnector(props);
            Runnable updateCheckerRunnable = new UpdateChecker(serverConnector, jPanel);
            Thread updateCheckerThread = new Thread(updateCheckerRunnable);
            System.out.println("Starting Update Checker");
            updateCheckerThread.start();
        } catch (FileNotFoundException e) {
            fileException = true;
        }
    }

    /**
     * makeDisplay initialises the display by calling init to set up a server connection, creating a jFrame and JPanel
     * if there is a file exception from init the getFile page will be shown else the login page will be shown.
     * @throws IOException thrown in init function
     */
    public void makeDisplay() throws IOException {
        init();

        jFrame = new JFrame("QUT Asset Trader");
        jFrame.setSize(800, 600);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jPanel = new JPanel();
        jUserPanel = new JPanel();
        cards = new CardLayout();
        jPanel.setLayout(cards);

        if(fileException){
            getFile = new Page_GetFile();
            panel_getFile = getFile.create();
            jPanel.add(panel_getFile, "Get File");
            panel_getFile.setVisible(true);
        }

        else {
            login = new Page_Login(serverConnector);
            panel_Login = login.create();
            jPanel.add(panel_Login, "Login");
            panel_Login.setVisible(true);
        }
        jFrame.getContentPane().add(jPanel, BorderLayout.NORTH);
        jFrame.pack();
        jFrame.setVisible(true);
    }

    /**
     * changePanel switches the card displayed in jPanel.
     * @param card the card to switch to
     * @throws IOException thrown in some pages
     */
    public static void changePanel(String card) throws IOException {
        if(loggedIn) {
            // ** Temporary code to get user info, this should be relocated to its own thread under run maybe so it updates every 10 seconds? ** //

            HashMap<String,Object> query = new HashMap<>();
            HashMap<String,Object> reply;
            query.put("type","getUserInfo");
            query.put("session",Main.sessionId);
            try {
                reply = serverConnector.queryServer(query);
                System.out.println(reply.toString());
                if(reply.get("accountType").equals("Admin")){
                    authorised = true;
                }
            } catch (IOException ignored) {
            }
            // ** End Temporary Code ** //
            // dynamically change user border to reflect if on main page or not
            cardName = card;
            Component[] components = jUserPanel.getComponents();
            for(Component c : components){
                if(c instanceof JPanel){
                    jUserPanel.remove(c);
                }
            }
            jUserPanel.add(createPanels("User Border"));
            jUserPanel.repaint();
            jUserPanel.revalidate();
            jFrame.getContentPane().add(jUserPanel,BorderLayout.SOUTH);
        } else {
            Component[] components = jUserPanel.getComponents();
            for(Component c : components){
                if(c instanceof JPanel){
                    jUserPanel.remove(c);
                }
            }
        }
        jPanel.add(createPanels(card), card);
        jPanel.repaint();
        jPanel.revalidate();
        cards.show(jPanel,card);
        jFrame.pack();
        jFrame.setVisible(true);
    }//change panel

    /**
     * run calls make display
     */
    @Override
    public void run() {
        try {
            makeDisplay();
        }
        catch(IOException e) {
        // to be done, throw a warning
        }
    }

    /**
     * function to invoke main.
     * @param args blank
     */
    public static void main(String[] args){
        SwingUtilities.invokeLater(new Main());
    }

}
