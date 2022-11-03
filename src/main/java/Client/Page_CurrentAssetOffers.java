package Client;
//imports
import java.awt.*;
import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;

/**
 * Page_CurrentAssetOffers class creates a jPanel for the viewing offers of a certain asset type functionality
 */
public class Page_CurrentAssetOffers {

    static ServerConnector serverConnector;
    static String[] assetName;
    static Integer[] assetQuantity;
    static Integer[] assetPrice;
    static String[] offerType;
    static String[] offerUnit;

    /**
     * Constructor for class
     * @param serverConnector the current server connection
     */
    public Page_CurrentAssetOffers(ServerConnector serverConnector) {
        Page_CurrentAssetOffers.serverConnector = serverConnector;
    }

    /**
     * Create the jPanel and adds all part to it
     * @return jPanel
     */
    public JPanel create() {
        //query server for current asset offers given asset ID selected
        HashMap<String, Object> request = new HashMap<>();
        request.put("type", "viewAssetOffers");
        request.put("assetId", Main.assetHistoryToDisplay+1);
        try {
            HashMap<String, Object> response = serverConnector.queryServer(request);
            System.out.println(response);
            HashMap<String, Object> offers = (HashMap<String, Object>) response.get("offers");
            System.out.println(offers);
            //Add HashMap response to arrays to be displayed by table
            if (offers != null) {
                assetName = new String[offers.size()];
                assetQuantity = new Integer[offers.size()];
                assetPrice = new Integer[offers.size()];
                offerType = new String[offers.size()];
                offerUnit = new String[offers.size()];

                Integer[] keySet = offers.keySet().toArray(new Integer[0]);
                for (int i = 0; i < keySet.length; i++) {
                    Integer index = keySet[i];
                    HashMap<String, Object> anOffer = (HashMap<String, Object>) offers.get(index);
                    System.out.println(anOffer);
                    assetName[i] = (String) anOffer.get("Asset Name");
                    assetQuantity[i] = (Integer) anOffer.get("Quantity");
                    assetPrice[i] = (Integer) anOffer.get("Price");
                    offerType[i] = (String) anOffer.get("Type");
                    offerUnit[i] = (String) anOffer.get("Organisational Unit");
                }
            } else {
                assetName = new String[0];
                assetQuantity = new Integer[0];
                assetPrice = new Integer[0];
                offerType = new String[0];
                offerUnit = new String[0];
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        //create jPanel and paint it white
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();
        //construct grid bad layout and add to panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        jPanel.setLayout(gridBagLayout);
        //Add table to scroll pane and scroll pane to panel
        JTable tableAssetOffers = new JTable(new JTableModel());
        JScrollPane scrollPaneAssetOffers = new JScrollPane(tableAssetOffers);
        tableAssetOffers.setFillsViewportHeight(true);
        tableAssetOffers.getTableHeader().setReorderingAllowed(false);

        jPanel.add(scrollPaneAssetOffers);
        //return the panel
        return jPanel;
    }//end create

    /**
     * JTableModel creates a new JTable for displaying test
     * Code adapted from:
     * https://www.cordinc.com/blog/2010/01/jbuttons-in-a-jtable.html
     * */
    public static class JTableModel extends AbstractTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = new String[]{"Asset Name", "Quantity", "Credits", "Offer", "Unit"};
        private static final Class<?>[] COLUMN_TYPES = new Class<?>[]{String.class, String.class, String.class, String.class, String.class};


        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public int getRowCount() {
            return assetName.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        @Override
        public Object getValueAt(final int rowIndex, final int columnIndex) {
            return switch (columnIndex) {
                case 0 -> assetName[rowIndex];
                case 1 -> assetQuantity[rowIndex];
                case 2 -> assetPrice[rowIndex];
                case 3 -> offerType[rowIndex];
                case 4 -> offerUnit[rowIndex];
                default -> "Error";
            };
        }
    }//End JTable

}//end class