package rmiinterface;
import java.rmi.Remote;
import java.rmi.RemoteException;
/**
 *
 * @author Juan Yuri Díaz Sánchez
 */
public interface Hello extends Remote{
    public String say_hi() throws RemoteException; // capturar de manera especializada
}
