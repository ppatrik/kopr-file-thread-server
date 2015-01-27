package sk.upjs.kopr2014.ppatrik.client;

import sk.upjs.kopr2014.ppatrik.common.Config;
import sk.upjs.kopr2014.ppatrik.common.DialogUtils;
import sk.upjs.kopr2014.ppatrik.common.FileSaverReader;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadPooledClient implements Runnable {
    public FileSaverReader fs;
    public FileSaverReader fsParts;
    public ClientGui.Listener listener;
    protected ExecutorService threadPool;

    public ThreadPooledClient(ClientGui.Listener listener) {
        this.listener = listener;
    }

    public boolean loadFileInfo() {
        try {
            Socket client = connection();

            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            DataInputStream input = new DataInputStream(client.getInputStream());

            output.writeUTF("meta");

            System.out.println("Meta from server:");

            try {
                String fileName = input.readUTF();
                long fileSize = input.readLong();
                int partSize = input.readInt();
                int parts = input.readInt();

                fs = new FileSaverReader(new File("files_out/" + fileName), false);
                fs.setFileSize(fileSize);
                fs.setPartSize(partSize);
                fs.setParts(parts);

                fsParts = new FileSaverReader(new File("files_out/" + fileName + ".part"), false);
                fsParts.setFileSize(parts);
                fsParts.setPartSize(1);
                fsParts.setParts(parts);
            } catch (EOFException e) {
                e.printStackTrace();
                return false;
            }

            System.out.println("Meta received");

            client.close();
        } catch (IOException e) {
            DialogUtils.errorDialog(null, e.getMessage());
            return false;
        }
        listener.setMeta(fs.getFileName(), fs.getFileSize(), fs.getPartSize(), fs.getParts());

        if (fs.getParts() > 0) {
            for (int i = 0; i < fs.getParts(); i++) {
                byte[] status = this.fsParts.read(i);
                if (status[0] != 0) {
                    listener.setDownloaded(i);
                }
            }
        }

        return true;
    }

    public Socket connection() {
        try {
            Socket s = new Socket(Config.SERVER_IP, Config.SERVER_PORT);
            s.setKeepAlive(true);
            return s;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void init() {
        threadPool = Executors.newFixedThreadPool(3);
    }


    @Override
    public void run() {
        init();
        if (fs.getParts() > 0) {
            for (int i = 0; i < fs.getParts(); i++) {
                byte[] status = this.fsParts.read(i);
                if (status[0] == 0) {
                    this.threadPool.execute(new WorkerRunnable(i, this, fs, fsParts));
                }
            }
        }
        //fs.close();
        System.out.println("Client download service stopped");
    }

    public void pause() {
        this.threadPool.shutdownNow();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        listener.paused();
    }

    public void stop() {
        pause();

        fs.close();
        if (fs.getFile().delete()) {
            System.out.println(fs.getFile().getName() + " is deleted!");
        } else {
            System.out.println("Delete operation is failed.");
        }
        fsParts.close();
        if (fsParts.getFile().delete()) {
            System.out.println(fsParts.getFile().getName() + " is deleted!");
        } else {
            System.out.println("Delete operation is failed.");
        }

        listener.stopped();
        DialogUtils.errorDialog(null, "Zastavené");
        System.exit(0);
    }
}
