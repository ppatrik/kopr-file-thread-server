package sk.upjs.kopr2014.ppatrik.server;

import sk.upjs.kopr2014.ppatrik.common.FileSaverReader;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPooledServer implements Runnable {

    public FileSaverReader fs;
    protected int serverPort = 8080;
    protected boolean endRequested = false;
    protected ExecutorService threadPool = Executors.newFixedThreadPool(4);

    public ThreadPooledServer(int port, File subor) {
        serverPort = port;
        fs = new FileSaverReader(subor, true);
    }

    public void run() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (serverSocket != null) {
            while (!endRequested) {
                try {
                    // pridanie poziadavky do zasobnika
                    Socket clientSocket = serverSocket.accept();
                    this.threadPool.execute(new WorkerRunnable(clientSocket, fs));
                } catch (IOException e) {
                    throw new RuntimeException("Error accepting client connection", e);
                }
            }

            this.threadPool.shutdown();
            System.out.println("Server Stopped.");
        }
    }
}
