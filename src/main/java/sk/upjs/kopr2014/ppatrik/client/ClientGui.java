package sk.upjs.kopr2014.ppatrik.client;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ClientGui {
    private JTextField txtFileName;
    private JTextField txtFileSize;
    private JTextField txtFileParts;
    private JTextField txtFilePartSize;
    private JTextField txtFilePartsDown;
    private JButton btnStart;
    private JButton btnPause;
    private JButton btnStop;
    private JProgressBar barStatus;
    private JPanel panel;
    private JTable table1;
    private JFrame frame;

    private ThreadPooledClient client;
    private Listener listener;

    public ClientGui() {
        frame = new JFrame("ClientGui");
        frame.setContentPane(panel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        listener = new Listener(this);
        client = new ThreadPooledClient(listener);
        MyTableModel tm = new MyTableModel();
        listener.setTableModel(tm);
        table1.setModel(tm);

        btnStart.setEnabled(true);
        btnPause.setEnabled(false);
        btnStop.setEnabled(false);

        btnStart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(client).start();
                btnStart.setEnabled(false);
                btnPause.setEnabled(true);
                btnStop.setEnabled(true);
            }
        });
        btnPause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.pause();
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });
        btnStop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.stop();
            }
        });

        frame.pack();
        frame.setVisible(true);

        // Nacitanie suboru
        if (!client.loadFileInfo()) {
            frame.setVisible(false); //you can't see me!
            frame.dispose(); //Destroy the JFrame object
        }
    }

    public class Listener {
        public int spentTime = 0;
        public int totalParts = 0;
        public long start = 0;
        ClientGui gui;
        MyTableModel tableModel;
        int parts = 0;

        public Listener(ClientGui gui) {
            this.gui = gui;
        }

        public void setTableModel(MyTableModel tableModel) {
            this.tableModel = tableModel;
        }

        public void setMeta(String fileName, long fileSize, int filePartSize, int fileParts) {
            totalParts = 0;
            txtFileName.setText(fileName);
            txtFileSize.setText(Long.toString(fileSize));
            txtFilePartSize.setText(Integer.toString(filePartSize));
            txtFileParts.setText(Integer.toString(fileParts));
            parts = fileParts;

            barStatus.setMinimum(0);
            barStatus.setMaximum(fileParts);

            tableModel.createParts(fileParts);
        }

        public void setDataTime(int filePart, byte[] data, int timeSpent) {
            if (start == 0)
                start = System.currentTimeMillis();
            spentTime += timeSpent;
            totalParts++;
            int totalTime = (int) (System.currentTimeMillis() - start);
            txtFilePartsDown.setText(Integer.toString(totalTime) + " s " + Integer.toString(spentTime) + " s " + totalParts + "parts");
            barStatus.setValue(totalParts);
            tableModel.setRiadok(filePart, data, timeSpent);
        }

        public void setDownloaded(int filePart) {
            totalParts++;
            barStatus.setValue(totalParts);
            tableModel.setRiadok(filePart, new byte[]{}, -1);
            txtFilePartsDown.setText("Downloaded before " + totalParts);
        }

        public void paused() {
            txtFilePartsDown.setText("Paused");
        }

        public void stopped() {
            txtFilePartsDown.setText("Stopped");
        }
    }

    class MyTableModel extends AbstractTableModel {

        Row[] riadky = new Row[0];

        public void createParts(int parts) {
            riadky = new Row[parts];
            fireTableDataChanged();
        }

        public void setRiadok(int part, byte[] data, int time) {
            riadky[part] = new Row();
            riadky[part].row_id = part;
            riadky[part].time = time;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return riadky.length;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "ID";
                case 1:
                    return "Time";
            }
            return "null";
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (riadky[rowIndex] == null) {
                return "";
            }
            switch (columnIndex) {
                case 0:
                    return riadky[rowIndex].row_id;
                case 1:
                    if (riadky[rowIndex].time == -1)
                        return "Downloaded before";
                    return riadky[rowIndex].time;
            }
            return null;
        }

        class Row {
            public int row_id;
            public int nuly = 0;
            public int time;
            public int serverNuly;
        }

    }
}
