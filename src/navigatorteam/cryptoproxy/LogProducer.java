package navigatorteam.cryptoproxy;

import java.util.logging.Logger;

/**
 * Created on 2019-07-22.
 */
public interface LogProducer {

    String getLoggerName();


    default void printErr(String text){
        Logger.getLogger(getLoggerName()).severe(text);
    }

    default void printException(String sourceClass, String method, Throwable e){
        Logger.getLogger(getLoggerName()).throwing(sourceClass, method, e);
    }




}
