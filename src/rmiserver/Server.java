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

public class Server extends UnicastRemoteObject implements Wordle, Runnable {
    String nombreServidor; 
    int numeroJugadores; 

    String palabraPropuesta;
    String palabraIntento;

    int capacidadVector;
    ArrayList<String> palabrasProhibidas;  
    ArrayList<String> palabrasPropuestas;  

    Random rand = new Random();
             
    HashMap<String,ServerPlayer> mapaJugadores;
    HashMap<Character, List<String>> mapaPalabrasPosibles;


    public Server(String nombreServidor) throws RemoteException {
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
        char[] letrasAbecedario = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };

        HashMap<Character, List<String>> diccionarioPalabras = new HashMap<Character, List<String>>();
        for (int i = 0; i < letrasAbecedario.length; i++) {
            diccionarioPalabras.put(letrasAbecedario[i], new LinkedList<String>());
        }
        try {
            File doc = new File("..\\src\\5palabras.txt");
            Scanner obj = new Scanner(doc);
            while (obj.hasNextLine()) {
                 
                 
                for (String palabra : obj.nextLine().split(" ")) {
                     
                    char clave = palabra.charAt(0);
                    diccionarioPalabras.get(clave).add(palabra);  
                }
            }
             
        } catch (FileNotFoundException fe) {
            System.out.println("No se ha encontrado el archivo");
        }
        //showMessage("Server inicializado correctamente");
        return diccionarioPalabras;
    }    




    public String iniciarConexion(String nombre) throws RemoteException {
        //showMessage("El usuario " + nombre + " ha entrado en el servidor");

        mapaJugadores.put(nombre, new ServerPlayer());
        numeroJugadores++;
        
        return "De acuerdo, " + nombre + ", puedes empezar a jugar";
    }


    public boolean asociarPalabra(String nombre){
        String palabraAsociada = mapaJugadores.get(nombre).getPalabraAsociada();
        boolean estaJugando = mapaJugadores.get(nombre).estaJugando();
        

        if(palabraAsociada == null){
             
            mapaJugadores.get(nombre).setPalabraAsociada(palabraPropuesta);

            ////showMessage("Palabra asociada al jugador(antes vacia) " + nombre + ": " + mapaJugadores.get(nombre).getPalabraAsociada());
            visualizarJugadores();
            mapaJugadores.get(nombre).setNumIntentos(0);
            mapaJugadores.get(nombre).setJugando(true);
            return true;
        }else if(mapaJugadores.get(nombre).getPalabraAsociada().equals(palabraPropuesta) && !estaJugando){  
             
            visualizarJugadores();
            return false;
        }else if(mapaJugadores.get(nombre).getPalabraAsociada().equals(palabraPropuesta) && estaJugando){
             
            visualizarJugadores();
            return false;
        }else if(!mapaJugadores.get(nombre).getPalabraAsociada().equals(palabraPropuesta) && !estaJugando){
             
            mapaJugadores.get(nombre).setPalabraAsociada(palabraPropuesta);
            ////showMessage("Palabra asociada al jugador(ya generada nueva) " + nombre + ": " + mapaJugadores.get(nombre).getPalabraAsociada());
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
        //showMessage("El jugador " + nombre + " ha intentado jugar con: " + intento);

        String palabraCorrespondiente = mapaJugadores.get(nombre).getPalabraAsociada().toUpperCase();
            
            String resultado;

            if(comprobarPalabra(nombre,intento)){
                mapaJugadores.get(nombre).aumentarIntento();  
                //showMessage("El jugador "+nombre+" lleva "+mapaJugadores.get(nombre).getNumIntentos()+" intentos");
                resultado = generarResultado(intento,palabraCorrespondiente);

                if(resultado.equals("VVVVV")) {  
                    //showMessage("El jugador " + nombre + " ha ganado");
                    mapaJugadores.get(nombre).setJugando(false);
                     
                }else if(mapaJugadores.get(nombre).getNumIntentos() >= 5){
                    //showMessage("El jugador " + nombre + " ha perdido");
                    mapaJugadores.get(nombre).setJugando(false);
                    
                    resultado = "Has perdido, la palabra correcta era "+ mapaJugadores.get(nombre).getPalabraAsociada();
                }

            }else{
                resultado = "La palabra no es valida. O bien no tiene 5 letras o no es una palabra real";
            }

        return resultado;
    }

    
    public static void main(String[] args) {
        String nombreServidor = args[0];  
        String ipServidor= args[1];
        int puertoServidor= Integer.parseInt(args[2]);
        try {
            Server tsv = new Server(nombreServidor);

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
    public void run() {  
        while (true) {
            try {
                Thread.sleep(25000);

                do {
                    palabraPropuesta = generarPalabraPropuesta();
                } while (palabrasProhibidas.contains(palabraPropuesta));

                System.out.println("(" + "Server" + ") " + "Palabra propuesta nueva: " + palabraPropuesta);
                palabrasProhibidas.add(palabraPropuesta);
                checkProhibidas();  

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

        return (i != vectorCharacter.length);  
    }

    private boolean comprobarPalabra(String nombre, String intento) {
        char letraInicial = intento.charAt(0);
        if (intento.length() != 5 || !mapaPalabrasPosibles.get(letraInicial).contains(intento)) {  
            return false;  
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
        char[] propuestaVector = palabraCorrespondiente.toCharArray();  

        for (int i = 0; i < intentoVector.length; i++) {
            if (intentoVector[i] == propuestaVector[i]) { 
                 
                resultado.append("V");
            } else if (contiene(propuestaVector, intentoVector[i])) { 
                 
                resultado.append("A");
            } else { 
                 
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

        //System.out.println(toret.toString());
    }
}