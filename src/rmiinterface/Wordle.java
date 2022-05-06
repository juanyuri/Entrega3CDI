package rmiinterface;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Wordle extends Remote{
    public String iniciarConexion(String name) throws RemoteException; // El jugador puede empezar a jugar
    public String play(String name, String palabraEnviada) throws RemoteException, Exception;
    //public String noJuega(String name) throws RemoteException;
}
