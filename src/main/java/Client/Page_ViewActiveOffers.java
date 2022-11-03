package Client;
//imports
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;

/**
 * Page_ViewActiveOffers creates panel for viewing active offers functionality
 */
public class Page_ViewActiveOffers {

    private static JPanel jPanel;
    static ServerConnector serverConnector;

    static Integer[] offerIds;

    static String[] allOffersAssets;
    static Integer[] allOffersAssetQuantity;
    static Integer[] allOffersCredits;
    static String[] allOffersType;
    static String[] allOffersUnit;

    static String[] myAssetsName;
    static Integer[] myOffersAssetQuantity;
    static Integer[] myOffersCredits;
    static String[] myOffersType;

    /**
     * Page_ViewActiveOffers constructor
     * @param serverConnector current server connection
     */
    public Page_ViewActiveOffers(ServerConnector serverConnector){
        Page_ViewActiveOffers.serverConnector =serverConnector; }

    /**
     * adds elements to jPanel
     * @return jPanel
     */
    public JPanel create(){
        //query offers
        HashMap<String, Object> request = new HashMap<>();
        request.put("type", "viewOffers");
        try {
            HashMap<String, Object> response = serverConnector.queryServer(request);
            System.out.println(response);
            HashMap<String, Object> offers = (HashMap<String, Object>) response.get("offers");
            System.out.println(offers);
            //add offers to arrays
            if(offers != null) {
                allOffersAssets = new String[offers.size()];
                allOffersAssetQuantity = new Integer[offers.size()];
                allOffersCredits = new Integer[offers.size()];
                allOffersType = new String[offers.size()];
                allOffersUnit = new String[offers.size()];
                Integer[] keySet = offers.keySet().toArray(new Integer[0]);
                for (int i = 0; i < keySet.length; i++)
                {
                    Integer index = keySet[i];
                    HashMap<String, Object> anOffer = (HashMap<String, Object>) offers.get(index);
                    System.out.println(anOffer);
                    allOffersAssets[i] = (String) anOffer.get("Asset Name");
                    allOffersAssetQuantity[i] = (Integer) anOffer.get("Quantity");
                    allOffersCredits[i] = (Integer) anOffer.get("Price");
                    allOffersType[i] = (String) anOffer.get("Type");
                    allOffersUnit[i] = (String) anOffer.get("Organisational Unit");
                }
            }
            else{
                allOffersAssets = new String[0];
                allOffersAssetQuantity = new Integer[0];
                allOffersCredits = new Integer[0];
                allOffersType = new String[0];
                allOffersUnit = new String[0];
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        //query users offers
        request.put("type", "viewMyOffers");
        try {
            HashMap<String, Object> response = serverConnector.queryServer(request);
            System.out.println(response);
            HashMap<String, Object> offers = (HashMap<String, Object>) response.get("offers");
            System.out.println(offers);
            //add offers to arrays
            if(offers != null) {
                myAssetsName = new String[offers.size()];
                myOffersAssetQuantity = new Integer[offers.size()];
                myOffersCredits = new Integer[offers.size()];
                myOffersType = new String[offers.size()];
                offerIds = offers.keySet().toArray(new Integer[0]);
                for (int i = 0; i < offerIds.length; i++)
                {
                    Integer index = offerIds[i];
                    HashMap<String, Object> anOffer = (HashMap<String, Object>) offers.get(index);
                    System.out.println(anOffer);
                    myAssetsName[i] = (String) anOffer.get("Asset Name");
                    myOffersAssetQuantity[i] = (Integer) anOffer.get("Quantity");
                    myOffersCredits[i] = (Integer) anOffer.get("Price");
                    myOffersType[i] = (String) anOffer.get("Type");
                }
            }
            else{
                myAssetsName = new String[0];
                myOffersAssetQuantity = new Integer[0];
                myOffersCredits = new Integer[0];
                myOffersType = new String[0];
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        //create new panels, tabbed pane
        jPanel = new JPanel();
        JPanel allActiveOffers = new JPanel();
        JPanel unitActiveOffers = new JPanel();
        JTabbedPane jTabbedPane = new JTabbedPane();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();

        //set grid bag layout
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        jPanel.setLayout(gridBagLayout);

        //add tableMyOffers to scroll pane and add action listeners to buttons in table
        JTable tableMyOffers = new JTable(new JTableModel());
        JScrollPane scrollPaneMyOffers = new JScrollPane(tableMyOffers);
        tableMyOffers.setFillsViewportHeight(true);
        tableMyOffers.getTableHeader().setReorderingAllowed(false);
        TableCellRenderer buttonRenderer = new JTableButtonRenderer();
        tableMyOffers.getColumn("Remove").setCellRenderer(buttonRenderer);
        tableMyOffers.addMouseListener(new JTableButtonMouseListener(tableMyOffers));

        //add table unit offers to scroll pane
        JTable tableUnitOffers = new JTable(new JTableModel2());
        JScrollPane scrollPaneUnitOffers = new JScrollPane(tableUnitOffers);
        tableUnitOffers.setFillsViewportHeight(true);
        tableUnitOffers.getTableHeader().setReorderingAllowed(false);

        //add scroll panes to panels
        allActiveOffers.add(scrollPaneMyOffers); //panel1
        unitActiveOffers.add(scrollPaneUnitOffers); //panel2

        //add panels to tabbed pane
        jTabbedPane.add("All Active Offers", unitActiveOffers);
        jTabbedPane.add("My Unit's Active Offers", allActiveOffers);

        //add tabbed pane to jPanel
        jPanel.add(jTabbedPane);
        //return jPanel
        return jPanel;
    }//end create

    /**
     * JTable creates all offers tables
     * Code adapted from:
     * https://www.cordinc.com/blog/2010/01/jbuttons-in-a-jtable.html
     */
    public static class JTableModel extends AbstractTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = new String[] {"Asset Name", "Quantity", "Credits", "Offer", "Remove"};
        private static final Class<?>[] COLUMN_TYPES = new Class<?>[] {String.class, String.class, String.class, String.class, JButton.class};


        @Override public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override public int getRowCount() {
            return myAssetsName.length;
        }

        @Override public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        @Override public Object getValueAt(final int rowIndex, final int columnIndex) {
            switch (columnIndex) {
                case 0: return myAssetsName[rowIndex];
                case 1: return myOffersAssetQuantity[rowIndex];
                case 2: return myOffersCredits[rowIndex];
                case 3: return myOffersType[rowIndex];
                case 4: final JButton button = new JButton("Remove");
                    button.addActionListener(arg0 -> {
                        HashMap<String, Object> request = new HashMap<>();
                        request.put("type", "removeOffer");
                        request.put("offerId", offerIds[rowIndex]);
                        try {
                            HashMap<String, Object> response = serverConnector.queryServer(request);
                            System.out.println(response);
                            if((Integer) response.get("status") == 200){
                                JOptionPane.showMessageDialog(jPanel, "Offer Deleted");
                                //need to refresh page so deleted item no longer shows
                                Main.changePanel("View Active Offers");
                            }
                            else if((Integer)response.get("status")== (400 | 401)) {
                                String error =(String)response.get("error");
                                System.out.println(error);
                                JOptionPane.showMessageDialog(jPanel, error,  error, JOptionPane.ERROR_MESSAGE);
                            }

                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    });
                    return button;
                default: return "Error";
            }
        }
    }//end JTable

    /**
     * JTable2 creates my offers table
     * Code adapted from:
     * https://www.cordinc.com/blog/2010/01/jbuttons-in-a-jtable.html
     */
    public static class JTableModel2 extends AbstractTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = new String[] {"Asset Name", "Quantity", "Credits","Offer", "Unit"};
        private static final Class<?>[] COLUMN_TYPES = new Class<?>[] {String.class, String.class, String.class, String.class, String.class};


        @Override public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override public int getRowCount() {
            return allOffersAssets.length;
        }

        @Override public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        @Override public Object getValueAt(final int rowIndex, final int columnIndex) {
            return switch (columnIndex) {
                case 0 -> allOffersAssets[rowIndex];
                case 1 -> allOffersAssetQuantity[rowIndex];
                case 2 -> allOffersCredits[rowIndex];
                case 3 -> allOffersType[rowIndex];
                case 4 -> allOffersUnit[rowIndex];
                default -> "Error";
            };
        }
    }//end JTable2

    /**
     * Button renderer to help render buttons within tables
     */
    private static class JTableButtonRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = (JButton)value;
            if (isSelected) {
                button.setForeground(table.getSelectionForeground());
                button.setBackground(table.getSelectionBackground());
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(UIManager.getColor("Button.background"));
            }
            return button;
        }
    }//end button renderer

    //mouse listener for button clicks in table
    private static class JTableButtonMouseListener extends MouseAdapter {
        private final JTable table;

        public JTableButtonMouseListener(JTable table) {
            this.table = table;
        }

        public void mouseClicked(MouseEvent e) {
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            int row    = e.getY()/table.getRowHeight();

            if (row < table.getRowCount() && row >= 0 && column < table.getColumnCount() && column >= 0) {
                Object value = table.getValueAt(row, column);
                if (value instanceof JButton) {
                    ((JButton)value).doClick();
                }
            }
        }
    }//end mouse listener

}//end ViewActiveOffers Class