package sk.upjs.kopr2014.ppatrik.server;

import sk.upjs.kopr2014.ppatrik.common.FileSaverReader;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


public class WorkerRunnable implements Runnable {

    protected Socket clientSocket = null;
    private FileSaverReader fs;

    public WorkerRunnable(Socket clientSocket, FileSaverReader fs) {
        this.clientSocket = clientSocket;
        this.fs = fs;
    }

    public void run() {
        try {
            DataInputStream input = new DataInputStream(clientSocket.getInputStream());

            String request = input.readUTF();
            if ("meta".equals(request)) {
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                output.writeUTF(fs.getFileName());
                output.writeLong(fs.getFileSize());
                output.writeInt(fs.getPartSize());
                output.writeInt(fs.getParts());
                output.flush();
                output.close();
            }

            if ("down".equals(request)) {
                int part = input.readInt();
                BufferedOutputStream bus = new BufferedOutputStream(clientSocket.getOutputStream(), fs.getPartSize());

                bus.write(fs.readBytes(part).toByteArray());
                bus.flush();
                bus.close();
            }

            input.close();
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}