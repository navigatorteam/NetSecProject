package navigatorteam.cryptoproxy;

import java.math.BigInteger;

public class SharedKey implements SymmetricKey {

    private String key;

    public SharedKey(String k)
    {
        key = k;
    }

    @Override
    public String getKey() {
        return key;
    }

}
