
/**
 * File contains Client class (main) that creates a socket connection with server.
 * Client class constitutes Miniserver (inner) class to cater requests from other peers.
 * Client class constitutes MiniClient (inner) class to consume services from other peers.
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class Client implements Runnable {
    final String SERVER_ADDR = ""; /* Server's IP Address */
    final int SERVER_PORT = 8000; /* Server's Port Number */
    Socket sock;
    BufferedReader clientBr;
    DataOutputStream clientDos;
    ArrayList<String> peersIPList = new ArrayList<>(); /* List of all connected peers. */
    ArrayList<SearchResponse> searchResults = new ArrayList<>(); /* List of Search Results aggregated from all peers. */
    ClientUtils utils = new ClientUtils(); /* Utility class object */

    /**
     * Creates a socket connection with server and instantiates its input/output
     * streams. Requests Peers list from server. Creates new thread and starts
     * miniserver on it.
     */
    Client() {
        try {
            sock = new Socket(SERVER_ADDR, SERVER_PORT);
            clientBr = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            clientDos = new DataOutputStream(sock.getOutputStream());
            getPeers(); /* Retrieve peers list from server. */
            utils.connectedToServerNotification();
            new Thread(new MiniServer()).start(); /* Starts Mini Server */
        } catch (Exception e) {
            utils.connectionFailedNotification();
        }
    }

    /**
     * Client class implemented run() method that listens to server to get list of
     * other peers connected.
     */
    public void run() {
        try {
            while (true) {
                String responseString = clientBr.readLine();
                if (responseString.matches(ClientConstants.SEND_LIST_RESPONSE)) {
                    String sizeString = clientBr.readLine();
                    int size = Integer.parseInt(sizeString);
                    for (int i = 0; i < size; i++) {
                        String p = clientBr.readLine();
                        if (p.compareTo(sock.getLocalAddress().getHostAddress()) != 0) {
                            peersIPList.add(p); /* Maintains list of IP adresses of all peers. */
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(ClientConstants.READ_ERROR);
        }
    }

    /**
     * Displays menu and prompts user to enter choice. Allows user to view list of
     * connected peers or search for particular files.
     */
    void mainMenu() {
        Scanner sc = new Scanner(System.in);
        int input = 0;
        do {
            utils.printMenu();
            input = sc.nextInt();
            switch (input) {
                case 1:
                    utils.printPeersList(peersIPList);
                    mainMenu();
                    break;

                case 2:
                    search();
                    break;

                default:
                    System.out.println(ClientConstants.VALID_CHOICE_ERROR);
                    break;
            }
        } while (input < 1 || input > 2);
        sc.close();
    }

    /**
     * Request server to send list of all connected peers.
     */
    void getPeers() {
        try {
            clientDos.writeBytes(ClientConstants.SEND_LIST_REQUEST); /* initiates request to send list of all peers */
            clientDos.flush();
            peersIPList.clear();
        } catch (Exception e) {
            System.out.println(ClientConstants.WRITE_ERROR);
        }
    }

    /**
     * Prompts user to enter search keywords. Miniclient creates socket
     * connection with all peers one by one and initiates search request.
     */
    void search() {
        System.out.print(ClientConstants.ENTER_KEYWORDS);
        Scanner sc = new Scanner(System.in);
        String searchKeywords = sc.nextLine();
        searchResults.clear();
        for (int i = 0; i < peersIPList.size(); i++) { /* iterates over list of all peers */
            MiniClient mc = new MiniClient(peersIPList.get(i)); /* establishes connection with peer */
            try {
                mc.miniClientDos.writeBytes(ClientConstants.SEARCH_REQUEST + "\r\n"); /* initiate search request */
                mc.miniClientDos.writeBytes(searchKeywords + "\r\n"); /* sends search keyword to peer */
                mc.thread.join();
            } catch (Exception e) {
                System.out.println(ClientConstants.WRITE_ERROR);
            }
        }
        if (searchResults.size() == 0) {
            System.out.println(ClientConstants.NO_RESULTS);
            mainMenu();
        } else {
            utils.printSearchResponse(searchResults);
            postSearchflow();
        }
        sc.close();
    }

    /**
     * Determine if user wants to download any file.
     */
    void postSearchflow() {
        Scanner sc = new Scanner(System.in);
        String input = "";
        do {
            System.out.println(ClientConstants.DOWNLOAD_PERMISSION);
            input = sc.nextLine();
            switch (input) {
                case ClientConstants.YES:
                    download();
                    break;

                case ClientConstants.NO:
                    mainMenu();
                    break;

                default:
                    System.out.println(ClientConstants.VALID_CHOICE_ERROR);
            }
        } while (!(input.matches(ClientConstants.YES) || input.matches(ClientConstants.NO)));
        sc.close();
    }

    /**
     * Prompts user to select file to download. Establishes connection with file
     * owner and initiates download request.
     */
    void download() {
        Scanner sc = new Scanner(System.in);
        int fileIndex = 0;
        do {
            utils.downloadMenu();
            fileIndex = sc.nextInt();
        } while (fileIndex < 1 || fileIndex > searchResults.size());
        sc.close();
        MiniClient mc = new MiniClient(searchResults.get(fileIndex - 1).IP); /* creates connection with peer. */
        try {
            mc.miniClientDos.writeBytes(ClientConstants.DOWNLOAD_REQUEST + "\r\n"); /* initiates download request */
            mc.miniClientDos.writeBytes(searchResults.get(fileIndex - 1).name + "\r\n"); /* send's file name to peer */
        } catch (Exception e) {
            System.out.println(ClientConstants.WRITE_ERROR);
        }
    }

    /**
     * @param args 
     * Creates new thread for client and displays menu to user.
     */
    public static void main(String args[]) {
        Client clientobj = new Client();
        new Thread(clientobj).start();
        clientobj.mainMenu();
    }

    /**
     * Inner class to cater requests of other peers.
     */
    class MiniServer implements Runnable {
        ServerSocket miniServerSock;
        final int MINI_SERVER_PORT = 8100; /* Port number for server of this client. */

        /**
         * Creates new server socket on Server Port for client.
         */
        MiniServer() {
            try {
                miniServerSock = new ServerSocket(MINI_SERVER_PORT);
            } catch (Exception e) {
                System.out.println(ClientConstants.CLIENT_SERVER_FAILED);
            }
        }

        /**
         * MiniServer class implemented run() method that listens to peers connections. Creates
         * a new request processor(MiniClientHandler) for each new connection from peer. Initiates a new
         * thread for every request processor.
         */
        public void run() {
            try {
                while (true) {
                    Socket sock = miniServerSock.accept();
                    new Thread(new MiniClientHandler(sock)).start();
                }
            } catch (Exception e) {
                System.out.println(ClientConstants.CONNECTION_ERROR);
            }
        }

        /**
         * Request processor of every peer.
         */
        class MiniClientHandler implements Runnable {
            Socket miniClientHandlerSock;
            DataOutputStream miniClientHandlerDos;
            BufferedReader miniClientHandlerBr;
            final String folderPath = "C:\\Users\\Public\\Shared"; /* Search Repository system path */
            final String filePath = "C:\\Users\\Public\\Shared\\"; /* Files path in search Repository */
            ArrayList<FileType> filesList = new ArrayList<>(); /* List of search results for this client */

            /**
             * @param sock 
             * Handles socket created by Mini server and instantiates its
             * input/output streams.
             */
            MiniClientHandler(Socket sock) {
                miniClientHandlerSock = sock;
                try {
                    miniClientHandlerBr = new BufferedReader(
                            new InputStreamReader(miniClientHandlerSock.getInputStream()));
                    miniClientHandlerDos = new DataOutputStream(miniClientHandlerSock.getOutputStream());
                } catch (Exception e) {
                    System.out.println(ClientConstants.IO_STREAM_ERROR);
                }
            }

            /**
             * MiniClientHandler class implemented run() method. Listens to requests
             * (Search/Download files) of peers.
             */
            public void run() {
                try {
                    String request = miniClientHandlerBr.readLine();
                    if (request.matches(ClientConstants.SEARCH_REQUEST))
                        processSearchRequest();
                    else if (request.matches(ClientConstants.DOWNLOAD_REQUEST))
                        processDownloadRequest();
                } catch (Exception e) {
                    System.out.println(ClientConstants.READ_ERROR);
                }
            }

            /**
             * Searches for files at given path. Maintaines list of names and size of
             * files that matches search keywords. Sends search response back to client.
             */
            void processSearchRequest() {
                try {
                    String searchString = miniClientHandlerBr.readLine(); /* Search Keywords */
                    String localFilesList[] = new File(folderPath).list();
                    for (int i = 0; i < localFilesList.length; i++) {
                        if (localFilesList[i].startsWith(searchString)) {
                            File file = new File(filePath + localFilesList[i]);
                            FileType fileobj = new FileType(file.getName(), file.length());
                            filesList.add(fileobj); /* list of files that starts with search keywords. */
                        }
                    }
                    miniClientHandlerDos.writeBytes(ClientConstants.SEARCH_RESPONSE + "\r\n");
                    miniClientHandlerDos.writeBytes(filesList.size() + "\r\n");
                    for (int i = 0; i < filesList.size(); i++) {
                        miniClientHandlerDos.writeBytes(filesList.get(i).name + "\r\n");
                        miniClientHandlerDos.writeBytes(filesList.get(i).size + "\r\n");
                    }
                } catch (Exception e) {
                    System.out.println(ClientConstants.READ_WRITE_ERROR);
                }
            }

            /**
             * Process download request for specified file. Reads specified file at
             * given path as bytes and writes over output stream of client.
             */
            void processDownloadRequest() {
                try {
                    String fileName = miniClientHandlerBr.readLine(); /* file to read */
                    miniClientHandlerDos.writeBytes(ClientConstants.DOWNLOAD_RESPONSE + "\r\n");
                    miniClientHandlerDos.writeBytes(fileName + "\r\n");
                    byte buffer[] = new byte[10000];
                    FileInputStream fis = new FileInputStream(filePath + fileName);
                    while (true) {
                        int bytesRead = fis.read(buffer, 0, 10000); /* reads file from local system */
                        if (bytesRead == -1) {
                            fis.close();
                            miniClientHandlerDos.close();
                            miniClientHandlerSock.close();
                            break;
                        }
                        miniClientHandlerDos.write(buffer, 0,
                                bytesRead); /* writes file on output stream of socket */
                    }
                } catch (Exception e) {
                    System.out.println(ClientConstants.READ_WRITE_ERROR);
                }
            }
        }
    }

    /**
     * Inner class to consume services from other peers.
     */
    class MiniClient implements Runnable {
        final int PEER_SERVER_PORT = 8100; /* Port for server process of peer */
        final String downloadFilePath = "c:\\Users\\Public\\shared\\"; /* system path to save downloaded file */
        Socket miniClientSock;
        DataOutputStream miniClientDos;
        DataInputStream miniClientDis;
        BufferedReader miniClientBr;
        Thread thread;

        /**
         * @param peerIP 
         * Makes a socket connection with peer and instantiates its
         * input/output streams. Runs connection on separate thread.
         */
        MiniClient(String peerIP) {
            try {
                miniClientSock = new Socket(peerIP, PEER_SERVER_PORT);
                miniClientBr = new BufferedReader(new InputStreamReader(miniClientSock.getInputStream()));
                miniClientDos = new DataOutputStream(miniClientSock.getOutputStream());
                miniClientDis = new DataInputStream(miniClientSock.getInputStream());
                thread = new Thread(this);
                thread.start();
            } catch (Exception e) {
                System.out.println(ClientConstants.PEER_CONNECTION_ERROR);
            }
        }

        /**
         * MiniClient class implemented run() method. Listens to response sent by any
         * peer.
         */
        public void run() {
            try {
                String responseString = miniClientBr.readLine();
                if (responseString.matches(ClientConstants.SEARCH_RESPONSE))
                    processSearchResponse();
                else if (responseString.matches(ClientConstants.DOWNLOAD_RESPONSE))
                    processDownloadResponse();
            } catch (Exception e) {
                System.out.println(ClientConstants.READ_ERROR);
            }
        }

        /**
         * Processes response sent by peer's server for search request. Aggregates
         * search results from all peers in a list.
         */
        public void processSearchResponse() {
            try {
                int searchResultsSize = Integer.parseInt(miniClientBr.readLine());
                for (int i = 0; i < searchResultsSize; i++) {
                    String filename = miniClientBr.readLine();
                    Long filesize = Long.parseLong(miniClientBr.readLine());
                    SearchResponse obj = new SearchResponse(filename, filesize,
                            miniClientSock.getInetAddress().getHostAddress());
                    searchResults.add(obj); /* List of search results */
                }
            } catch (Exception e) {
                System.out.println(ClientConstants.READ_ERROR);
            }
        }

        /**
         * Processes response sent by peer's server for download request. Reads
         * file over input stream and stores file at specified path.
         */
        public void processDownloadResponse() {
            try {
                String filename = miniClientBr.readLine();
                FileOutputStream fos = new FileOutputStream(downloadFilePath + filename);
                byte buffer[] = new byte[10000];
                System.out.println(ClientConstants.DOWNLOAD_STARTED);
                while (true) {
                    int bytesRead = miniClientDis.read(buffer, 0, 10000);
                    if (bytesRead == -1)
                        break;
                    fos.write(buffer, 0, bytesRead);
                }
                System.out.println(ClientConstants.DOWNLOAD_FINISHED);
                fos.close();
                mainMenu();
            } catch (Exception e) {
                System.out.println(ClientConstants.READ_ERROR);
            }
        }
    }
}
