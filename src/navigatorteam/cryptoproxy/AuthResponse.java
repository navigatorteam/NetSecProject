package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-25.
 */
public class AuthResponse {
    private String encryptedChosenToken;
    private AsymmetricKey p2PublicKey;

    public AuthResponse(String encryptedChosenToken, AsymmetricKey p2PublicKey) {
        this.encryptedChosenToken = encryptedChosenToken;
        this.p2PublicKey = p2PublicKey;
    }


    public String getEncryptedChosenToken() {
        return encryptedChosenToken;
    }

    public AsymmetricKey getP2PublicKey() {
        return p2PublicKey;
    }
}
