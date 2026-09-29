/**
 * Kastas när en transaktion är ogiltig
 */
public class InvalidTransactionException extends Exception {

    public InvalidTransactionException(String meddelande) {
        super(meddelande);
    }
}