package navigatorteam.cryptoproxy;

import java.math.BigInteger;

public class AsymmetricKey {

    private BigInteger exponent;
    private BigInteger modulus;

    public AsymmetricKey(BigInteger e, BigInteger m)
    {
        exponent = e;
        modulus = m;
    }

    public BigInteger getModulus() {
        return modulus;
    }

    public BigInteger getExponent() {
        return exponent;
    }
}
