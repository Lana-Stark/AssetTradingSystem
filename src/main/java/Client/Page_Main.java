package Client;
//imports
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Page_Main class returns a panel for main page functionality
 */
public class Page_Main extends JFrame implements ActionListener {
    JButton button_viewAsset;
    JButton button_AddUser;
    JButton button_ActiveOffers;
    JButton button_ManageUnits;
    JButton button_NewAsset;
    JButton button_NewUnit;
    JButton button_NewOffer;
    JButton button_ResetPassword;
    ServerConnector serverConnector;

    /**
     * Constructor for Page_Main
     * @param serverConnector current server connection
     */
    public Page_Main(ServerConnector serverConnector){this.serverConnector=serverConnector; }

    /**
     * Add elements to the jPanel
     * @return panel
     */
    public JPanel create(){
        //Create panel and set background colour to white
        JPanel pnlDisplay = new JPanel();
        pnlDisplay.setBackground(Color.WHITE);
        getContentPane().add(pnlDisplay,BorderLayout.CENTER);
        repaint();

        //Set grid bag layout constraints and add to panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.NONE;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        gridBagConstraints.ipadx = 50;
        gridBagConstraints.ipady = 50;
        gridBagConstraints.gridwidth=2;
        gridBagConstraints.gridheight=3;
        pnlDisplay.setLayout(gridBagLayout);

        //add view asset button with action listener
        button_viewAsset = new JButton( "<html><center>View Assets<br><br>View History (via Above)</center></html>");
        add(button_viewAsset,0,1,pnlDisplay,gridBagConstraints);
        button_viewAsset.addActionListener(this);

        //add active offers button with action listener
        button_ActiveOffers = new JButton( "View Active Offers");
        add(button_ActiveOffers,0,4,pnlDisplay,gridBagConstraints);
        button_ActiveOffers.addActionListener(this);

        //add new offers button with action listener
        button_NewOffer = new JButton("New Offer");
        add(button_NewOffer, 0, 7, pnlDisplay, gridBagConstraints);
        button_NewOffer.addActionListener(this);

        //add reset password button with action listener
        button_ResetPassword = new JButton( "Reset Password");
        add(button_ResetPassword,0,10,pnlDisplay,gridBagConstraints);
        button_ResetPassword.addActionListener(this);

        //add add user button with action listener if user authorised
        button_AddUser = new JButton( "Add User");
        add(button_AddUser,2,1, pnlDisplay,gridBagConstraints);
        button_AddUser.addActionListener(this);
        button_AddUser.setVisible(Main.authorised);

        //add manage units button with action listener if user authorised
        button_ManageUnits = new JButton("Manage Unit");
        add(button_ManageUnits, 2, 4, pnlDisplay, gridBagConstraints);
        button_ManageUnits.addActionListener(this);
        button_ManageUnits.setVisible(Main.authorised);

        //add new unit button with action listener if user authorised
        button_NewUnit = new JButton("New Unit");
        add(button_NewUnit, 2, 7, pnlDisplay, gridBagConstraints);
        button_NewUnit.addActionListener(this);
        button_NewUnit.setVisible(Main.authorised);

        //add new asset button with action listener if user authorised
        button_NewAsset = new JButton("New Asset");
        add(button_NewAsset, 2, 10, pnlDisplay, gridBagConstraints);
        button_NewAsset.addActionListener(this);
        button_NewAsset.setVisible(Main.authorised);
        //return the panel
        return pnlDisplay;
    }//end create

    /**
     * A helper method to add component to panel
     * @param component the component to be added to panel
     * @param x the x co-ordinate of the location
     * @param y the desired y-co-ordinate
     * @param jPanel - the jPanel to add the component to
     * @param gridBagConstraints - the constraints of the layout
     */
    private void add(Component component, int x, int y, JPanel jPanel, GridBagConstraints gridBagConstraints){
        gridBagConstraints.gridx=x;
        gridBagConstraints.gridy=y;
        component.setPreferredSize(new Dimension(130, 30));
        jPanel.add(component, gridBagConstraints);
    }//end add

    /**
     * Override action performed to switch pages based on event type
     * @param e the button pressed
     */
    @Override
    public void actionPerformed(ActionEvent e){
        if(e.getSource()==button_viewAsset){
            try {
                Main.changePanel("View Assets");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if(e.getSource()==button_AddUser){
            try {
                Main.changePanel("Add User");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if(e.getSource()==button_ManageUnits){
            try {
                Main.changePanel("Manage Units");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if(e.getSource()==button_NewUnit){
            try {
                Main.changePanel("Add Unit");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if(e.getSource()==button_NewAsset){
            try {
                Main.changePanel("Add Asset");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if(e.getSource()==button_ActiveOffers){
            try {
                Main.changePanel("View Active Offers");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if(e.getSource()== button_NewOffer){
            try {
                Main.changePanel("New Offer");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        else if(e.getSource()== button_ResetPassword){
            try {
                Main.changePanel("Reset Password");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }//end actionPerformed
}//end class
