package rmiinterface;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Wordle extends Remote{
    //public String say_hi() throws RemoteException;
    //public String play(String intento) throws RemoteException;
    public String iniciarPartida(String name) throws RemoteException;
    public String play(String name, String palabraEnviada) throws RemoteException;
    public String noJuega(String name) throws RemoteException;  
}
