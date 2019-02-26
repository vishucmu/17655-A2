package FailureDetection;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class DaemonPingEchoReplier implements Runnable {
    @Override
    public void run() {
        ServerSocket ss = null;

        try {
            System.out.println("Starting server ....");
            ss = new ServerSocket(8888);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        while(true){
            try {
                Socket s = ss.accept();
                System.out.println("Connected:"+s.getInetAddress().getLocalHost());

                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String mess = br.readLine();
                System.out.println("msg："+mess);

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
                bw.write(mess+"\n");
                bw.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
