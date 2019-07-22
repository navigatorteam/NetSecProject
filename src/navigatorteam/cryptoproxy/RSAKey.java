package navigatorteam.cryptoproxy;

import java.math.BigInteger;

public class RSAKey implements AsymmetricKey {

    private BigInteger exponent;
    private BigInteger modulus;

    public RSAKey(BigInteger e, BigInteger m)
    {
        exponent = e;
        modulus = m;
    }

    @Override
    public BigInteger getModulus() {
        return modulus;
    }

    @Override
    public BigInteger getExponent() {
        return exponent;
    }
}
