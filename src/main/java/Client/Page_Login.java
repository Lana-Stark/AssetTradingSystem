package Client;
//imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 * Page_Login class creates a panel for login functionality
 */
public class Page_Login extends JPanel implements ActionListener {
    private JPanel jPanel;
    JButton button_Login;
    JPasswordField passwordField_Password;
    JTextField textField_Username;
    ServerConnector serverConnector;

    /**
     * Page Login constructor
     * @param serverConnector the current server connection
     */
    public Page_Login(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Create function add all elements to the panel
     * @return panel
     */
    public JPanel create(){
        //create a new panel
        jPanel = new JPanel();
        //Define layout and add to panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        jPanel.setLayout(gridBagLayout);
        //add username field
        JLabel label_Username = new JLabel("Username: ");
        add(label_Username,1,3,1, jPanel,gridBagConstraints);
        textField_Username = new JTextField(20);
        textField_Username.setText("");
        add(textField_Username,2,3,4, jPanel,gridBagConstraints);
        //add password field
        JLabel label_Password = new JLabel("Password: ");
        add(label_Password,1,4,1, jPanel,gridBagConstraints);
        passwordField_Password = new JPasswordField(20);
        passwordField_Password.setText("");
        add(passwordField_Password,2,4,4, jPanel,gridBagConstraints);
        //add login button with action listener
        button_Login = new JButton( "Login");
        add(button_Login,1,5,5, jPanel,gridBagConstraints);
        button_Login.addActionListener(this);
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
     * actionPerformed override defines what to do when button pressed
     * @param e event button press
     */
    @Override
    public void actionPerformed(ActionEvent e){
        String username;
        String password;
        if(e.getSource()==button_Login){
            username = textField_Username.getText().trim();
            password = new String(passwordField_Password.getPassword());
            password = password.trim();
            try {
                HashMap<String,Object> response = serverConnector.Login_Request(username, password);
                if((Integer)response.get("status")==200) {
                    Main.sessionId=(String)response.get("sessionId");
                    System.out.println(Main.sessionId);
                    Main.loggedIn = true;
                    Main.changePanel("Main");
                }
                else if((Integer)response.get("status")==400) {
                    String error =(String)response.get("error");
                    System.out.println(error);
                    Main.loggedIn = false;
                    JOptionPane.showMessageDialog(jPanel, error,  error, JOptionPane.ERROR_MESSAGE);
                }
            } catch (NoSuchAlgorithmException | IOException exception ) {
                exception.printStackTrace();
            }
        }
    }//end actionPerformed
}//end class
