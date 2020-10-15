/**
 * FileType class to store search results for any particular client.
 */
public class FileType {
    String name;
    Long size;

    FileType(String name, long size) {
        this.name = name;
        this.size = size;
    }
}