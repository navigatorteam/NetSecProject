package navigatorteam.cryptoproxy;

import java.math.BigInteger;

public interface AsymmetricKey {

    BigInteger getModulus();
    BigInteger getExponent();

}
