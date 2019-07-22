package navigatorteam.cryptoproxy;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

public class CryptoServiceImplementation implements CryptoServiceProvider {

    private AsymmetricKey publicKey;
    private AsymmetricKey privateKey;
    private AsymmetricKey otherEntityPublicKey;
    //TODO SymmetricKey

    private BigInteger p;
    private BigInteger q;
    private BigInteger n;
    private BigInteger phiN;
    private BigInteger e;
    private BigInteger d;

    private int bigIntegerLength = 512;

    @Override
    public String encrypt(String input) {
        BigInteger plainTextBigInteger = new BigInteger(1, input.getBytes());
        return new String(plainTextBigInteger.modPow(otherEntityPublicKey.getExponent(), otherEntityPublicKey.getModulus()).toByteArray());
    }

    @Override
    public String decrypt(String input) {
        BigInteger cipherTextBigInteger = new BigInteger(1, input.getBytes());
        return new String(cipherTextBigInteger.modPow(privateKey.getExponent(), privateKey.getModulus()).toByteArray());
    }

    @Override
    public void generateKeys() {
        generateRandomPrimes();
        generateN();
        generateE();
        generateD();
        publicKey = new RSAKey(e, n);
        privateKey = new RSAKey(d, n);
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

    public BigInteger encrypt(BigInteger text) {
        return text.modPow(e, n);
    }

    public BigInteger decrypt(BigInteger cipher) {
        return cipher.modPow(d, n);
    }
}
