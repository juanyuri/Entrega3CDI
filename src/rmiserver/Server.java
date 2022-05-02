package rmiserver;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import rmiinterface.Hello;

public class Server implements Hello{

    @Override
    public String say_hi() throws RemoteException{
        return "Hi Client!!!";
    }
    public String play(String intento) throws RemoteException{
        System.out.println("Estoy jugando");
        return "OK, esta registrada la palabra: "+intento;
    }
    public static void main(String[] args) {
        Server srv= new Server();
        try{
        Hello stub=(Hello)UnicastRemoteObject.exportObject(srv, 0);
        Registry reg=LocateRegistry.getRegistry();
        reg.bind("MyServer",stub);

        System.out.println("Server ready");
        }catch(AlreadyBoundException abe){
            System.out.println("Server Name already at board");
        }catch(RemoteException re){
            System.out.println("Host unreachable");
        }
    }
}
