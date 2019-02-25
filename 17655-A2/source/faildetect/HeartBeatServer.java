package faildetect;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class HeartBeatServer implements Runnable {

    @Override
    public void run() {

        try {
            ServerSocket ss = new ServerSocket(8888);
            System.out.println("Starting detection ....");

            while (true){
                Socket s = ss.accept();
                System.out.println("Client:"+s.getInetAddress().getLocalHost()+" has connected");
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

                String mess = br.readLine();

                //do some check here
                System.out.println("message："+mess);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
