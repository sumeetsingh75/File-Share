import java.net.*;
import java.util.*;
import java.io.*;

public class Server implements Runnable {
    ServerSocket sersock;  
    final int SERVER_PORT = 8000;  /* Server port */
    ArrayList<ClientHandler> clientsList = new ArrayList<>();  /*List of clients connected to server */
    ServerUtils util = new ServerUtils();  /* Utility class object */

    /**
     * Creates new server socket on Server Port.
     */
    Server() {
        try {
            sersock = new ServerSocket(SERVER_PORT);
            util.serverUpNotification();
        } catch (Exception e) {
            e.printStackTrace();
            util.serverDownNotification();
        }
    }

    /**
     * Server's class implemented run() method that listens to any new client.
     * Creates a new request processor for every new client.
     * Initiates a new thread for every request processor.
     * Maintaines a list of all connected clients.
     */
    public void run() {
        while (true) {
            try {
                Socket sock = sersock.accept();
                ClientHandler requestProcessor = new ClientHandler(sock);
                clientsList.add(requestProcessor);
                new Thread(requestProcessor).start();
            } catch (Exception e) {
                System.out.println(ServerConstants.CONNECTION_ERROR);
            }
        }
    }

    /**
     * @param args
     * Creates a new Thread for Server.
     */
    public static void main(String args[]) {
        new Thread(new Server()).start();
    }

    /**
     * Inner class for request processor of every client.
     */
    public class ClientHandler implements Runnable {
        Socket clientHandlersock;
        BufferedReader clientHandlerbr;
        DataOutputStream clientHandlerDos;

        /**
         * @param sock
         * Handles socket created by server.
         */
        ClientHandler(Socket sock) {
            this.clientHandlersock = sock;
        }

        /**
         * ClientHandler class implemented run() method.
         * Sends list of all clients connected to server to client requesting for it. 
         */
        public void run() {
            try {
                clientHandlerbr = new BufferedReader(new InputStreamReader(clientHandlersock.getInputStream()));
                clientHandlerDos = new DataOutputStream(clientHandlersock.getOutputStream());
                while (true) {
                    String request = clientHandlerbr.readLine();
                    if (request.matches(ServerConstants.SEND_LIST_REQUEST)) {
                        clientHandlerDos.writeBytes(ServerConstants.SEND_LIST_RESPONSE);
                        clientHandlerDos.flush();
                        clientHandlerDos.writeBytes(clientsList.size() + "\r\n");
                        clientHandlerDos.flush();

                        for (int i = 0; i < clientsList.size(); i++) {
                            clientHandlerDos.writeBytes(
                                    clientsList.get(i).clientHandlersock.getInetAddress().getHostAddress() + "\r\n");
                        }
                    }
                }
            } catch (Exception e) {
                clientsList.remove(this); /* Removes client that disconnects from server from list. */
            }
        }
    }
}
