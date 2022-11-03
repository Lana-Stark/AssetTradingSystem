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
 * Page_AddAsset
 * This class contains functionality for creating a jPanel for the add asset functionality
 */
public class Page_AddAsset implements ActionListener {
    //Definitions
    private JPanel jPanel;
    ServerConnector serverConnector;
    JButton button_createAsset;
    JTextField textField_assetName;

    /**
     * Page_AddAsset constructor
     * @param serverConnector : Connection to serverside
     */
    public Page_AddAsset(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Create makes a new jPanel of the Add Asset Type.
     * @return jPanel
     */
    public JPanel create()
    {
        //Make a new panel set background colour to white and repaint
        jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();
        //Define GridBagLayout Constraints for panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        //Assign panel to the layout
        jPanel.setLayout(gridBagLayout);
        //Add an asset name field
        JLabel label_assetName = new JLabel("Asset Name: ");
        add(label_assetName,1,1,1, jPanel,gridBagConstraints);
        textField_assetName = new JTextField(20);
        add(textField_assetName,2,1,4, jPanel,gridBagConstraints);
        //Add create button with action listener
        button_createAsset = new JButton( "Create");
        add(button_createAsset,1,3,5, jPanel,gridBagConstraints);
        button_createAsset.addActionListener(this);
        //return panel created
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
     * Action Performed Function
     * Will try to add an asset to database if appropriate once button clicked
     * @param e An event or button click
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String assetName;
        if(e.getSource() == button_createAsset)
        {
            assetName = textField_assetName.getText().trim();
            if(!assetName.isEmpty()){
                try {
                    HashMap<String,Object> response = serverConnector.addAsset_Request(assetName);
                    if((Integer)response.get("status")==200) {
                        JOptionPane.showMessageDialog(jPanel, MessageFormat.format("{0} added to the database",assetName));
                    }
                    else if((Integer)response.get("status")==400) {
                        String error =(String)response.get("error");
                        System.out.println(error);
                        JOptionPane.showMessageDialog(jPanel, error,  error, JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            else {
                JOptionPane.showMessageDialog(jPanel, "Please provide a name");
            }
        }
    }//End Action Performed function
}//End Page_AddAsset Class
