package Client;
//imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;

/**
 * Page_NewOffer class creates a new panel for new offer functionality
 */
public class Page_NewOffer implements ActionListener {

    private JPanel jPanel;
    ServerConnector serverConnector;
    JComboBox<String> comboBox_BuySell;
    JComboBox<String> comboBox_Assets;
    JTextField textField_quantity;
    JTextField textField_credits;
    JButton button_Create;

    /**
     * Constructor for Page_NewOffer
     * @param serverConnector current server connection
     */
    public Page_NewOffer(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Create function adds elements to jPanel
     * @return panel
     */
    public JPanel create()
    {
        //create panel and set background white
        jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();
        //define layout constraints and add to panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        jPanel.setLayout(gridBagLayout);
        //add Buy/Sell field
        JLabel label_BuySell = new JLabel("BUY/SELL: ");
        add(label_BuySell,1,1,1, jPanel,gridBagConstraints);
        String[] buySell = {"BUY", "SELL"};
        comboBox_BuySell = new JComboBox<>(buySell);
        add(comboBox_BuySell,2,1,4, jPanel,gridBagConstraints);
        //Add asset field
        JLabel label_asset = new JLabel("Asset: ");
        add(label_asset,1,2,1, jPanel,gridBagConstraints);
        //query server for current assets
        HashMap<String,Object> response;
        HashMap<Integer,String> assets = new HashMap<>();
        try{
            response = serverConnector.getAssets_Request();
            assets = (HashMap<Integer, String>) response.get("assets");
        }
        catch (IOException ioException){ioException.printStackTrace();}
        //add assets from hashmap response to an array
        String[] assetNamesArray;
        if(assets != null) {
            assetNamesArray = new String[assets.size()];
            for(int i = 1; i <= assets.size(); i++){
                String assetName = String.valueOf(assets.get(i));
                assetNamesArray[i-1] = assetName;
            }
        }
        else{
            assetNamesArray = new String[0];
        }
        //create combo box with asset names
        comboBox_Assets = new JComboBox<>(assetNamesArray);
        add(comboBox_Assets,2,2,4, jPanel,gridBagConstraints);
        //add quantity field
        JLabel label_quantity = new JLabel("Quantity: ");
        add(label_quantity, 1, 3, 1, jPanel, gridBagConstraints);
        textField_quantity = new JTextField(20);
        add(textField_quantity, 2, 3, 4, jPanel, gridBagConstraints);
        //add credits field
        JLabel label_credits = new JLabel("Credits/Item: ");
        add(label_credits,1,4,1, jPanel,gridBagConstraints);
        textField_credits = new JTextField(20);
        add(textField_credits,2,4,4, jPanel,gridBagConstraints);
        //add create button with action listener
        button_Create = new JButton( "Create");
        add(button_Create,1,7,5, jPanel,gridBagConstraints);
        button_Create.addActionListener(this);
        //return panel
        return jPanel;
    }//end create

    /**
     * A helper method to add component to panel
     * @param component the component to be added to panel
     * @param x the x co-ordinate of the location
     * @param y the desired y-co-ordinate
     * @param width - the width of the component
     * @param jPanel - the jPanel to add the component to
     * @param gridBagConstraints - the constraints of the layout
     */
    private void add(Component component, int x, int y, int width, JPanel jPanel, GridBagConstraints gridBagConstraints){
        gridBagConstraints.gridx=x;
        gridBagConstraints.gridy=y;
        gridBagConstraints.gridwidth=width;
        gridBagConstraints.gridheight= 1;
        jPanel.add(component, gridBagConstraints);
    }//end add

    /**
     * Override action performed to attempt to add new offer when button pressed
     * @param e button pressed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String offerType;
        int quantity;
        int price;
        int unitId;
        int assetId;
        if(e.getSource()==button_Create){
            try{
                quantity = Integer.parseInt(textField_quantity.getText());
                price = Integer.parseInt(textField_credits.getText());
                offerType = (String) comboBox_BuySell.getSelectedItem();
                unitId = 0;
                HashMap<String,Object> query = new HashMap<>();
                HashMap<String,Object> reply;
                query.put("type","getUserInfo");
                try {
                    reply = serverConnector.queryServer(query);
                    unitId = (Integer) reply.get("unitId");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                assetId = comboBox_Assets.getSelectedIndex()+1;
                try{
                    HashMap<String, Object> response = serverConnector.addOffer_Request(offerType, quantity, price, assetId, unitId);
                    if((Integer)response.get("status")==200){
                        JOptionPane.showMessageDialog(jPanel, MessageFormat.format("{0} order added to the database",offerType));
                    }
                    else if((Integer)response.get("status")==400){
                        String error =(String)response.get("error");
                        System.out.println(error);
                        JOptionPane.showMessageDialog(jPanel, error,  error, JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (IOException ioException){
                    ioException.printStackTrace();
                }
            }
            catch (NumberFormatException numberFormatException){
                JOptionPane.showMessageDialog(jPanel, "Please enter a number for quantity and price");
            }

        }
    }//end action performed
}//end class
