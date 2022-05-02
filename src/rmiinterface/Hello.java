package rmiinterface;
import java.rmi.Remote;
import java.rmi.RemoteException;

/*Hablamos de sistemas multimáquina
  Ejemplos: Redes sociales, moovi, google, amazon, videojuegos online
  Pregunta importante: que tiene que tener nuestro servidor? 
    Puede que necesite bases de datos
  En el caso de Amazon el código cliente es la capacidad de comunicarse 
  con el servidor, no obligatoriamente no tiene por qué tener interfaz 
  gráfica.
  P2P busca repetir, en lugar de distribuir, quiere servir al mayor 
  número de usuarios posibles. 
  Cliente-Servidor no típicamente pueden hablar entre sí los clientes.
  Un .java para clientes, y mínimo uno o varios para el servidor. 
  Todos los clientes usan el mismo código.
  En el caso de una subasta, en el lado del servidor se deben captar 
  las pujas de los clientes y saber quienes son.

  3packages rmiclient, rmiserver, rmiinterface
*/
public interface Hello extends Remote{
    public String say_hi() throws RemoteException;
}
