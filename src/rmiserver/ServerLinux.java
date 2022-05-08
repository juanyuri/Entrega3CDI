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

public class ServerLinux extends UnicastRemoteObject implements Wordle, Runnable {
    String nombreServidor; 
    int numeroJugadores; 

    String palabraPropuesta;
    String palabraIntento;

    int capacidadVector;
    ArrayList<String> palabrasProhibidas; // Lista de palabras prohibidas, para que no se puedan volver a jugar en un tiempo
    ArrayList<String> palabrasPropuestas; // Lista de posibles palabras

    Random rand = new Random();
             
    HashMap<String,ServerPlayer> mapaJugadores;
    HashMap<Character, List<String>> mapaPalabrasPosibles;


    public ServerLinux(String nombreServidor) throws RemoteException {
        this.nombreServidor = nombreServidor;
        this.numeroJugadores = 0;
        this.capacidadVector = 4;
        this.palabrasPropuestas = addValuesToList();
        this.palabraPropuesta = generarPalabraPropuesta();
        this.palabrasProhibidas = new ArrayList<String>(capacidadVector);
        this.mapaJugadores = new HashMap<String,ServerPlayer>();
        this.mapaPalabrasPosibles = crearDiccionario();
    }
    
    private String generarPalabraPropuesta(){
        return palabrasPropuestas.get(rand.nextInt(palabrasPropuestas.size()));
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

    private HashMap<Character, List<String>> crearDiccionario() {
        // Genera el mapa de palabras posibles
        char[] letrasAbecedario = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

        HashMap<Character, List<String>> diccionarioPalabras = new HashMap<Character, List<String>>();
        for (int i = 0; i < letrasAbecedario.length; i++) { // Inicializamos claves
            diccionarioPalabras.put(letrasAbecedario[i], new LinkedList<String>());
        }
        try {
            File doc = new File("./5palabras.txt");
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
        showMessage("Server inicializado correctamente");
        return diccionarioPalabras;
    }    




    public String iniciarConexion(String nombre) throws RemoteException {
        showMessage("El usuario " + nombre + " ha entrado en el servidor");

        mapaJugadores.put(nombre, new ServerPlayer());
        numeroJugadores++;
        
        return "De acuerdo, " + nombre + ", puedes empezar a jugar";
    }


    public boolean asociarPalabra(String nombre){
        String palabraAsociada = mapaJugadores.get(nombre).getPalabraAsociada();
        boolean estaJugando = mapaJugadores.get(nombre).estaJugando();
        

        if(palabraAsociada == null){
            // Si no tiene palabra asociada
            mapaJugadores.get(nombre).setPalabraAsociada(palabraPropuesta);

            showMessage("Palabra asociada al jugador(antes vacia) " + nombre + ": " + mapaJugadores.get(nombre).getPalabraAsociada());
            visualizarJugadores();
            mapaJugadores.get(nombre).setNumIntentos(0);
            mapaJugadores.get(nombre).setJugando(true);
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


    public String play(String nombre, String intento){
        mapaJugadores.get(nombre).setJugando(true);

        intento = intento.toUpperCase();
        showMessage("El jugador " + nombre + " ha intentado jugar con: " + intento);

        String palabraCorrespondiente = mapaJugadores.get(nombre).getPalabraAsociada().toUpperCase();
            
            String resultado;

            if(comprobarPalabra(nombre,intento)){
                mapaJugadores.get(nombre).aumentarIntento(); // numIntentos++
                showMessage("El jugador "+nombre+" lleva "+mapaJugadores.get(nombre).getNumIntentos()+" intentos");
                resultado = generarResultado(intento,palabraCorrespondiente);

                if(resultado.equals("VVVVV")) { // Ha ganado
                    showMessage("El jugador " + nombre + " ha ganado");
                    mapaJugadores.get(nombre).setJugando(false);
                    //visualizarJugadores();
                }else if(mapaJugadores.get(nombre).getNumIntentos() >= 5){
                    showMessage("El jugador " + nombre + " ha perdido");
                    mapaJugadores.get(nombre).setJugando(false);
                    
                    resultado = "Has perdido, la palabra correcta era "+ mapaJugadores.get(nombre).getPalabraAsociada();
                }

            }else{
                resultado = "La palabra no es valida. O bien no tiene 5 letras o no es una palabra real";
            }

        return resultado;
    }

    


    

    public static void main(String[] args) {
        String nombreServidor = args[0]; // Se guarda el nombre
        String ipServidor= args[1];
        int puertoServidor= Integer.parseInt(args[2]);
        try {
            ServerLinux tsv = new ServerLinux(nombreServidor);

            Naming.bind("//"+ipServidor+":"+puertoServidor+"/" + nombreServidor, tsv);
            Thread hilo = new Thread(tsv, "Timer");
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
    public void run() { // Hilo que cambia la palabra
        while (true) {
            try {
                Thread.sleep(25000);

                do {
                    palabraPropuesta = generarPalabraPropuesta();
                } while (palabrasProhibidas.contains(palabraPropuesta));

                System.out.println("(" + "Server" + ") " + "Palabra propuesta nueva: " + palabraPropuesta);
                palabrasProhibidas.add(palabraPropuesta);
                checkProhibidas(); // Comprueba si ya han pasado las palabras necesarias para sacar una en la lista de prohibidas

            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
        }
    }
    






    /* COMPROBACIONES */
    private boolean contiene(char[] vectorCharacter, char letra) {
        int i = 0;
        while (i < vectorCharacter.length && vectorCharacter[i] != letra) {
            i++;
        }

        return (i != vectorCharacter.length); // si ha llegado al final, no ha encontrado nada
    }

    private boolean comprobarPalabra(String nombre, String intento) {
        char letraInicial = intento.charAt(0);
        if (intento.length() != 5 || !mapaPalabrasPosibles.get(letraInicial).contains(intento)) { // Si la palabra no tiene 5 letras, no esta
            return false; // No se puede realizar esa jugada
        } else {
            return true;
        }
    }

    private void checkProhibidas() {
        if (palabrasProhibidas.size() >= capacidadVector) {
            palabrasProhibidas.remove(0);
        }
    }

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
    private void showMessage(String msg) {
        System.out.println("(" + "Server" + ") " + msg);
    }

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