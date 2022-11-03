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
 * Page reset password creates jPanel for reset password functionality
 */
public class Page_ResetPassword extends JPanel implements ActionListener {
    private JPanel jPanel;
    JButton button_Reset;
    JPasswordField passwordField_Password;
    JPasswordField passwordField_RepeatPassword;
    ServerConnector serverConnector;

    /**
     * Page_ResetPassword constructor
     * @param serverConnector current server connection
     */
    public Page_ResetPassword(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Create adds elements to panel
     * @return panel
     */
    public JPanel create(){
        //create panel
        jPanel = new JPanel();
        //set grid bad layout constraints
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        jPanel.setLayout(gridBagLayout);
        // add password field
        JLabel label_Password = new JLabel("Password: ");
        add(label_Password,1,3,1, jPanel,gridBagConstraints);
        passwordField_Password = new JPasswordField(20);
        add(passwordField_Password,2,3,4, jPanel,gridBagConstraints);
        //Add repeat password field
        JLabel label_RepeatPassword = new JLabel("Repeat Password: ");
        add(label_RepeatPassword,1,4,1, jPanel,gridBagConstraints);
        passwordField_RepeatPassword = new JPasswordField(20);
        add(passwordField_RepeatPassword,2,4,4, jPanel,gridBagConstraints);
        //add reset button with action listener
        button_Reset = new JButton( "Reset");
        add(button_Reset,1,5,5, jPanel,gridBagConstraints);
        button_Reset.addActionListener(this);
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
     * Action performed override to reset password when button pressed
     * @param e button pressed
     */
    @Override
    public void actionPerformed(ActionEvent e){
        String password;
        String repeatPassword;
        //attempt to reset password if passwords match
        if(e.getSource()==button_Reset){
            password=new String(passwordField_Password.getPassword());
            password = password.trim();
            repeatPassword = new String(passwordField_RepeatPassword.getPassword());
            repeatPassword = repeatPassword.trim();
            System.out.println(password+repeatPassword);
            if(!(password.isEmpty()) && (password.equals(repeatPassword))) {
                try {
                    HashMap<String, Object> response = serverConnector.changePassword_Request(Main.sessionId, password);
                    if ((Integer) response.get("status") == 200) {
                        JOptionPane.showMessageDialog(jPanel, "Password Updated");
                    } else if ((Integer) response.get("status") == 400) {
                        String error = (String) response.get("error");
                        System.out.println(error);
                        JOptionPane.showMessageDialog(jPanel, error, error, JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException | NoSuchAlgorithmException exception) {
                    exception.printStackTrace();
                }
            }
            else if(password.equals(repeatPassword)){
                JOptionPane.showMessageDialog(jPanel, "Passwords do not match");
            }
            else if(password.isEmpty()){
                JOptionPane.showMessageDialog(jPanel, "Password cannot be empty");
            }
        }
    }//end action performed
}//end class

