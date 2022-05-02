package rmiserver;
import java.net.MalformedURLException;
import rmiinterface.Hello;

import java.rmi.RemoteException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Juan Yuri Díaz Sánchez
 */
public class Server implements Hello{

    public Server(){
        //this.server = null;
    }
    
    public static void main(String[] args){ // para poder arrancarlo
        Server server = new Server();
        //java.rmi.server.logCalls=true;
        
        try{
            // parte visible
            Hello stub = (Hello) UnicastRemoteObject.exportObject(server,0); // que esté asociado a Hello extends Remote
            
            // publicar nuestro servidor en el registry. Localizar tablon de anuncios con registry
            //Registry registro = LocateRegistry.getRegistry(1099); // 1099 a buscarlo si no es local getRegistry(host, port);
            
            //registro.bind("MundoFeliz",stub); // bind: vincular, asociar
            // server no es una referencia remota, ccon stub decimos como llegar a la parte abierta del servidor
            
            Naming.bind("//localhost:1099/MundoFeliz", stub);
            
            System.out.println("Servidor listo.");
        
        }catch(RemoteException e){ // problemas de 
            System.out.println("Host unreachable. Failed communication." + e);
        }catch(AlreadyBoundException abe){
            System.out.println("AlreadyBound Exception: " + abe);
        }// catch (MalformedURLException ex) {
           // System.out.println("AlreadyBound Exception: " + ex);
        catch(Exception ex2){
            System.err.println(ex2.getMessage());
        }  
        
    }
    
    
    public String say_hi() throws RemoteException {
        return "hello World";
    }
}
