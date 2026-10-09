/**
 * FileFormatException skapad (checked) för trasiga rader i CSV-filen.
 * Om filformatet bråkar
 */

public class FileFormatException extends Exception {
    public FileFormatException(String meddelande) {
        super(meddelande);
    }
}
