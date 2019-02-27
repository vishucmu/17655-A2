package Robustness;

import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.RemoteException;


/*
 * A utility class to simplify code
 *
 * @author Li Zhang
 * @date 02/26/2019
 *
 */
public class Robust {

    public static final int WAITING_TIME_FOR_RESTART_MSG_MGR = 1000;

    //
    // Sleep several seconds and return a new MessageManagerInterface instance
    //
    // @arg type: what type of message will be send through the new MessageManagerInterface
    // @return MessageManagerInterface instance
    //
    public static MessageManagerInterface sleepAndReconnect(MessageType type){
        try {
            Thread.sleep(Robust.WAITING_TIME_FOR_RESTART_MSG_MGR);
            return Robust.newMsgMgr(type);
        } catch (InterruptedException | RemoteException e){
            //do nothing and retry again
        }
        return null;
    }

    //
    // This method will new a MessageManagerInterface with message type and ip address
    //
    // @arg type: what type of message will be send through this MessageManagerInterface
    // @return MessageManagerInterface instance
    //
    public static MessageManagerInterface newMsgMgr(MessageType type) throws RemoteException {
        // message manager is on the local system
        System.out.println("\n\nAttempting to register on the local machine..." );
        try
        {
            // Here we create an message manager interface object. This assumes
            // that the message manager is on the local machine

            return new MessageManagerInterface(type);
        }
        catch (Exception e)
        {
            throw new RemoteException("Error instantiating message manager interface: ",e);
        } // catch
    }

    //
    // This method will new a MessageManagerInterface with message type and ip address
    //
    // @arg type: what type of message will be send through this MessageManagerInterface
    // @arg msgMgrIP: the ip address that new MessageManagerInterface will be bound.
    // @return MessageManagerInterface instance
    //
    public static MessageManagerInterface newMsgMgr(MessageType type, String msgMgrIP) throws RemoteException {
        // message manager is on the local system
        System.out.println("\n\nAttempting to register on the machine:: " + msgMgrIP );
        try
        {
            // Here we create an message manager interface object. This assumes
            // that the message manager is on the local machine

            return new MessageManagerInterface(type, msgMgrIP);
        }
        catch (Exception e)
        {
            throw new RemoteException("Error instantiating message manager interface: ",e);
        } // catch
    }

    //This method returns the current classpath
    public static String classPath(){
        return Thread.currentThread().getContextClassLoader().getResource("").getPath();
    }

    //This method will start a new Java process, the main class name is given by the argument
    //The classpath will be searched automatically
    //
    // @arg className: main class name
    // @return java.lang.Process object
    //
    public static Process startNewJava(String className){

        try {

            //new a process by Java Runtime
            final Process x = Runtime.getRuntime().exec(new String[]{"java", "-classpath", classPath(), className});

            //This thread will be start to watch the error message from the new started process
            new Thread(new Runnable() {
                @Override
                public void run() {
                    //Get error input stream from the started process.
                    InputStream es = x.getErrorStream();
                    BufferedReader bf = new BufferedReader(new InputStreamReader(es));
                    String tmp = null;
                    try {
                        while ((tmp = bf.readLine()) != null) {
                            System.out.println("========== Error in the new started Java process ========== !");
                            System.out.println(tmp);
                        }
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
