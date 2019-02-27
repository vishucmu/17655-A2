package Robustness;

import MessagePackage.MessageManagerInterface;
import MessagePackage.RMIMessageManagerInterface;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class MessageManagerDaemon {

    private static final int PING_TIME_GAP = 1000;
    private static final int RECONNECT_TIME_GAP = 1000;

    public static void main(String[] args) {

        RMIMessageManagerInterface rmiMMI = connect(args);

        while (true) {
            try {
                rmiMMI.ping();
                Thread.sleep(PING_TIME_GAP);
            } catch (InterruptedException e) {
                //do nothing
            } catch (RemoteException e) {
                System.out.println("Connection lost! Reconnecting ... ");
                Process p = Robust.startNewJava("MessageManager");
                if (p.isAlive()) {
                    System.out.println("New MessageManager has been restart up!");
                }
                rmiMMI = connect(args);
            }
        }
    }

    private static RMIMessageManagerInterface connect(String[] args){

        String MMIPAdress = null;
        RMIMessageManagerInterface result = null;

        while (true) {
            try {
                if ( args.length == 0 ){
                    result = (RMIMessageManagerInterface) Naming.lookup("MessageManager");
                }else{
                    MMIPAdress = args[0];
                    String EMServer = "//" + MMIPAdress + ":" + MessageManagerInterface.DEFAULTPORT + "/MessageManager";
                    result = (RMIMessageManagerInterface) Naming.lookup( EMServer );
                }

                if (result != null){
                    return result;
                }

            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                System.out.println("Daemon process start failed, retrying ...");

                try {
                    Thread.sleep(RECONNECT_TIME_GAP);
                } catch (InterruptedException e1) {
                    //do nothing
                }
            }
        }
    }
}