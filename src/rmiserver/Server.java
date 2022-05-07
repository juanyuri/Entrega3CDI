package rmiserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import rmiinterface.Wordle;

public class Server extends UnicastRemoteObject implements Wordle, Runnable {
    String nombreServidor; // Nombre del servidor, se le pasa como parametro
    int numeroJugadores; // Numero de jugadores actuales

    String palabraPropuesta; // Palabra propuesta general, se actualiza cada cierto tiempo
    String nuevaPalabra;
    String palabraIntento;

    int capacidadVector;
    ArrayList<String> palabrasProhibidas; // Lista de palabras prohibidas, para que no se puedan volver a jugar en un
                                          // tiempo
    ArrayList<String> palabrasPropuestas; // Lista de posibles palabras

    Random rand = new Random();

    char[] letrasAbecedario; // Vector de las letras del abecedario, para realizar búsquedas de manera mas
                             // eficaz

    Map<String, Integer> mapaJugadorIntento; // Almacena los intentos de los jugadores
    Map<Character, List<String>> mapaPalabrasPosibles; // Diccionario de palabras
    Map<String, String> mapaJugadorPalabra; // Almacena los jugadores y sus palabras asociadas
    Map<String, String> jugadoresActuales; // Lista de jugadores con sus IP

    public Server(String nombreServidor) throws RemoteException {
        this.nombreServidor = nombreServidor;
        this.numeroJugadores = 0;
        this.nuevaPalabra = "";
        this.capacidadVector = 4;
        this.palabrasPropuestas = addValuesToList();
        this.palabraPropuesta = palabrasPropuestas.get(rand.nextInt(palabrasPropuestas.size()));

        this.palabrasProhibidas = new ArrayList<String>(capacidadVector);
        jugadoresActuales = new HashMap<>();
        mapaJugadorPalabra = new HashMap<>();
        mapaJugadorIntento = new HashMap<>();
        letrasAbecedario = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
                'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

        this.mapaPalabrasPosibles = crearDiccionario(); // Creamos el diccionario
        // System.out.println(mapaPalabrasPosibles);
    }

    private void checkProhibidas() {
        if (palabrasProhibidas.size() >= capacidadVector) {// Si la lista de prohibidas es lo suficientemente grande
            palabrasProhibidas.remove(0);
        }
    }

    private ArrayList<String> addValuesToList() {
        ArrayList<String> lista = new ArrayList<String>();

        lista.add("HOJAS");
        lista.add("CASAS");
        lista.add("RELAX");
        lista.add("ACOTA");
        lista.add("AGUDO");
        lista.add("GRAVE");
        lista.add("SOLAR");
        lista.add("SALIR");
        lista.add("ABETO");
        lista.add("MATES");
        lista.add("AGUAS");
        lista.add("MATON");

        lista.add("LUNES");
        lista.add("ODIAR");
        lista.add("COMER");
        lista.add("BEBER");
        lista.add("FUMAR");
        lista.add("FUTIL");
        lista.add("PERRO");
        lista.add("GATOS");
        lista.add("LOROS");
        lista.add("CACOS");
        lista.add("CAJON");
        lista.add("TORTA");

        return lista;
    }

    @Override
    public void run() { // Hilo que cambia la palabra
        while (true) {
            try {
                Thread.sleep(10000);

                this.nuevaPalabra = palabrasPropuestas.get(rand.nextInt(palabrasPropuestas.size()));

                do {
                    nuevaPalabra = palabrasPropuestas.get(rand.nextInt(palabrasPropuestas.size()));
                } while (palabrasProhibidas.contains(nuevaPalabra));

                //System.out.println("Intento de palabra: " + nuevaPalabra);

                palabraPropuesta = nuevaPalabra;
                palabrasProhibidas.add(palabraPropuesta);
                checkProhibidas(); // Comprueba si ya han pasado las palabras necesarias para sacar una en la lista
                                   // de prohibidas

                // System.out.println(palabraPropuesta);

            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }

           //System.out.println(palabrasProhibidas);
        }
    }

    private void resetPart(String nombre) {
        jugadoresActuales.remove(nombre); // Eliminamos el registro del jugador
        mapaJugadorPalabra.remove(nombre); // Eliminamos alguna palabra que quede suelta
        if (numeroJugadores != 0) {
            numeroJugadores--;
        }
        System.out.println("He eliminado la información del jugador " + nombre);
    }

    @Override
    public String iniciarConexion(String nombre) throws RemoteException {
        //resetPart(nombre); // Reseteamos posible informacion sobre el jugador que acaba de entrar
        showMessage("El usuario " + nombre + " ha entrado en el servidor");
        try {
            jugadoresActuales.put(nombre, getClientHost());
            //System.out.println(jugadoresActuales);
            mapaJugadorPalabra.put(nombre, palabraPropuesta); // Escoge una palabra y la asocia al jugador
            mapaJugadorIntento.put(nombre, 0); // Al iniciar conexion se asocia un intento
            numeroJugadores++;

            System.out.println(mapaJugadorPalabra);

        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
        showMessage("Se ha asociado la palabra " + mapaJugadorPalabra.get(nombre) + " al jugador " + nombre);
        return "De acuerdo, " + nombre + " puedes empezar a jugar";
    }

    public String play(String nombre, String intento){
        String peticion = "intento jugar con " + intento;
        intento = intento.toUpperCase();

        String palabraCorrespondiente = mapaJugadorPalabra.get(nombre).toUpperCase();
        showRequest(nombre, peticion);

        StringBuilder resultado = new StringBuilder();
        char[] intentoVector = intento.toCharArray();
        char[] propuestaVector = palabraCorrespondiente.toCharArray(); // suponemos minuscula

        if (compruebaPalabra(nombre, intento)) {

            int intentoJugador = mapaJugadorIntento.get(nombre); // Recogemos el numero de intento
            mapaJugadorIntento.put(nombre, intentoJugador + 1); // Lo actualizamos

            System.out.println(mapaJugadorIntento);

            for (int i = 0; i < intentoVector.length; i++) {
                if (intentoVector[i] == propuestaVector[i]) { // Si están en la misma posición
                    resultado.append("V");
                } else if (contiene(propuestaVector, intentoVector[i])) { // si la propuesta no está en la misma
                                                                          // posición,
                                                                          // pero si en otra
                    resultado.append("A");
                } else { // si la propuesta no está en la palabra
                    resultado.append("G");
                }
            }

            if (resultado.toString().equals("VVVVV")) { // Ha ganado
                showRequest(nombre, nombre + " ha ganado");
                mapaJugadorPalabra.put(nombre, null); // Se elimina la palabra propuesta
                // System.out.println("Hola bo dia," + nombre);
                System.out.println(mapaJugadorPalabra);
            } else if (mapaJugadorIntento.get(nombre) >= 5) { // Si ha fallado hasta el ultimo intento
                showRequest(nombre, nombre + " ha perdido");
                mapaJugadorIntento.remove(nombre); // Reiniciamos sus intentos
                resultado.setLength(0);
                resultado.append("Has perdido, la palabra correcta era "+mapaJugadorPalabra.get(nombre)); 
            }
        } else {
            resultado.setLength(0);
            resultado.append("La palabra no es valida. O bien no tiene 5 letras o no es una palabra real");
        }
        return resultado.toString();
    }

    private HashMap<Character, List<String>> crearDiccionario() {
        // Genera el mapa de palabras posibles
        HashMap<Character, List<String>> diccionarioPalabras = new HashMap<Character, List<String>>();
        for (int i = 0; i < letrasAbecedario.length; i++) { // Inicializamos claves
            diccionarioPalabras.put(letrasAbecedario[i], new LinkedList<String>());
        }
        try {
            File doc = new File("..\\src\\5palabras.txt");
            Scanner obj = new Scanner(doc);
            while (obj.hasNextLine()) {
                // System.out.println("Puedo sacar un archivo");
                // System.out.println("He sacado "+obj.next());
                for (String palabra : obj.nextLine().split(" ")) {
                    // System.out.println(palabra);
                    char clave = palabra.charAt(0);
                    diccionarioPalabras.get(clave).add(palabra); // Añadimos las palabras al array
                }
            }
            // System.out.println(diccionarioPalabras);
        } catch (FileNotFoundException fe) {
            System.out.println("No se ha encontrado el archivo");
        }
        showMessage("Server ready");
        return diccionarioPalabras;
    }

    private boolean contiene(char[] vectorCharacter, char letra) {
        int i = 0;
        while (i < vectorCharacter.length && vectorCharacter[i] != letra) {
            i++;
        }

        return (i != vectorCharacter.length); // si ha llegado al final, no ha encontrado nada
    }

    private boolean compruebaPalabra(String nombre, String intento) {
        char letraInicial = intento.charAt(0);
        if (intento.length() != 5 || !mapaPalabrasPosibles.get(letraInicial).contains(intento)) { // Si la palabra no tiene 5 letras, no esta
                                                                                                  // presente en el diccionario
            return false; // No se puede realizar esa jugada
        } else {
            return true;
        }
    }

    private void showRequest(String nombre, String request) {
        System.out.println("(" + nombre + ") " + request);
    }

    private static void showMessage(String msg) {
        System.out.println("(" + "Server" + ") " + msg);
    }

    public static void main(String[] args) {
        String nombreServidor = args[0]; // Se guarda el nombre
        String ipServidor= args[1];
        int puertoServidor= Integer.parseInt(args[2]);
        try {
            Server tsv = new Server(nombreServidor);

            Naming.bind("//"+ipServidor+":"+puertoServidor+"/" + nombreServidor, tsv);
            Thread hilo = new Thread(tsv, "Timer");
            hilo.start();
            //showMessage("Server ready");

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
}