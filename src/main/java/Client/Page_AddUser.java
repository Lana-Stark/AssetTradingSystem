package Client;
//imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.HashMap;

/**
 * Page_AddUser class generates jPanel for Add User
 */
public class Page_AddUser implements ActionListener {
    private JPanel jPanel;
    ServerConnector serverConnector;
    JTextField textField_userName;
    JTextField textField_firstName;
    JTextField textField_lastName;
    JPasswordField passwordField_Password;
    JButton button_CreateUser;
    JCheckBox checkBox_Admin;
    JComboBox<String> comboBox_unit;

    /**
     * Page_AddUser Constructor
     * @param serverConnector instance of ServerConnector generated in main
     */
    public Page_AddUser(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * creates a new jPanel for adding users.
     * @return panel
     */
    public JPanel create(){
        //Create new jPanel and pain background white
        jPanel = new JPanel();
        jPanel.setBackground(Color.WHITE);
        jPanel.repaint();
        //Set GridBag constraints and add to panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        jPanel.setLayout(gridBagLayout);
        //Add username field
        JLabel label_userName = new JLabel("Username: ");
        add(label_userName,1,3,1, jPanel,gridBagConstraints);
        textField_userName = new JTextField(20);
        add(textField_userName,2,3,4, jPanel,gridBagConstraints);
        //Add firstname field
        JLabel label_firstName = new JLabel("First Name: ");
        add(label_firstName,1,4,1, jPanel,gridBagConstraints);
        textField_firstName = new JTextField(20);
        add(textField_firstName,2,4,4, jPanel,gridBagConstraints);
        //Add last name field
        JLabel label_lastName = new JLabel("Last Name: ");
        add(label_lastName,1,5,1, jPanel,gridBagConstraints);
        textField_lastName = new JTextField(20);
        add(textField_lastName,2,5,4, jPanel,gridBagConstraints);
        //Add units to choose from
        JLabel label_unit = new JLabel("Unit: ");
        add(label_unit,1,6,1, jPanel,gridBagConstraints);
        //query units from database
        HashMap<Integer,String> units = new HashMap<>();
        HashMap<String,Object> response;
        try{
            response = serverConnector.getUnits_Request();
            System.out.println(response);
            units = (HashMap<Integer, String>) response.get("units");
            System.out.println(units);
        }
        catch (IOException ioException){ioException.printStackTrace();}
        //Create a String array for the units with the HashMap of units provided
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
        //Add the unitNames to a combo box
        comboBox_unit = new JComboBox<>(unitNamesArray);
        add(comboBox_unit,2,6,4, jPanel,gridBagConstraints);
        //Add password field
        JLabel label_password = new JLabel("Password: ");
        add(label_password,1,7,1, jPanel,gridBagConstraints);
        passwordField_Password = new JPasswordField(20);
        add(passwordField_Password,2,7,4, jPanel,gridBagConstraints);
        //Add Create User button with action listener
        button_CreateUser = new JButton( "Create User");
        add(button_CreateUser,1,8,5, jPanel,gridBagConstraints);
        button_CreateUser.addActionListener(this);
        //Add check box or admin
        checkBox_Admin = new JCheckBox( "Administrator");
        checkBox_Admin.setBackground(Color.white);
        add(checkBox_Admin,0,8,5, jPanel,gridBagConstraints);
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
     * Action Performed override method
     * @param e an event
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        int unitId;
        String username;
        String firstName;
        String lastName;
        String password;
        boolean admin;
        //in the event create user is pressed attempt to add new user to database and provide feedback to user
        if(e.getSource() == button_CreateUser) {
            unitId = comboBox_unit.getSelectedIndex() + 1;
            username = textField_userName.getText().trim();
            firstName = textField_firstName.getText().trim();
            lastName = textField_lastName.getText().trim();
            password = new String(passwordField_Password.getPassword());
            password = password.trim();
            admin = checkBox_Admin.isSelected();
            if (!(username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty())) {

                try {
                    HashMap<String, Object> response = serverConnector.addUser_Request(unitId, username, firstName, lastName, password, admin);
                    if ((Integer) response.get("status") == 200) {
                        JOptionPane.showMessageDialog(jPanel, MessageFormat.format("{0} added to the database", username));
                    } else if ((Integer) response.get("status") == 400) {
                        String error = (String) response.get("error");
                        System.out.println(error);
                        JOptionPane.showMessageDialog(jPanel, error, error, JOptionPane.ERROR_MESSAGE);
                    } else {
                        System.out.println(response);
                    }
                } catch (IOException | NoSuchAlgorithmException ioException) {
                    ioException.printStackTrace();
                }
            }
            else {
                JOptionPane.showMessageDialog(jPanel, "Please fill in all the forms.");
            }
        }
    }//end action performed
}//end class

