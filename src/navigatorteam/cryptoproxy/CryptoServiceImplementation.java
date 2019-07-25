package navigatorteam.cryptoproxy;



import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

public class CryptoServiceImplementation extends  CryptoService implements CryptoServiceProvider {


    public CryptoServiceImplementation(AsymmetricKey privateKey, AsymmetricKey publicKey, AsymmetricKey otherEntityPublicKey) {
        super(privateKey, publicKey, otherEntityPublicKey);
    }


    @Override
    public String encrypt(String input) {

        SymmetricKey sharedKey = new SharedKey(generateSharedKey());

        byte[] inputInBytes = input.getBytes(StandardCharsets.UTF_8);

        byte[] encryptedRequest = encryptSymmetric(inputInBytes, sharedKey, "Encrypt");

        BigInteger keyBigInteger = new BigInteger(1, sharedKey.getKey().getBytes());
        byte[] encryptedKey = keyBigInteger.modPow(otherEntityPublicKey.getExponent(), otherEntityPublicKey.getModulus()).toByteArray();


        //Signature
        byte[] digitalSignature = generateHash(inputInBytes);
        byte[] digitalSignatureBase64 = Base64.getEncoder().encode(digitalSignature);
        BigInteger signatureBase64BigInteger = new BigInteger(1, digitalSignatureBase64);
        BigInteger encDigitalSignature = signatureBase64BigInteger.modPow(privateKey.getExponent(), privateKey.getModulus());
        byte[] encSignatureByteArray = encDigitalSignature.toByteArray();

        ExchangedObject messageToSend = new ExchangedObject(Base64.getEncoder().encode(encryptedRequest), Base64.getEncoder().encode(encryptedKey), Base64.getEncoder().encode(encSignatureByteArray));


        return gson.toJson(messageToSend);
    }

    @Override
    public String decrypt(String input) throws IntegrityCheckFailedException {


        ExchangedObject messageReceived = gson.fromJson(input, ExchangedObject.class);
        BigInteger keyBigInteger = new BigInteger(1, Base64.getDecoder().decode(messageReceived.getEncryptedSharedKey()));
        String decryptedKey = new String(keyBigInteger.modPow(privateKey.getExponent(), privateKey.getModulus()).toByteArray());
        SymmetricKey sharedKey = new SharedKey(decryptedKey);
        byte[] decryptedRequest = encryptSymmetric(Base64.getDecoder().decode(messageReceived.getEncryptedRequest()), sharedKey, "Decrypt");

        byte[] generatedHash = generateHash(decryptedRequest);

        //decrypt
        byte[] encryptedDigitalSignature = Base64.getDecoder().decode(messageReceived.getEncryptedDigitalSignature());
        BigInteger encSignatureBigInteger = new BigInteger(1, encryptedDigitalSignature);
        BigInteger signatureBigInteger = encSignatureBigInteger.modPow(otherEntityPublicKey.getExponent(), otherEntityPublicKey.getModulus());
        byte[] decryptedSignatureBase64 = signatureBigInteger.toByteArray();
        byte[] decryptedSignature = Base64.getDecoder().decode(decryptedSignatureBase64);

        String result = new String(decryptedRequest);

        if (!Arrays.equals(decryptedSignature, generatedHash)) {
            throw new IntegrityCheckFailedException(result);
        }
        return result;
    }



}
