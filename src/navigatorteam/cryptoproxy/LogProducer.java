package navigatorteam.cryptoproxy;

import java.util.logging.Logger;

/**
 * Created on 2019-07-22.
 */
public interface LogProducer {

    default String getLoggerName(){
        return "navigatorteam.cryptoproxy";
    }

    default Logger log(){
        return Logger.getLogger(getLoggerName());
    }




}
