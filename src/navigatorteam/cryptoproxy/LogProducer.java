package navigatorteam.cryptoproxy;

import java.util.logging.Logger;

/**
 * Created on 2019-07-22.
 */
public interface LogProducer {

    String getLoggerName();


    default Logger logger(){
        return Logger.getLogger(getLoggerName());
    }


    default void print(String text){
        logger().info(text);
    }

    default void printErr(String text){
        logger().severe(text);
    }

    default void printException(String sourceClass, String method, Throwable e){
        logger().throwing(sourceClass, method, e);
    }




}
