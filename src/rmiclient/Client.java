package rmiclient;

import rmiinterface.Hello;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;


/**
 *
 * @author Juan Yuri Díaz Sánchez
 */
public class Client {
    
    public static void main(String[] args){
        try{
            Registry registro = LocateRegistry.getRegistry(); // todo en local host (ip y puerto) del registro que estamos usando
            Hello objeto = (Hello) registro.lookup("MundoFeliz"); // BUSCAMOS EL SERVIDOR
            
            // obtenemos respuesta
            String respuesta = objeto.say_hi();
            System.out.println(respuesta);
            
            
            
            
        }catch(RemoteException e){
            System.out.println("Host unreachable. Failed communication." + e);
        } catch(NotBoundException nbe){
            System.out.println("Name not bounded to nay remote reference!" + nbe);
        }
        
    }
}
