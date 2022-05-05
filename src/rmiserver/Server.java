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
import java.util.Set;
import java.util.Random;

import rmiinterface.Wordle;

public class Server extends UnicastRemoteObject implements Wordle, Runnable {
    String nombreServidor;
    int numeroJugadores;

    String palabraPropuesta;
    String nuevaPalabra;
    String palabraIntento;
    
    int capacidadVector;
    ArrayList<String> palabrasProhibidas;
    ArrayList<String> palabrasPropuestas;

    

    Random rand = new Random();

    //char[] letrasAbecedario;
    //static Map<Character,List<String>> mapaPalabrasPosibles;

    static Map<String,String> mapaJugadorPalabra;
    Map<String,String> jugadoresActuales;

    public Server(String nombreServidor) throws RemoteException{
        this.nombreServidor = nombreServidor;
        this.numeroJugadores = 0;
        this.nuevaPalabra = "";
        this.capacidadVector = 4;
        this.palabrasPropuestas = addValuesToList();
        this.palabraPropuesta = palabrasPropuestas.get(rand.nextInt(palabrasPropuestas.size()));
        System.out.println("La palabra propuesta inicial es: " + palabraPropuesta);
        this.palabrasProhibidas = new ArrayList<String>(capacidadVector);
        jugadoresActuales= new HashMap<>();
        mapaJugadorPalabra= new HashMap<>();
        //letrasAbecedario= new char[]{'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
    }

    public String getPalabraPropuesta(){
        return this.palabraPropuesta;
    }

    private ArrayList<String> addValuesToList(){
        ArrayList<String> lista = new ArrayList<String>();

        lista.add("HOJAS"); lista.add("CASAS");
        lista.add("RELAX");lista.add("ACOTA");
        lista.add("AGUDO");lista.add("GRAVE");
        lista.add("SOLAR"); lista.add("SALIR");
        lista.add("ABETO");lista.add("MATES");
        lista.add("AGUAS");lista.add("MATON");

        lista.add("LUNES");lista.add("ODIAR");
        lista.add("COMER");lista.add("BEBER");
        lista.add("FUMAR");lista.add("FUTIL");
        lista.add("PERRO");lista.add("GATOS");
        lista.add("LOROS");lista.add("CACOS");
        lista.add("CAJON");lista.add("TORTA");

        return lista;
    }


    @Override
    public void run() { // Hilo que cambia la palabra
        while(true){
            try{
                Thread.sleep(10000);
                
                this.nuevaPalabra = palabrasPropuestas.get(rand.nextInt(palabrasPropuestas.size()));
                
                do{
                    nuevaPalabra = palabrasPropuestas.get(rand.nextInt(palabrasPropuestas.size()));
                }while(palabrasProhibidas.contains(nuevaPalabra));
                
                System.out.println("Intento de palabra: " + nuevaPalabra);

                palabraPropuesta = nuevaPalabra;
                palabrasProhibidas.add(palabraPropuesta);

                //System.out.println(palabraPropuesta);

            }catch(InterruptedException ie){
                ie.printStackTrace();
            }
            
            System.out.println(palabrasProhibidas);
        }
    }

    public void resetPart(String nombre) {
        jugadoresActuales.remove(nombre); // Eliminamos el registro del jugador
        mapaJugadorPalabra.remove(nombre); // Eliminamos alguna palabra que quede suelta
        if (numeroJugadores != 0) {
            numeroJugadores--;
        }
        System.out.println("He eliminado la información del jugador " + nombre);
    }

    @Override
    public String iniciarPartida(String nombre) throws RemoteException {
        //resetPart(nombre); // Reseteamos posible informacion sobre el jugador que acaba de entrar
        try {
            jugadoresActuales.put(nombre, getClientHost());
            mapaJugadorPalabra.put(nombre, palabraPropuesta); // Escoge una palabra y la asocia al jugador
            numeroJugadores++;

            System.out.println(mapaJugadorPalabra);

        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        showMessage("Se ha asociado la palabra " + mapaJugadorPalabra.get(nombre) + " al jugador " + nombre);
        return "De acuerdo, " + nombre + " puedes empezar a jugar";
    }

    public String play(String nombre, String intento) throws RemoteException {
        String peticion = "intento jugar con " + intento;
        intento = intento.toUpperCase();
        String palabraCorrespondiente = mapaJugadorPalabra.get(nombre).toUpperCase();
        showRequest(nombre, peticion);

        StringBuilder resultado = new StringBuilder();
        char[] intentoVector = intento.toCharArray();
        char[] propuestaVector = palabraCorrespondiente.toCharArray(); // suponemos minuscula

        for (int i = 0; i < intentoVector.length; i++) {
            if (intentoVector[i] == propuestaVector[i]) { // Si están en la misma posición
                resultado.append("V");
            } else if (contiene(propuestaVector, intentoVector[i])) { // si la propuesta no está en la misma posición,
                                                                      // pero si en otra
                resultado.append("A");
            } else { // si la propuesta no está en la palabra
                resultado.append("G");
            }
        }

        if (resultado.toString().equals("VVVVV")) { // Ha ganado
            showRequest(nombre, nombre + " ha ganado");
            mapaJugadorPalabra.put(nombre, null); //Se elimina la palabra propuesta
            System.out.println("Hola bo dia," + nombre);
            System.out.println(mapaJugadorPalabra);
        } else {
            showRequest(nombre, nombre + " ha perdido");
        }

        return resultado.toString();
    }

    private boolean contiene(char[] vectorCharacter, char letra) {
        int i = 0;
        while (i < vectorCharacter.length && vectorCharacter[i] != letra) {
            i++;
        }

        return (i != vectorCharacter.length); // si ha llegado al final, no ha encontrado nada
    }

    public boolean compruebaPalabra(String intento) {
        if (intento.length() != 5) {
            System.out.println("Esa palabra no vale");
        }
        return true;
    }

    private void showRequest(String nombre, String request) {
        System.out.println("(" + nombre + ") " + request);
    }

    private static void showMessage(String msg) {
        System.out.println("(" + "Server" + ") " + msg);
    }

    public static void main(String[] args) {
        String nombreServidor = args[0]; // Se guarda el nombre
        try {
            Server tsv = new Server(nombreServidor);
            
            Naming.bind("//localhost:1099/" + nombreServidor, tsv);
            Thread hilo = new Thread(tsv, "Timer");
            hilo.start();
            showMessage("Server ready");

        } catch (AlreadyBoundException abe) {
            System.out.println("Server Name already at board");
        } catch (MalformedURLException mue) {
            System.out.println("Malformed URL");
        } catch (RemoteException re) {
            System.out.println("Host unreachable");
        } // catch (InterruptedException e) {
          // e.printStackTrace();
          // }
    }

    @Override
    public String noJuega(String name) throws RemoteException {
        showMessage("El jugador " + name + " no quiere seguir jugando");
        return "De acuerdo";
    }

    private void asociarPalabra(){ //Actualiza las palabras propuestas para los jugadores sin palabras
        Set<String> nombres=mapaJugadorPalabra.keySet();
        for(String nombreJugador : nombres){
            if(mapaJugadorPalabra.get(nombreJugador).equals(null)){
                mapaJugadorPalabra.put(nombreJugador,palabraPropuesta);
            }
        }
    } 
}
