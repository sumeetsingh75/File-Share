/**
 * SearchResponse class to aggregate search results from all peers.
 */
public class SearchResponse {
    String name;
    long size;
    String IP;

    SearchResponse(String name, long size, String IP) {
        this.name = name;
        this.size = size;
        this.IP = IP;
    }
}