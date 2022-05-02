package rmiserver;
import java.net.MalformedURLException;
import java.nio.charset.MalformedInputException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
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
    
    public static void main(String[] args) {
        Server srv= new Server(); //Creamos el servidor con el que nos comunicaremos
        try{
        Hello stub=(Hello)UnicastRemoteObject.exportObject(srv, 0); //Generamos la referencia remote de la parte del servidor que queremos que se use por remoto, puerto 0 es el puerto por defecto, que es el 1099, necesitamos el say_hi (dado por Hello)
        //Hay que "publicar" el servidor java.rmi.registry
        Registry reg=LocateRegistry.getRegistry(); //Creamos el registro para que use, en este modo localhost con puerto 1099, cambiar si se necesita conectar a otra máquina
        //Usamos bind pero también podemos usar rebind
        reg.bind("MyServer",stub); //Vincula o asocia un nombre creado para cada cliente al stub, estamos diciendo como llegará un cliente a nuestro método libre para el público
        
        //Segunda forma
        //Naming.bind("//localhost:1099/MyServer",stub);
        System.out.println("Server ready");
        }catch(AlreadyBoundException abe){
            System.out.println("Server Name already at board"); //Ya existe ese nombre en el "tablón de anuncios"
        }catch(RemoteException re){
            System.out.println("Host unreachable"); //La comunicación ha fallado
        }//catch(MalformedURLException mue){
         //   System.out.println("Unknown URL"); //La comunicación ha fallado
         //}
    }
}
