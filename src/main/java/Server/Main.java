package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Properties;
import java.util.Scanner;

/**
 * The Main class for the server side of the program.
 */
public class Main {

    /**
     * Main Server function.
     * Creates the Socket which clients connect into and returns a response HashMap to clients.
     *
     * @param args  unused args
     */
    public static void main(String[] args) {
        // import configuration etc
        Properties props = new Properties();
        try {
            InputStream configFile = new FileInputStream(Paths.get(System.getProperty("user.dir"),"server.conf").toAbsolutePath().toString());

            props.load(configFile);
            if(!(props.containsKey("jdbc.ip") & props.containsKey("jdbc.username") & props.containsKey("jdbc.password") &
                    props.containsKey("serverPort") & props.containsKey("jdbc.schema") & props.containsKey("jdbc.port"))){
                throw new SQLException("Invalid server/sql config file");
            }
        } catch (FileNotFoundException | SQLException e) {
            // file does not exist
            System.out.println("Error: missing/invalid configuration file provided");
            // request user input to construct a configuration file - then create the file
            Scanner scanner = new Scanner(System.in);
            // server port
            System.out.println("Please provide server port to start server on:");
            int serverPort = scanner.nextInt();
            props.setProperty("serverPort", String.valueOf(serverPort));

            // jdbc ip:
            System.out.println("Please provide java database ip. E.g. 'localhost'");
            String jdbc_ip = scanner.next();
            props.setProperty("jdbc.ip", jdbc_ip);

            // jdbc port
            System.out.println("Please provide java database port. E.g. 3306");
            Integer jdbc_port = scanner.nextInt();
            props.setProperty("jdbc.port", String.valueOf(jdbc_port));

            // jdbc username
            System.out.println("Please provide java username username. E.g. 'root'");
            String jdbc_username = scanner.next();
            props.setProperty("jdbc.username", jdbc_username);

            // jdbc password
            System.out.println("Please provide java database password. E.g. 'wh!teMonkey22'");
            String jdbc_password = scanner.next();
            props.setProperty("jdbc.password", jdbc_password);

            // jdbc schema
            System.out.println("Please provide java database schema/name. E.g. 'cab302_groupof4_028'");
            String jdbc_schema = scanner.next();
            props.setProperty("jdbc.schema", jdbc_schema);

            try {
                props.store(new FileOutputStream(Paths.get(System.getProperty("user.dir"),"server.conf").toAbsolutePath().toString()),null);
            } catch (IOException ioException) {
                System.out.println("Error: Unable to save config file to " + Paths.get(System.getProperty("user.dir"),"server.conf. ").toAbsolutePath() + "The specified configuration will only be used for this run");
            }
        } catch (IOException e) {
            // unable to open/read config file
            System.out.println("Error: Unable to start the server as the file config file is unable to be read. Please ensure no other programs are accessing the config file and start the server again.");
        }


        // login to SQL server
        EventHandler eventHandler = null;
        try{
            eventHandler = new EventHandler(props);
        } catch (SQLException e) {
            // couldn't connect to server
            System.out.println("Error: Unable to connect to the specified server. This program will now terminate");
            System.exit(0);
        }

        //Define the server port
        int serverPort=Integer.parseInt(props.getProperty("serverPort"));
        System.out.println("Starting a server on port "+serverPort);
        ServerSocket serverSocket;
        try {
            //Listen on the specified port
            serverSocket = new ServerSocket(serverPort);
            //Repeat until the server is stopped
            for(;;) {
                //If there is a request, accept the request on a new socket
                Socket socket = serverSocket.accept();

                //Read the file
                ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

                HashMap<String,Object> receivedRequest = null;
                HashMap<String,Object> response;
                try {
                    //Try parse the response as a HashMap
                    receivedRequest = (HashMap<String,Object>) objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    // invalid response
                }

                //If the request is parsed successfully as a HashMap, process the request
                if(receivedRequest!=null) {
                    System.out.println("Request received");
                    try {
                        response=eventHandler.processRequest(receivedRequest);
                    } catch (SQLException e) {
                        response=new HashMap<>();
                        response.put("Status",400);
                        response.put("Error", "Bad Request");
                    }
                    //Otherwise, the request was not a HashMap. Return failure
                } else {
                    response=new HashMap<>();
                    response.put("Status",400);
                    response.put("Error", "Bad Request");
                }

                // compose the Object into a stream and send
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                objectOutputStream.writeObject(response);
                objectOutputStream.flush();
                //Close the streams and socket
                objectOutputStream.close();
                objectInputStream.close();
                socket.close();
            }
        } catch (IOException e) {
            //Something went wrong starting the server. Print the error
            System.out.println("Error: Failed to start server on port "+serverPort);
        }
    }
}
