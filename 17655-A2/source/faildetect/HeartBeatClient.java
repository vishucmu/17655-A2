package faildetect;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class HeartBeatClient implements Runnable{

    private String message;

    public HeartBeatClient(String msg){
        this.message = msg;
    }

    @Override
    public void run() {
        while (true){
            try {
                Socket s = new Socket("127.0.0.1",8888);

                OutputStream os = s.getOutputStream();

                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                bw.write("[Heartbeat sending] "+this.message +" \n");
                bw.flush();

                s.close();
            }catch(UnknownHostException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
