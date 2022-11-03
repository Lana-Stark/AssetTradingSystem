package Client;
//imports
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Serial;
import java.util.HashMap;

/**
 * Page_ViewAssets class creates panel for view assets functionality
 */
public class Page_ViewAssets{

    ServerConnector serverConnector;

    static String[] assetNamesArray;
    static Integer[] assetAmountArray;

    /**
     * Class constructor
     * @param serverConnector current server connection
     */
    public Page_ViewAssets(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Create makes panel and adds elements
     * @return panel
     * @throws IOException with some sql exceptions/ query exceptions
     */
    public JPanel create() throws IOException {
        //query assets
        HashMap<String, Object> query = new HashMap<>();
        HashMap<String, Object> response;
        HashMap<Integer,String> assets = new HashMap<>();
        query.put("session",Main.sessionId);
        try{
            response = serverConnector.getAssets_Request();
            assets = (HashMap<Integer, String>) response.get("assets");
        }
        catch (IOException ioException){ioException.printStackTrace();}
        //add assets to table
        if(assets != null) {
            Integer unitId = 0;
            assetNamesArray = new String[assets.size()];
            assetAmountArray = new Integer[assets.size()];
            //query user info to get unitId
            HashMap<String,Object> reply;
            query.put("type","getUserInfo");
            query.put("session",Main.sessionId);
            try {
                reply = serverConnector.queryServer(query);
                unitId = (Integer) reply.get("unitId");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            //find the asset amount of each asset for the given unitID of user logged in
            for(int i = 1; i <= assets.size(); i++){
                String assetName = String.valueOf(assets.get(i));
                assetNamesArray[i-1] = assetName;
                query = new HashMap<>();
                query.put("type","getAssetCount");
                query.put("unitId", unitId);
                query.put("assetId", i);
                try {
                    response = serverConnector.queryServer(query);
                    assetAmountArray[i-1] = (Integer) response.get("assetCount");
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        else{
            assetNamesArray = new String[0];
            assetAmountArray = new Integer[0];
        }

        //create panel and set background white
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();

        //add grid bad layout constraints
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        jPanel.setLayout(gridBagLayout);

        //Create new JTable add to scroll pane and add button rendering and mouse listener for buttons
        JTable table = new JTable(new JTableModel());
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);
        TableCellRenderer buttonRenderer = new JTableButtonRenderer();
        table.getColumn("History").setCellRenderer(buttonRenderer);
        table.getColumn("Current Offers").setCellRenderer(buttonRenderer);
        table.addMouseListener(new JTableButtonMouseListener(table));

        //increase the size of the first column to allow for assets with longer names
        for (int i = 0; i < 4; i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            if (i == 2) {
                column.setPreferredWidth(200); //third column is bigger
            } else {
                column.setPreferredWidth(100);
            }
        }
        //add scroll pane to panel
        jPanel.add(scrollPane);
        //return panel
        return jPanel;
    }//end create

    /**
     * Create view assets table
     * Adapted from : https://www.cordinc.com/blog/2010/01/jbuttons-in-a-jtable.html
     */
    public static class JTableModel extends AbstractTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = new String[] {"Asset Name", "My Amount", "Current Offers", "History"};
        private static final Class<?>[] COLUMN_TYPES = new Class<?>[] {String.class, String.class, String.class,  JButton.class};


        @Override public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override public int getRowCount() {
            return assetNamesArray.length;
        }

        @Override public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        @Override public Object getValueAt(final int rowIndex, final int columnIndex) {
            switch (columnIndex) {
                case 0: return assetNamesArray[rowIndex];
                case 1: return assetAmountArray[rowIndex];
                case 2: final JButton buttonViewOffers = new JButton("View");
                    buttonViewOffers.addActionListener(e -> {
                        Main.assetHistoryToDisplay = rowIndex;
                        try {
                            Main.changePanel("Current Asset Offers");
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    });
                    return buttonViewOffers;
                case 3: final JButton button = new JButton("View");
                    button.addActionListener(e -> {
                        Main.assetHistoryToDisplay = rowIndex;
                        try {
                            Main.changePanel("Asset History");
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                        //Eventually this dialogue box to show Item History
                    });
                    return button;
                default: return "Error";
            }
        }
    }//end JTable

    /**
     * Button renderer for buttons within table
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
    }// end button renderer

    /**
     * Mouse listener for buttons within table
     */
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
}//end class