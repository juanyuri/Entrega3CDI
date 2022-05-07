package rmiinterface;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Wordle extends Remote{
    public String iniciarConexion(String name) throws RemoteException; // El jugador puede empezar a jugar
    public boolean asociarPalabra(String nombre) throws RemoteException; // asociamos palabra propuesta al cliente
    public String play(String name, String palabraEnviada) throws RemoteException;
}
