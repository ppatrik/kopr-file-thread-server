package sk.upjs.kopr2014.ppatrik.client;

import sk.upjs.kopr2014.ppatrik.common.FileSaverReader;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class WorkerRunnable implements Runnable {
    private int part;
    private ThreadPooledClient threadPooledClient;
    private FileSaverReader fileSaver;
    private FileSaverReader fileSaverParts;

    public WorkerRunnable(int part, ThreadPooledClient threadPooledClient, FileSaverReader fileSaver, FileSaverReader fileSaverParts) {
        this.part = part;
        this.threadPooledClient = threadPooledClient;
        this.fileSaver = fileSaver;
        this.fileSaverParts = fileSaverParts;
    }

    public void run() {
        long start = System.currentTimeMillis();
        Socket client = threadPooledClient.connection();
        try {
            DataOutputStream output = new DataOutputStream(client.getOutputStream());
            BufferedInputStream bis = new BufferedInputStream(client.getInputStream(), fileSaver.getPartSize());

            output.writeUTF("down");
            output.writeInt(part);
            output.flush();

            byte[] filePart = new byte[fileSaver.getPartSize()];

            long startPos = fileSaver.getStartPos(part);
            long readLenght = fileSaver.getLength(part, startPos);
            while (bis.available() < (int) readLenght) {
                Thread.sleep(0);
            }
            bis.read(filePart, 0, (int) readLenght);
            fileSaver.write(part, filePart);

            // ukoncit spojenie
            output.close();
            bis.close();
            client.close();

            // oznamit oknu ze sme stiahli tento part
            long end = System.currentTimeMillis();
            fileSaverParts.write(part, new byte[]{1});
            threadPooledClient.listener.setDataTime(part, filePart, (int) (end - start));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.out.println("Pozastavene stahovanie");
        }
    }

}
