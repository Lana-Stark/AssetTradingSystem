package Client;
//imports
import java.awt.*;
import java.io.IOException;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
//Code for implementing jTable with buttons adapted from this webpage
//https://www.cordinc.com/blog/2010/01/jbuttons-in-a-jtable.html

/**
 * Page_AssetHistory
 * Returns a jPanel that display asset history
 */
public class Page_AssetHistory {
    private static String[] datesSold;
    private static Integer[] prices;
    ServerConnector serverConnector;

    /**
     * Constructor
     * @param serverConnector serverConnector from main
     */
    public Page_AssetHistory(ServerConnector serverConnector) {
        this.serverConnector = serverConnector;
    }

    /**
     * creates the jPanel and adds elements
     * @return jPanel
     * @throws IOException from making sql request
     */
    public JPanel create() throws IOException {
        //fetch asset history from database
        HashMap<String, Object> request = serverConnector.History_Request(Main.assetHistoryToDisplay+1);
        System.out.println(request);
        HashMap<LocalDateTime, Integer> historyRequest = (HashMap<LocalDateTime, Integer>) request.get("history");
        //Add to the array of date time and dates sold the information from query
        if(historyRequest != null){
            LocalDateTime[] datesSoldLDT = historyRequest.keySet().toArray(new LocalDateTime[0]);
            datesSold = new String[datesSoldLDT.length];
            for(int i = 0; i < datesSold.length; i++){
                datesSold[i] = datesSoldLDT[i].toString();
            }
            prices = historyRequest.values().toArray(new Integer[0]);
        }
        else{
            datesSold = new String[]{"No history"};
            prices = new Integer[]{0};
        }
        //create a new jPanel and paint background white
        JPanel jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();
        //Define Grid Bag Layout constraints and add to panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.weighty = 1;
        jPanel.setLayout(gridBagLayout);
        //Define a new table and add it to a scroll pane
        JTable table = new JTable(new JTableModel());
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        request = serverConnector.getAssets_Request();
        HashMap<Integer, String> assetRequest = (HashMap<Integer, String>) request.get("assets");
        System.out.println(assetRequest);
        if(assetRequest != null) {
            jPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
                    assetRequest.get(Main.assetHistoryToDisplay+1),
                    TitledBorder.CENTER,
                    TitledBorder.TOP));
        }
        //Add scrollPane to jPanel
        jPanel.add(scrollPane);
        //Return JPanel
        return jPanel;
    }//end create

    /**
     * JTableModel class creates a JTable to display history with two columns
     * Code for implementing jTable with buttons adapted from this webpage
     * https://www.cordinc.com/blog/2010/01/jbuttons-in-a-jtable.html
     */
    public static class JTableModel extends AbstractTableModel {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final String[] COLUMN_NAMES = new String[]{"Date", "Price"};
        private static final Class<?>[] COLUMN_TYPES = new Class<?>[]{String.class, String.class, String.class, JButton.class};

        @Override
        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        @Override
        public int getRowCount() {
            return datesSold.length;
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
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return datesSold[rowIndex];
            } else if (columnIndex == 1) {
                return prices[rowIndex] + " Credits per Item";
            }
            return "Error";
        }
    }//end JTable
}//end class