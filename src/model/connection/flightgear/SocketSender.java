package model.connection.flightgear;

import view.ChangeDataListener;

import java.io.PrintWriter;
import java.net.Socket;

public class SocketSender implements ChangeDataListener {

    private Socket mSocket = null;
    private int mPort;
    private PrintWriter mOut;

    public SocketSender(int port) {
        mPort = port;
        initSocket();
    }


    private void initSocket() {
        try {
            mSocket = new Socket("localhost", 5400);
            mOut = new PrintWriter(mSocket.getOutputStream());
        } catch (Exception e) { }
    }

    private static String convertFloatToStringLine(float[] arr, String separator) {
        if (null == arr || 0 == arr.length) return "";

        StringBuilder sb = new StringBuilder(256);
        sb.append(arr[0]);
        for (int i = 1; i < arr.length; i++) {
            sb.append(separator).append(arr[i]);
        }
        return sb.toString();
    }

    @Override
    public void onChangedData(int timeStamp, float[] values) {
        String line = convertFloatToStringLine(values, ",");
        if (mSocket == null) {
            initSocket();
        } else {
            System.out.println("Sending data to socket");
            mOut.println(line);
            mOut.flush();
        }
    }

}
