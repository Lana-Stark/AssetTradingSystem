package Client;

import javax.swing.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Thread to periodically check back against the server for any updates
 */
public class UpdateChecker implements Runnable {
    private final ServerConnector serverConnector;
    private final JPanel jPanel;

    /**
     * Creates new instance of updateChecker.
     *
     * @param serverConnector   existing serverConnector
     */
    public UpdateChecker(ServerConnector serverConnector, JPanel jPanel) {
        this.jPanel=jPanel;
        this.serverConnector=serverConnector;
    }

    // prepared statements for getting offers
    /**
     * Overrides the existing run function for runnable.
     * Every 5 seconds, runs the checker function.
     */
    @Override
    public void run() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            System.out.println("Checking if logged in");
            if(Main.loggedIn) {
                System.out.println("Checking for updates....");
                try {
                    HashMap<String, Object> result = serverConnector.getUpdateLogs(Main.sessionId);
                    HashMap<LocalDateTime,HashMap<String,Object>> updates = (HashMap<LocalDateTime,HashMap<String,Object>>)result.get("updates");
                    String notificationText="";
                    for (Map.Entry<LocalDateTime,HashMap<String,Object>> entry : updates.entrySet()) {
                        LocalDateTime dateTime = entry.getKey();
                        HashMap<String,Object> data = entry.getValue();
                        String unitName = (String)data.get("unitName");
                        String yourAction = (String)data.get("yourAction");
                        String assetName = (String)data.get("assetName");
                        Integer credits = (Integer)data.get("credits");
                        Integer quantity = (Integer)data.get("quantity");
                        notificationText = notificationText.concat(yourAction+" "+quantity+"x "+assetName+" @ "+credits+"credit(s) to "+unitName+"\r\n");
                    }
                    if(!notificationText.isBlank()) {
                        JOptionPane.showMessageDialog(jPanel, notificationText,"Trade Reconciled", JOptionPane.INFORMATION_MESSAGE);
                        System.out.println(notificationText);
                    }
                } catch (IOException e) {
                    System.out.println("Failed to get updates");
                }
            }
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }
}
