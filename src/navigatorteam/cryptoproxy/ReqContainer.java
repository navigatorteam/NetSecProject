package navigatorteam.cryptoproxy;

import rawhttp.core.RawHttpRequest;

/**
 * Created on 2019-07-23.
 */
public class ReqContainer {
    private RawHttpRequest req;

    public ReqContainer(RawHttpRequest req) {
        this.req = req;
    }

    public RawHttpRequest getReq() {
        return req;
    }
}
