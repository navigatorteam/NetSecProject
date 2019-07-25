package navigatorteam.cryptoproxy;

/**
 * Created on 2019-07-25.
 */
public class AuthRequest {
    private AsymmetricKey p1PublicKey;

    public AuthRequest(AsymmetricKey p1PublicKey) {
        this.p1PublicKey = p1PublicKey;
    }

    public AsymmetricKey getP1PublicKey() {
        return p1PublicKey;
    }
}
