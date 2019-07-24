package navigatorteam.cryptoproxy;

import java.nio.charset.StandardCharsets;

public class ExchangedObject {

    private String encryptedRequest;
    private String encryptedSharedKey;
    private String encryptedDigitalSignature;

    public ExchangedObject(byte[] r, byte[] sk, byte[] ds){
        encryptedRequest = new String(r, StandardCharsets.UTF_8);
        encryptedSharedKey = new String(sk, StandardCharsets.UTF_8);
        encryptedDigitalSignature = new String(ds, StandardCharsets.UTF_8);
    }

    public byte[] getEncryptedRequest() {
        return encryptedRequest.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getEncryptedSharedKey() {
        return encryptedSharedKey.getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getEncryptedDigitalSignature() {
        return encryptedDigitalSignature.getBytes(StandardCharsets.UTF_8);
    }
}
