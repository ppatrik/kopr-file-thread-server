package sk.upjs.kopr2014.ppatrik.server;

import sk.upjs.kopr2014.ppatrik.common.Config;

import java.io.File;

public class Server {
    private static final int port = Config.SERVER_PORT;

    private static File subor = new File(Config.SERVER_FILE);

    private ThreadPooledServer server;

    public Server() {
        server = new ThreadPooledServer(port, subor);
    }

    /**
     * Spustenie serverovej sluzby
     *
     */
    public static void main(String[] args) {
        new Server().start();
    }

    public Thread start() {
        Thread t = new Thread(server);
        t.start();
        return t;
    }
}
