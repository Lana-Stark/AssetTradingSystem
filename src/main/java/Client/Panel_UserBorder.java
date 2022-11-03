package Client;
//imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;

/**
 * Panel user border class create panel for display at bottom of gui that allows users to navigate between pages
 * It displays users name, unit and credits.
 */
public class Panel_UserBorder extends JPanel implements ActionListener{
    JTextField textField_Username;
    JTextField textField_Department;
    JTextField textField_Credits;
    JButton button_back;
    ServerConnector serverConnector;

    /**
     * Creates a new instance of page UserBorder
     * @param serverConnector   Existing serverConnector
     */
    public Panel_UserBorder(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Creates a panel with all the elements
     * @return panel
     */
    public JPanel create(){
        //create new panel
        JPanel jPanel = new JPanel();
        //query user info
        HashMap<String,Object> query = new HashMap<>();
        HashMap<String,Object> reply;
        query.put("type","getUserInfo");
        query.put("session",Main.sessionId);
        String usersName = null;
        String usersUnit = null;
        String usersCredits = null;
        try {
            reply = serverConnector.queryServer(query);
            usersName = (String) reply.get("name");
            usersUnit = (String) reply.get("unit");
            usersCredits = Integer.toString((Integer)reply.get("credits"));
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        //add username text field
        textField_Username = new JTextField(usersName);
        textField_Username.setEditable(false);
        jPanel.add(textField_Username,BorderLayout.WEST);
        //add users unit text field
        textField_Department = new JTextField(usersUnit);
        textField_Department.setEditable(false);
        jPanel.add(textField_Department,BorderLayout.CENTER);
        //add users credits field
        textField_Credits = new JTextField(usersCredits+ " Credits");
        textField_Credits.setEditable(false);
        jPanel.add(textField_Credits,BorderLayout.EAST);
        //add button if main add log out and if other add back add action listener to button
        if(Main.cardName.equals("Main")){
            button_back = new JButton("LOG OUT");
        }
        else {
            button_back = new JButton("BACK");
        }
        jPanel.add(button_back, "SOUTH");
        button_back.addActionListener(this);
        //return panel
        return jPanel;
    }

    /**
     * Action performed override
     * @param e button pressed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==button_back){
            if(Main.cardName.equals("Main")){
                //code for logout functionality
                try {
                    serverConnector.logOut_Request();
                    Main.loggedIn = false;
                    Main.authorised = false;
                    Main.jPanel.repaint();
                    Main.changePanel("Login");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
            else {
                try {
                    Main.changePanel("Main");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }//end action performed
}//end user border class
