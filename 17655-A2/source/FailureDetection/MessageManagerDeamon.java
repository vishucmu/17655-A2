package FailureDetection;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class MessageManagerDeamon implements Runnable{

    @Override
    public void run() {
        try {
            Runtime.getRuntime().exec(new String[]{"java","-classpath","out/production/","MessageManagerDeamon"});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        while (true){
            Socket s = null;
            boolean isTryingToRecover = false;
            try {
                s = new Socket("127.0.0.1", 8888);
            } catch (IOException e) {
                isTryingToRecover = true;
                System.out.println("Failure Detected");
            }

            if (!isTryingToRecover){
                try {

                    InputStream is = s.getInputStream();
                    OutputStream os = s.getOutputStream();

                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
                    bw.write("ping\n");
                    bw.flush();

                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String mess = br.readLine();
                    System.out.println("echo：" + mess);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
