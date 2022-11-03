package Client;
//imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

/**
 * Page_ManageUnit class creates panel for manage unit functionality
 */
public class Page_ManageUnit implements ActionListener {

    private JPanel jPanel;
    ServerConnector serverConnector;
    JButton button_UpdateCredits;
    JComboBox<String> comboBox_department;
    JTextField textField_Credits;
    JComboBox<String> comboBox_asset;
    JTextField textField_AssetAmount;
    JButton button_UpdateAsset;
    int Credits;
    int AssetAmount;
    int UnitId;
    int AssetId;

    /**
     * Creates a new instance of page ManageUnit
     * @param serverConnector   Existing serverConnector
     */
    public Page_ManageUnit(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Add elements to jPanel
     * @return panel
     */
    public JPanel create()
    {
        //Create panel and paint white
        jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();
        //Create constraints and set layout of panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        jPanel.setLayout(gridBagLayout);
        //add department label
        JLabel label_department = new JLabel("Department: ");
        add(label_department,1,1,1, jPanel,gridBagConstraints);
        //fetch units from database
        HashMap<Integer,String> units = new HashMap<>();
        HashMap<String,Object> response;
        try{
            response = serverConnector.getUnits_Request();
            System.out.println(response);
            units = (HashMap<Integer, String>) response.get("units");
            System.out.println(units);
        }
        catch (IOException ioException){ioException.printStackTrace();}
        //add the units in hashmap response to an array
        String[] unitNamesArray;
        if(units != null) {
            unitNamesArray = new String[units.size()];
            for (int i = 1; i <= units.size(); i++){
                String unitNameString = String.valueOf(units.get(i));
                unitNamesArray[i-1] = unitNameString;
            }
        }
        else{
            unitNamesArray = new String[0];
        }
        //add the unit names array to the combo box
        comboBox_department = new JComboBox<>(unitNamesArray);
        add(comboBox_department,2,1,4, jPanel,gridBagConstraints);
        comboBox_department.addActionListener(this);
        UnitId = comboBox_department.getSelectedIndex()+1;
        //add credits label
        JLabel label_Credits = new JLabel("Credits");
        add(label_Credits, 1, 2, 1, jPanel, gridBagConstraints);
        textField_Credits = new JTextField(10);
        //fetch credits from database
        HashMap<String,Object> query = new HashMap<>();
        query.put("type","getCredits");
        query.put("unitId", UnitId);
        Credits = 0;
        try {
            response = serverConnector.queryServer(query);
            Credits = (Integer) response.get("credits");
            System.out.println(response.get("credits"));
        } catch (IOException | NumberFormatException ioException) {
            ioException.printStackTrace();
        }
        //add credit amount to text box
        textField_Credits.setText(String.format("%d", Credits));
        add(textField_Credits, 2, 2, 4, jPanel, gridBagConstraints);
        //add update credits button with action listener
        button_UpdateCredits = new JButton( "Update Credits");
        add(button_UpdateCredits,1,3,5, jPanel,gridBagConstraints);
        button_UpdateCredits.addActionListener(this);
        //add asset field
        JLabel label_asset = new JLabel("Asset: ");
        add(label_asset,1,4,1, jPanel,gridBagConstraints);
        //fetch assets from database
        HashMap<Integer,String> assets = new HashMap<>();
        query.put("type","getAssets");
        query.put("session",Main.sessionId);
        try{
            response = serverConnector.getAssets_Request();
            assets = (HashMap<Integer, String>) response.get("assets");
        }
        catch (IOException ioException){ioException.printStackTrace();}
        //put the assets into an array
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
        //add assets to combo box
        comboBox_asset = new JComboBox<>(assetNamesArray);
        add(comboBox_asset,2,4,4, jPanel,gridBagConstraints);
        comboBox_asset.addActionListener(this);
        AssetId = comboBox_asset.getSelectedIndex()+1;
        //add asset amount field
        JLabel label_AssetAmount = new JLabel("Asset Amount");
        add(label_AssetAmount, 1, 5, 1, jPanel, gridBagConstraints);
        textField_AssetAmount = new JTextField(10);
        AssetAmount = 0;
        //fetch current asset amount
        query = new HashMap<>();
        query.put("type","getAssetCount");
        query.put("unitId", UnitId);
        query.put("assetId", AssetId);
        try {
            response = serverConnector.queryServer(query);
            AssetAmount = (Integer) response.get("assetCount");
        } catch (IOException | NullPointerException e) {
            AssetAmount=0;
        }
        //add current asset amount to text field
        textField_AssetAmount.setText(String.format("%d", AssetAmount));
        add(textField_AssetAmount, 2, 5, 4, jPanel, gridBagConstraints);
        //add update asset button with action listener
        button_UpdateAsset = new JButton( "Update Asset");
        add(button_UpdateAsset,1,6,5, jPanel,gridBagConstraints);
        button_UpdateAsset.addActionListener(this);
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
     * Override action performed do appropriate handling based off event
     * @param e Event button clicked or combo box selected
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        int credits;
        int unitId;
        int assetId;
        int assetAmount;
        HashMap<Integer, Integer> assetsMap = new HashMap<>();
        //in the case update asset button is pressed attempt to update asset amount
        if(e.getSource() == button_UpdateAsset)
        {
            unitId = comboBox_department.getSelectedIndex()+1;
            assetId = comboBox_asset.getSelectedIndex()+1;
            assetAmount = Integer.parseInt(textField_AssetAmount.getText());
            try {
                assetsMap.put(assetId, assetAmount);
                System.out.println(assetsMap);
                HashMap<String, Object> response = serverConnector.updateOUAssets_Request(unitId, assetsMap);
                System.out.println(response);
                if ((Integer) response.get("status") == 200) {
                    JOptionPane.showMessageDialog(jPanel, ("Organisational Unit Updated"));
                } else if ((Integer) response.get("status") == 400) {
                    String error = (String) response.get("error");
                    System.out.println(error);
                    JOptionPane.showMessageDialog(jPanel, error, error, JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }
        //in the case update credit button is pressed attempt to update credit amount
        else if(e.getSource() == button_UpdateCredits){
            unitId = comboBox_department.getSelectedIndex()+1;
            credits = Integer.parseInt(textField_Credits.getText());
            try {
                HashMap<String, Object> response = serverConnector.setCredits_Request(unitId, credits);
                if ((Integer) response.get("status") == 200) {
                    JOptionPane.showMessageDialog(jPanel, ("Credits Updated"));
                    Main.changePanel("Manage Units");
                } else if ((Integer) response.get("status") == 400) {
                    String error = (String) response.get("error");
                    System.out.println(error);
                    JOptionPane.showMessageDialog(jPanel, error, error, JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        //in the case department selected changed update credits and asset amount
        else if (e.getSource() == comboBox_department){
            //update credits
            UnitId = comboBox_department.getSelectedIndex()+1;
            HashMap<String,Object> query = new HashMap<>();
            HashMap<String,Object> response;
            query.put("type","getCredits");
            query.put("unitId", UnitId);
            try {
                response = serverConnector.queryServer(query);
                Credits = (Integer) response.get("credits");
                System.out.println(response.get("credits"));
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            textField_Credits.setText(String.format("%d", Credits));
            //update asset amount
            AssetId = comboBox_asset.getSelectedIndex()+1;
            query = new HashMap<>();
            query.put("type","getAssetCount");
            query.put("unitId", UnitId);
            query.put("assetId", AssetId);
            try {
                response = serverConnector.queryServer(query);
                AssetAmount = (Integer) response.get("assetCount");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            textField_AssetAmount.setText(String.format("%d", AssetAmount));
        }
        //in the case asset selected changed update asset amount
        else if(e.getSource() == comboBox_asset){
            //update asset amount
            UnitId = comboBox_department.getSelectedIndex()+1;
            AssetId = comboBox_asset.getSelectedIndex()+1;
            HashMap<String,Object> query;
            HashMap<String,Object> response;
            query = new HashMap<>();
            query.put("type","getAssetCount");
            query.put("unitId", UnitId);
            query.put("assetId", AssetId);
            try {
                response = serverConnector.queryServer(query);
                AssetAmount = (Integer) response.get("assetCount");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            textField_AssetAmount.setText(String.format("%d", AssetAmount));
        }

    }//end action performed
}//end class
