package Robustness;

import MessagePackage.MessageManagerInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;

public class Robust {

    public static final int WAITING_TIME_FOR_RESTART_MSG_MGR = 300;

    public static MessageManagerInterface newMsgMgr() throws RemoteException {
        // message manager is on the local system
        System.out.println("\n\nAttempting to register on the local machine..." );
        try
        {
            // Here we create an message manager interface object. This assumes
            // that the message manager is on the local machine

            return new MessageManagerInterface();
        }
        catch (Exception e)
        {
            throw new RemoteException("Error instantiating message manager interface: ",e);
        } // catch
    }

    public static MessageManagerInterface newMsgMgr(String msgMgrIP) throws RemoteException {
        // message manager is on the local system
        System.out.println("\n\nAttempting to register on the machine:: " + msgMgrIP );
        try
        {
            // Here we create an message manager interface object. This assumes
            // that the message manager is on the local machine

            return new MessageManagerInterface(msgMgrIP);
        }
        catch (Exception e)
        {
            throw new RemoteException("Error instantiating message manager interface: ",e);
        } // catch
    }

    public static String classPath(){
        return Thread.currentThread().getContextClassLoader().getResource("").getPath();
    }

    public static Process startNewJava(String className){

        try {

            final Process x = Runtime.getRuntime().exec(new String[]{"java", "-classpath", classPath(), className});

            new Thread(new Runnable() {
                @Override
                public void run() {
                    InputStream es = x.getErrorStream();
                    BufferedReader bf = new BufferedReader(new InputStreamReader(es));
                    String tmp = null;
                    try {
                        System.out.println("========== Error in the new started Java process ========== !");
                        while ((tmp = bf.readLine()) != null) {
                            System.out.println(tmp);
                        }
                        System.out.println("========== Error in the new started Java process ========== !");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            return x;

        }catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }
}
