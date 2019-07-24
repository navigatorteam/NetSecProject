package navigatorteam.cryptoproxy;

import rawhttp.core.RawHttp;
import rawhttp.core.RawHttpRequest;

import java.io.IOException;

/**
 * Created on 2019-07-23.
 */
public class ReqContainer {
    private String stringReq;

    public ReqContainer(RawHttpRequest req) throws IOException {
        this.stringReq = req.eagerly().toString();
    }

    public RawHttpRequest getReq(RawHttp rawHttp) {
        return rawHttp.parseRequest(stringReq);
    }
}
