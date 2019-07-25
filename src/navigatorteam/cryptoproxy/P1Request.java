package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-25.
 */
public class P1Request {
    String token;
    String encryptedMessage;

    public P1Request(String token, String encryptedMessage) {
        this.token = token;
        this.encryptedMessage = encryptedMessage;
    }

    public String getToken() {
        return token;
    }

    public String getEncryptedMessage() {
        return encryptedMessage;
    }
}
