
/**
 * ClientUtils class contains utility methods used by Client Class.
 */
import java.util.*;

public class ClientUtils {
    public void connectedToServerNotification() {
        System.out.println("####################################");
        System.out.println("***** Connected To Server *****");
        System.out.println("####################################");
    }

    public void connectionFailedNotification() {
        System.out.println("************************************");
        System.out.println("******** Connection Failed! ********");
        System.out.println("************************************");
    }

    public void printMenu() {
        System.out.println("\nXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX\n");
        System.out.println("What would you like to do ?");
        System.out.println("Enter Your Choice:");
        System.out.println("1 -> View Your Peers:");
        System.out.println("2 -> Search Files from Peers:");
    }

    public void printPeersList(ArrayList<String> peersIPList) {
        System.out.println("****************************");
        System.out.println("SNo.\tIP Address");
        for (int i = 0; i < peersIPList.size(); i++) {
            System.out.println(i + 1 + ".\t" + peersIPList.get(i));
        }
        System.out.println("****************************");
    }

    public void downloadMenu() {
        System.out.println("\n---------------------------------------------");
        System.out.print("Which File you want to Download ?");
    }

    public void printSearchResponse(ArrayList<SearchResponse> searchResults) {
        System.out.println("SNO.\tPeer IP\t\tFile size(MB)\t\tFile Name");
        for (int i = 0; i < searchResults.size(); i++) {
            System.out.println((i + 1) + "\t" + searchResults.get(i).IP + "\t"
                    + searchResults.get(i).size / (1024 * 1024) + "\t\t\t" + searchResults.get(i).name);
        }
    }

}