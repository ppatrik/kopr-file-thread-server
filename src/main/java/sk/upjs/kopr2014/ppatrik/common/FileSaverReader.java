package sk.upjs.kopr2014.ppatrik.common;

import java.io.*;

public class FileSaverReader {
    private final Object lock = new Object();
    Thread lockHolder;
    RandomAccessFile randomAccessFile;
    int partSize = 64 * 1024;
    long fileSize;
    int parts;
    File file;

    public FileSaverReader(File file, boolean read) {
        lockHolder = null;
        fileSize = file.length();
        parts = (int) Math.ceil(fileSize / (double) partSize);

        this.file = file;
        if (!read) {
            try {
                randomAccessFile = new RandomAccessFile(file, "rw");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            try {
                randomAccessFile = new RandomAccessFile(file, "r");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPartSize() {
        return partSize;
    }

    public void setPartSize(int partSize) {
        this.partSize = partSize;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
        try {
            randomAccessFile.setLength(fileSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getParts() {
        return parts;
    }

    public void setParts(int parts) {
        this.parts = parts;
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }

    public long getStartPos(int part) {
        return (long) part * (long) partSize;
    }

    public long getLength(int part, long startPos) {
        if (part + 1 == parts) {
            return fileSize - startPos;
        }
        return partSize;

    }

    public void write(int part, byte[] data) throws InterruptedException {
        long startPos = getStartPos(part);
        long readLenght = getLength(part, startPos);

        while (!acquireWrite(Thread.currentThread())) {
            Thread.sleep(50);
        }

        try {
            randomAccessFile.seek(startPos);
            randomAccessFile.write(data, 0, (int) readLenght);
        } catch (IOException e) {
            e.printStackTrace();
        }

        releaseWrite(Thread.currentThread());
    }

    public byte[] read(int part) {
        long startPos = getStartPos(part);
        int readLenght = (int) getLength(part, startPos);
        byte[] buf = new byte[readLenght];

        while (!acquireWrite(Thread.currentThread())) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            randomAccessFile.seek(startPos);
            randomAccessFile.read(buf, 0, buf.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        releaseWrite(Thread.currentThread());
        return buf;
    }

    public ByteArrayOutputStream readBytes(int part) {
        ByteArrayOutputStream ao = new ByteArrayOutputStream();

        byte[] buf = read(part);
        ao.write(buf, 0, buf.length);

        return ao;

    }

    public boolean acquireWrite(Thread t) {
        synchronized (lock) {
            if (lockHolder != null)
                return false;
            lockHolder = t;
        }
        return true;
    }

    public void releaseWrite(Thread t) {
        synchronized (lock) {
            if (t != lockHolder)
                return; // iba ten co si vytvoril zamok ho vie aj obnovit
            lockHolder = null;
        }
    }

    public void close() {
        if (randomAccessFile != null)
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}
