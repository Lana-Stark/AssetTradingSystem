package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileWriter;
import java.io.IOException;

import static Client.Main.init;

/**
 * Page_GetFile class creates a jPanel for requesting information on how to communicate with the server
 */
public class Page_GetFile extends JPanel implements ActionListener {
    private JPanel jPanel;
    JButton create;
    JTextField ipAddress;
    JTextField port;

    /**
     * Constructor for class
     */
    public JPanel create() {
        // import configuration etc
        // file does not exist
        // request user input to construct a configuration file - then create the file
        //Define layout and add to panel
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.CENTER;
        gridBagConstraints.weightx = 100;
        gridBagConstraints.weighty = 100;
        jPanel = new JPanel();
        jPanel.setLayout(gridBagLayout);
        add(new JLabel("IP Address:"), 0, 0, 1, jPanel, gridBagConstraints);
        ipAddress = new JTextField(20);
        add(ipAddress, 2, 0, 1, jPanel, gridBagConstraints);
        add(new JLabel("Port:"), 0, 1, 1, jPanel, gridBagConstraints);
        port = new JTextField(20);
        add(port, 2, 1, 2, jPanel, gridBagConstraints);
        create = new JButton("Done");
        create.addActionListener(this);
        add(create, 0, 3, 1, jPanel, gridBagConstraints);
        return jPanel;
    }

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
        if(e.getSource() == create){
            if(!(ipAddress.getText().isEmpty() || port.getText().isEmpty())) {
                try {
                    FileWriter myWriter = new FileWriter("client.conf");
                    myWriter.write("serverAddress=" + ipAddress.getText() + "\n");
                    myWriter.write("port=" + port.getText());
                    myWriter.close();
                    init();
                    Main.changePanel("Login");
                } catch (IOException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(jPanel, "Please provide a value");
            }
        }
    }
}
