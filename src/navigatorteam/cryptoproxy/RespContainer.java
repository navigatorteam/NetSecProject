package navigatorteam.cryptoproxy;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpResponse;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created on 2019-07-23.
 */
public class RespContainer implements Serializable {
    private String stringResponse;

    public RespContainer(RawHttpResponse<Void> response) throws IOException {
        stringResponse = response.eagerly().toString();

    }


    public RawHttpResponse<Void> getResponse(RawHttp rawHttp) {
        return rawHttp.parseResponse(stringResponse);
    }
}
