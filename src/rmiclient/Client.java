package rmiclient;

/*Para compilar: javac rmiinterface/*.java rmiserver/*.java rmiclient/*.java
  Para ejecutar: start rmiregistry
                 java rmiserver/Server
*/
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import rmiinterface.Hello;

public class Client{
    public static void main(String[] args) {
        //No se puede exportar nada si el servidor no está levantado
        try{
        Registry reg= LocateRegistry.getRegistry(1099); //Cambiar si no es localhost
        Hello replyobj=(Hello)reg.lookup("MyServer"); //El nombre del servidor del tablón de anuncios, devuelve un Hello por el método que buscamos en el Server
        String reply=replyobj.play("abcde");
        System.out.println("I have obtained a reply: "+reply);
        }catch(NotBoundException nbe){
            System.out.println("Server does not exist");
        }catch(RemoteException re){
            System.out.println("Host unreachable");
        }
    }
}