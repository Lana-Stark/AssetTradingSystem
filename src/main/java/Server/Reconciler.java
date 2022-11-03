package Server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A class which creates a thread that periodically runs the reconciler.
 */
public class Reconciler implements Runnable {

    private final DBConnection connection;

    public Reconciler(DBConnection connection){
        this.connection=connection;
    }

    // prepared statements for getting offers

    // suggested in https://stackoverflow.com/questions/54394042/java-how-to-avoid-using-thread-sleep-in-a-loop
    // to do this as a ScheduledExecutorService rather than using thread.sleep
    /**
     * Overrides the existing run function for runnable.
     * Every 5 seconds, runs the reconciler function.
     */
    @Override
    public void run() {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            //noinspection StatementWithEmptyBody
            while(connection.Reconcile());
        }, 0, 5000, TimeUnit.MILLISECONDS);
    }
}
