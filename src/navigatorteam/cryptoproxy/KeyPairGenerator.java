package navigatorteam.cryptoproxy;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Created on 2019-07-25.
 */
public class KeyPairGenerator {

    private AsymmetricKey publicKey = null;
    private AsymmetricKey privateKey = null;

    private int bigIntegerLength = 512;

    private BigInteger p;
    private BigInteger q;
    private BigInteger n;
    private BigInteger phiN;
    private BigInteger e;
    private BigInteger d;

    public void generateKeys() {
        generateRandomPrimes();
        generateN();
        generateE();
        generateD();
        publicKey = new RSAKey(e, n);
        privateKey = new RSAKey(d, n);
    }


    public AsymmetricKey getPublicKey() {
        return publicKey;
    }


    public AsymmetricKey getPrivateKey() {
        return privateKey;
    }

    private void generateRandomPrimes()
    {
        Random randomNumber = new SecureRandom();
        this.p = BigInteger.probablePrime(bigIntegerLength, randomNumber);
        this.q = BigInteger.probablePrime(bigIntegerLength, randomNumber);
    }

    private void generateN() {
        this.n = p.multiply(q);
        generatePhiN();
    }


    private void generatePhiN() {
        this.phiN = p.subtract(BigInteger.valueOf(1)).multiply(q.subtract(BigInteger.valueOf(1)));
    }

    private void generateE() {
        Random randomNumber = new SecureRandom();
        BigInteger probableE;
        byte[] generatedRandom = new byte[bigIntegerLength/8];
        do {
            randomNumber.nextBytes(generatedRandom);
            probableE = new BigInteger(1, generatedRandom);
        }while((probableE.equals(BigInteger.valueOf(1))) || (phiN.compareTo(probableE) < 1) || (!(phiN.gcd(probableE).equals(BigInteger.valueOf(1)))));
        this.e = probableE;
    }

    private void generateD() {
        this.d = e.modInverse(phiN);
    }
}
