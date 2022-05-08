package rmiserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import rmiinterface.Wordle;

public class ServerDebug extends UnicastRemoteObject implements Wordle, Runnable {
    String nombreServidor; //Nombre del servidor
    int numeroJugadores; //Numero jugadores

    String palabraPropuesta; //Palabra objetivo para todos los jugadores que no estén jugando
    String palabraIntento; //Palabra que ha enviado un jugador para poder intentar ganar

    int capacidadVector;
    ArrayList<String> palabrasProhibidas; // Lista de palabras prohibidas, para que no se puedan volver a jugar en un tiempo
    ArrayList<String> palabrasPropuestas; // Lista de posibles palabras

    Random rand = new Random();
             
    HashMap<String,ServerPlayer> mapaJugadores;
    HashMap<Character, List<String>> mapaPalabrasPosibles;


    public ServerDebug(String nombreServidor) throws RemoteException {
        this.nombreServidor = nombreServidor;
        this.numeroJugadores = 0; //Inicializamos el número de jugadores a 0
        this.capacidadVector = 4; //Cada 3 palabras quitaremos una de la lista de baneadas
        this.palabrasPropuestas = addValuesToList(); //Lista de palabras que saldrán como objetivo para adivinar
        this.palabraPropuesta = generarPalabraPropuesta(); //Se genera una nueva palabra propuesta
        this.palabrasProhibidas = new ArrayList<String>(capacidadVector); //Lista de palabras prohibidas vacia
        this.mapaJugadores = new HashMap<String,ServerPlayer>(); //Mapa que relaciona un jugador con información útil, como la palabra que está jugando, su número de intentos o si está jugando
        this.mapaPalabrasPosibles = crearDiccionario(); //Diccionario de palabras válidas de 5 letras
    }
    
    /** Genera palabras para jugar*/
    private String generarPalabraPropuesta(){
        return palabrasPropuestas.get(rand.nextInt(palabrasPropuestas.size()));
    }

    /** Añade a la lista de palabras posibles algunas seleccionadas */
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

    /** Creamos el diccionario al inicio de la ejecución del Servidor */
    private HashMap<Character, List<String>> crearDiccionario() {
        // Genera el mapa de palabras posibles
        char[] letrasAbecedario = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' }; //Para indexar por letra el mapa

        HashMap<Character, List<String>> diccionarioPalabras = new HashMap<Character, List<String>>();
        for (int i = 0; i < letrasAbecedario.length; i++) { // Inicializamos claves
            diccionarioPalabras.put(letrasAbecedario[i], new LinkedList<String>());
        }
        try {
            File doc = new File("..\\src\\5palabras.txt"); //Accedemos al contenido del fichero de palabras
            Scanner obj = new Scanner(doc);
            while (obj.hasNextLine()) {
                for (String palabra : obj.nextLine().split(" ")) {
                    // System.out.println(palabra);
                    char clave = palabra.charAt(0);
                    diccionarioPalabras.get(clave).add(palabra); // Añadimos las palabras al mapa indexado por su inicial
                }
            }
        } catch (FileNotFoundException fe) {
            System.out.println("No se ha encontrado el archivo");
        }
        showMessage("Server inicializado correctamente");
        return diccionarioPalabras;
    }    



    /** Inicializa un jugador que quiere acceder al servidor */
    public String iniciarConexion(String nombre) throws RemoteException {
        showMessage("El usuario " + nombre + " ha entrado en el servidor"); //Informa del acceso 

        mapaJugadores.put(nombre, new ServerPlayer()); //Añadimos un nuevo jugador al mapa
        numeroJugadores++;
        
        return "De acuerdo, " + nombre + ", puedes empezar a jugar";
    }

    /** Cada vez que el cliente quiere jugar pide que se le asocie una palabra */
    public boolean asociarPalabra(String nombre){
        String palabraAsociada = mapaJugadores.get(nombre).getPalabraAsociada(); //Miramos la que tiene asociada
        boolean estaJugando = mapaJugadores.get(nombre).estaJugando(); //y si está jugando
        

        if(palabraAsociada == null){ //Si no tiene ninguna
            // Si no tiene palabra asociada
            mapaJugadores.get(nombre).setPalabraAsociada(palabraPropuesta); //Se le asocia la actual

            showMessage("Palabra asociada al jugador(antes vacia) " + nombre + ": " + mapaJugadores.get(nombre).getPalabraAsociada());
            visualizarJugadores();
            mapaJugadores.get(nombre).setNumIntentos(0); //Iniciamos el numero de intentos
            mapaJugadores.get(nombre).setJugando(true); //Si está jugando
            return true;
        }else if(mapaJugadores.get(nombre).getPalabraAsociada().equals(palabraPropuesta) && !estaJugando){ // si ha ganado ha terminado de jugar
            // Si ha jugado la partida y la palabra propuesta sigue siendo la misma
            visualizarJugadores();
            return false;
        }else if(mapaJugadores.get(nombre).getPalabraAsociada().equals(palabraPropuesta) && estaJugando){
            // Si esta jugando
            visualizarJugadores();
            return false;
        }else if(!mapaJugadores.get(nombre).getPalabraAsociada().equals(palabraPropuesta) && !estaJugando){
            // Si la palabra propuesta no es la misma y ha terminado de jugar
            mapaJugadores.get(nombre).setPalabraAsociada(palabraPropuesta);
            showMessage("Palabra asociada al jugador(ya generada nueva) " + nombre + ": " + mapaJugadores.get(nombre).getPalabraAsociada());
            visualizarJugadores();
            mapaJugadores.get(nombre).setNumIntentos(0);
            mapaJugadores.get(nombre).setJugando(true);
            return true;
        }

        return false;
    }

    /** El cliente envia una palabra para poder jugar */
    public String play(String nombre, String intento){
        mapaJugadores.get(nombre).setJugando(true); //El jugador ha empezado a jugar

        intento = intento.toUpperCase(); //Convertimos la palabra en mayúscula
        showMessage("El jugador " + nombre + " ha intentado jugar con: " + intento);

        String palabraCorrespondiente = mapaJugadores.get(nombre).getPalabraAsociada().toUpperCase(); //Miramos cual le toca
            
            String resultado;

            if(comprobarPalabra(nombre,intento)){ //Si la palabra tiene 5 letras y está en el diccionario
                mapaJugadores.get(nombre).aumentarIntento(); // numIntentos++
                showMessage("El jugador "+nombre+" lleva "+mapaJugadores.get(nombre).getNumIntentos()+" intentos");
                resultado = generarResultado(intento,palabraCorrespondiente); //Enviamos el código de comprobación al cliente

                if(resultado.equals("VVVVV")) { // Ha ganado
                    showMessage("El jugador " + nombre + " ha ganado");
                    mapaJugadores.get(nombre).setJugando(false); //Para de jugar
                    //visualizarJugadores();
                }else if(mapaJugadores.get(nombre).getNumIntentos() >= 5){
                    showMessage("El jugador " + nombre + " ha perdido");
                    mapaJugadores.get(nombre).setJugando(false); //Para de jugar
                    
                    resultado = "Has perdido, la palabra correcta era "+ mapaJugadores.get(nombre).getPalabraAsociada();
                }

            }else{ //Si la palabra no es válida se informa al cliente
                resultado = "La palabra no es valida. O bien no tiene 5 letras o no es una palabra real";
            }

        return resultado;
    }


    public static void main(String[] args) {
        String nombreServidor = args[0]; // Se guarda el nombre
        String ipServidor= args[1]; //Ip de dónde queremos establecer el servidor 
        int puertoServidor= Integer.parseInt(args[2]); //Puerto
        try {
            ServerDebug tsv = new ServerDebug(nombreServidor); //Creamos un servidor

            Naming.bind("//"+ipServidor+":"+puertoServidor+"/" + nombreServidor, tsv);
            Thread hilo = new Thread(tsv, "Timer"); //Creamos un hilo encargado de cambiar las palabras
            hilo.start();

        } catch (AlreadyBoundException abe) {
            System.out.println("Server Name already at board");
        } catch (MalformedURLException mue) {
            System.out.println("Malformed URL");
        } catch (RemoteException re) {
            System.out.println("Host unreachable");
        } 
    }

    @Override
    /** Un hilo se encarga de actualizar la palabra objetivo cada 5 min */
    public void run() { // Hilo que cambia la palabra
        while (true) {
            try {
                Thread.sleep(300000); //Espera 5 minutos

                do {
                    palabraPropuesta = generarPalabraPropuesta(); //Genera una palabra mientras no esté en la lista negra
                } while (palabrasProhibidas.contains(palabraPropuesta));

                System.out.println("(" + "Server" + ") " + "Palabra propuesta nueva: " + palabraPropuesta);
                palabrasProhibidas.add(palabraPropuesta); //Se añade a la lista
                checkProhibidas(); // Comprueba si ya han pasado las palabras necesarias para sacar una en la lista de prohibidas

            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
    

    /* COMPROBACIONES */
    /** Comprueba si una letra está en un vector*/
    private boolean contiene(char[] vectorCharacter, char letra) {
        int i = 0;
        while (i < vectorCharacter.length && vectorCharacter[i] != letra) {
            i++;
        }

        return (i != vectorCharacter.length); // si ha llegado al final, no ha encontrado nada
    }

    /** Comprueba que la palabra introducida por el usuario es correcta */
    private boolean comprobarPalabra(String nombre, String intento) {
        char letraInicial = intento.charAt(0); //Recogemos la letra inicial
        if (intento.length() != 5 || !mapaPalabrasPosibles.get(letraInicial).contains(intento)) { // Si la palabra no tiene 5 letras o no está en el diccionario
            return false; // No se puede realizar esa jugada
        } else {
            return true;
        }
    }

    /** Quita una palabra de la lista negra cuando se ñan añadido 3 más FIFO */
    private void checkProhibidas() {
        if (palabrasProhibidas.size() >= capacidadVector) {
            palabrasProhibidas.remove(0);
        }
    }

    /** Comprueba que letras están o no bien colocadas comparando el intento del usuario con la palabra que le ha tocado*/
    public String generarResultado(String intento, String palabraCorrespondiente){
        StringBuilder resultado = new StringBuilder();
        char[] intentoVector = intento.toCharArray();
        char[] propuestaVector = palabraCorrespondiente.toCharArray(); // suponemos minuscula

        for (int i = 0; i < intentoVector.length; i++) {
            if (intentoVector[i] == propuestaVector[i]) { 
                // misma posición(correcta)
                resultado.append("V");
            } else if (contiene(propuestaVector, intentoVector[i])) { 
                // distinta posición pero está contenida
                resultado.append("A");
            } else { 
                // no están en la palabra
                resultado.append("G");
            }
        }
        return resultado.toString();
    }


    /* SALIDA - OUTPUT */
    /** Muestra mensaje del servidor */
    private void showMessage(String msg) {
        System.out.println("(" + "Server" + ") " + msg);
    }

    /** Muestra los jugadores con sus palabras asociadas */
    private void visualizarJugadores(){
        StringBuilder toret = new StringBuilder();

        toret.append("[ ");
        for(String nombreCliente : mapaJugadores.keySet()){
            toret.append("(");
            toret.append(nombreCliente).append(", ").append(mapaJugadores.get(nombreCliente).getPalabraAsociada());
            toret.append(") ");
        }
        toret.append(" ]");

        System.out.println(toret.toString());
    }
}