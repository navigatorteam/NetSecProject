package navigatorteam.cryptoproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created on 2019-07-22.
 */
public class Pipe extends Thread {

    private Set<Thread> activeThreads;
    private BufferedReader in;
    private PrintWriter out;
    private final Function<String, String> onEveryLine;


    public Pipe(Set<Thread> activeThreads, BufferedReader in, PrintWriter out, Function<String, String> onEveryLine) {
        this.activeThreads = activeThreads;
        this.in = in;
        this.out = out;
        this.onEveryLine = onEveryLine;
    }

    @Override
    public void run() {

        String line;
        try {
            while ((line = in.readLine()) != null) {
                out.println(onEveryLine.apply(line));
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        activeThreads.remove(this);
    }
}
