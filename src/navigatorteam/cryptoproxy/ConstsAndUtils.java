package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-22.
 */
public class ConstsAndUtils {
    public static final int P1Port = 6789;
    public static final String P2Host = "127.0.0.1";
    public static final int P2Port = 6790;

    public static final boolean PLAINTEXT_MODE = false;
    public static final boolean INTEGRITY_CHECK = true;
    public static final boolean DEBUG_CLOSE_ON_FAIL = false;

    private static int idCounter = 0;

    public synchronized static int nextID(){
        int next = idCounter;
        idCounter = (idCounter + 1) % Integer.MAX_VALUE;
        return next;
    }
}
