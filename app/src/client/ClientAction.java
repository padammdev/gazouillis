package client;

import java.io.IOException;

public interface ClientAction {
   String getCommand();
   void run() throws IOException, InterruptedException;
}
