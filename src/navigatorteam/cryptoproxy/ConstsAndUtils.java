package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-22.
 */
public class ConstsAndUtils {
    static final int P1Port = 6789;
    static final String P21Host = "127.0.0.1";
    static final int P21Port = 6790;
    static final String P22Host = "127.0.0.1";
    static final int P22Port = 6791;



    private static int idCounter = 0;

    public static int nextID(){
        int next = idCounter;
        idCounter = (idCounter + 1) % Integer.MAX_VALUE;
        return next;
    }
}
