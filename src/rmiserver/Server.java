package rmiserver;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rmiinterface.Wordle;

public class Server extends UnicastRemoteObject implements Wordle {
    
    String nombreServidor;
    int numeroJugadores;

    String palabraPropuesta;
    String palabraIntento;
    String posiblesPropuestas[];
    Map<String,String> jugadoresActuales;
    int capacidadVector;
    ArrayList<String> palabrasProhibidas;
    char[] letrasAbecedario;
    static Map<Character,List<String>> mapaPalabrasPosibles;
    static Map<String,String> palabraCorrespondeJugador;

    public Server(String nombreServidor) throws RemoteException{
        this.nombreServidor=nombreServidor;
        this.numeroJugadores=0;
        this.capacidadVector=20;
        this.posiblesPropuestas= new String[]{"HOJAS","CASAS","RELAX","ACOTA","AGUDO","GRAVE",
                                              "SOLAR","SALIR","ABETO","MATES","MATON","LUNES",
                                              "AGUAS","ODIAR","COMER","BEBER","FUMAR","FUTIL",
                                              "PERRO","GATOS","LOROS","CACOS","CAJON","TORTA"};
        this.palabrasProhibidas= new ArrayList<String>(capacidadVector);
        jugadoresActuales= new HashMap<>();
        palabraCorrespondeJugador= new HashMap<>();
        letrasAbecedario= new char[]{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    }

    public void resetPart(String nombre){
        jugadoresActuales.remove(nombre); //Eliminamos el registro del jugador
        palabraCorrespondeJugador.remove(nombre); //Eliminamos alguna palabra que quede suelta
        if(numeroJugadores!=0){
            numeroJugadores--;
        }
        System.out.println("He eliminado la información del jugador "+nombre);
    }
    @Override
    public String iniciarPartida(String nombre) throws RemoteException{
        resetPart(nombre); //Reseteamos posible informacion sobre el jugador que acaba de entrar
        try {
            jugadoresActuales.put(nombre,getClientHost());
            palabraCorrespondeJugador.put(nombre,posiblesPropuestas[(int)(Math.random()*(capacidadVector-1)+1)]); //Escoge una palabra y la asocia al jugador
            numeroJugadores++;

        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        showMessage("Se ha asociado la palabra "+palabraCorrespondeJugador.get(nombre)+" al jugador "+nombre);
        return "De acuerdo, "+nombre+" puedes empezar a jugar";
    }

    public String play(String nombre, String intento) throws RemoteException{
        String peticion = "intento jugar con " + intento;
        intento = intento.toUpperCase();
        String palabraCorrespondiente= palabraCorrespondeJugador.get(nombre).toUpperCase();
        showRequest(nombre,peticion);


        StringBuilder resultado = new StringBuilder();
        char[] intentoVector = intento.toCharArray();
        char[] propuestaVector = palabraCorrespondiente.toCharArray(); // suponemos minuscula

        for(int i = 0; i< intentoVector.length; i++){
            if(intentoVector[i] == propuestaVector[i]){ // Si están en la misma posición
                resultado.append("V");
            }else if(contiene(propuestaVector,intentoVector[i])){ // si la propuesta no está en la misma posición, pero si en otra
                resultado.append("A");
            }else{ // si la propuesta no está en la palabra
                resultado.append("G");
            }
        }

        if(resultado.toString().equals("VVVVV")){ //Ha ganado
            showRequest(nombre, nombre+" ha ganado");
        }else{
            showRequest(nombre, nombre+" ha perdido");
        }
       
        return resultado.toString();
    }

    private boolean contiene(char[] vectorCharacter, char letra){
        int i = 0;
        while(i < vectorCharacter.length && vectorCharacter[i] != letra){
            i++;
        }

        return (i != vectorCharacter.length); // si ha llegado al final, no ha encontrado nada
    }

    public boolean compruebaPalabra(String intento){
        if(intento.length() != 5){
            System.out.println("Esa palabra no vale");
        }
        return true;
    }

    private void showRequest(String nombre, String request){
        System.out.println("(" + nombre + ") " + request);
    }

    private static void showMessage(String msg){
        System.out.println("(" + "Server" + ") " + msg);
    }

    public static void main(String[] args){
        String nombreServidor= args[0]; //Se guarda el nombre

        try{
            Naming.bind("//localhost:1099/"+nombreServidor,new Server(nombreServidor));
            showMessage("Server ready");
        }catch(AlreadyBoundException abe){
            System.out.println("Server Name already at board");
        }catch(MalformedURLException mue){
            System.out.println("Malformed URL");
        }catch(RemoteException re){
            System.out.println("Host unreachable");
        }
    }

    @Override
    public String noJuega(String name) throws RemoteException {
        showMessage("El jugador "+name+" no quiere seguir jugando");
        return "De acuerdo";
    }
}
