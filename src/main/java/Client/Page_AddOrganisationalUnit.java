package Client;
//imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashMap;
import javax.swing.JOptionPane;

/**
 * Page_AddOrganisationalUnit class creates a panel for adding organisational units
 */
public class Page_AddOrganisationalUnit implements ActionListener {
    private JPanel jPanel;
    ServerConnector serverConnector;
    JTextField textField_UnitName;
    JTextField textField_credits;
    JButton button_createUnit;

    /**
     * Constructor with serverConnector passed from main to allow for connection to server.
     * @param serverConnector connection passed from main
     */
    public Page_AddOrganisationalUnit(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Create methods makes a panel to display all the information
     * @return panel
     */
    public JPanel create()
    {
        //create a new panel and paint it white
        jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();
        //Define GridBagLayout for components
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        //set the layout of the panel to the defined values
        jPanel.setLayout(gridBagLayout);
        //Make a unit name field
        JLabel label_UnitName = new JLabel("Unit Name: ");
        add(label_UnitName,1,1,1, jPanel,gridBagConstraints);
        textField_UnitName = new JTextField(20);
        add(textField_UnitName,2,1,4, jPanel,gridBagConstraints);
        //Make a credits field
        JLabel label_credits = new JLabel("Credits: ");
        add(label_credits,1,2,1, jPanel,gridBagConstraints);
        textField_credits = new JTextField(20);
        add(textField_credits,2,2,4, jPanel,gridBagConstraints);
        //Make a create button with action listener
        button_createUnit = new JButton( "Create");
        add(button_createUnit,1,4,5, jPanel,gridBagConstraints);
        button_createUnit.addActionListener(this);
        //return the panel
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
     * Define what action to take in event that button is pressed
     * @param e Action type
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String unitName;
        int credits;
        if(e.getSource() == button_createUnit)
        {
            unitName = textField_UnitName.getText().trim();
            if(!unitName.isEmpty()) {
                try {
                    credits = Integer.parseInt(textField_credits.getText());
                    try {
                        HashMap<String, Object> response = serverConnector.addUnit_Request(unitName, credits);
                        if ((Integer) response.get("status") == 200) {
                            JOptionPane.showMessageDialog(jPanel, MessageFormat.format("{0} added to the database", unitName));
                        } else if ((Integer) response.get("status") == 400) {
                            String error = (String) response.get("error");
                            System.out.println(error);
                            JOptionPane.showMessageDialog(jPanel, error, error, JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } catch (NumberFormatException numberFormatException) {
                    JOptionPane.showMessageDialog(jPanel, "Please input a numeral for number of credits");
                }
            }
            else {
                JOptionPane.showMessageDialog(jPanel, "Please provide a unit name");
            }
        }
    } //end action performed

}//end class
