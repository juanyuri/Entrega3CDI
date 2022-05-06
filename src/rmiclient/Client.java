package rmiclient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static rmiclient.ES.*;
import rmiinterface.Wordle;

public class Client{
    String nombre;
    int numIntentos; 

    public Client(String nombre){
        this.nombre = nombre;
        this.numIntentos = 0;
    }

    /** Método que permite mostrar la respuesta del server */
    private void showReply(String reply) {
        System.out.println("(Server) " + reply);
    }

    /** Método que permite mostrar el texto que mostrará el propio cliente */
    private void showMessage(String msg) {
        System.out.println("(" + "Console" + ") " + msg);
    }

    private String visualizar(String palabraSend, String respuesta) { // VVGVV
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < respuesta.length(); i++) {
            switch (respuesta.charAt(i)) {
                case 'G':
                    resultado.append(" -->  ").append(palabraSend.toUpperCase().charAt(i)).append(" inexistente\n");
                    break;
                case 'A':
                    resultado.append(" -->  ").append(palabraSend.toUpperCase().charAt(i)).append(" desordenada\n");
                    break;
                case 'V':
                    resultado.append(" -->  ").append(palabraSend.toUpperCase().charAt(i)).append(" correcta\n");
                    break;
            }
        }
        if(esCorrecta(respuesta)){
            resultado = new StringBuilder();
            resultado.append("La palabra es correcta. Has ganado.");
        }
        return resultado.toString();
    }
    
    /*
     __  __   _   ___ _  _ 
    |  \/  | /_\ |_ _| \| |
    | |\/| |/ _ \ | || .` |
    |_|  |_/_/ \_\___|_|\_|                   

    */

    public static void main(String[] args) {
        try {
            Registry reg = LocateRegistry.getRegistry(1099);
            Wordle objetoRemoto = (Wordle) reg.lookup("MyServer");

            seleccionOpcion(objetoRemoto);

        } catch (NotBoundException nbe){
            System.out.println("El servidor al que intentas conectarte no existe");
        } catch (RemoteException re){
            System.out.println("Host unreachable");
        }
        
    }



    /* Permite seleccionar una opcion, primeramente muestra menu, despues usuario elige */
    public static void seleccionOpcion(Wordle objetoRemoto){
        int op;
        String nombreCliente = leerCadena("Introduce nombre jugador: ");
        Client cliente = new Client(nombreCliente);
        

        // BUCLE PRINCIPAL
        do {
            op = cliente.mostrarMenuYEscoger();
            
            switch(op) {
                case 1:
                    try {
                        String reply = objetoRemoto.iniciarConexion(nombreCliente); // jugador recibe puedes comenzar a jugar
                        System.out.println("(Server) " + reply);
                    }catch(RemoteException ex){
                        System.out.println("Host unreachable.");
                }
                    System.out.println("\n---------------------------------");
                    cliente.bucleIntentosPalabras(objetoRemoto);
                    break;
            }
            
            // Volvemos a preguntar mientras la operación sea distinta de dos, en cuyo caso saldremos
        } while( op != 2 );
    }



    /* Muestra el menu de INICIO */
    private int mostrarMenuYEscoger(){

        // MOSTRAMOS EL MENU
        StringBuilder toret = new StringBuilder();

        toret.append(" ");
        toret.append("__      _____   ___ ___  _    ___ \n");
        toret.append(" \\ \\    / / _ \\ | _ \\   \\| |  | __|\n");
        toret.append("  \\ \\/\\/ / (_) \\|   / |) | |__| _| \n");
        toret.append("   \\_/\\_/ \\___/\\|_|_\\___/|____|___|\n");
        toret.append(" \n");
        toret.append("Jugador: ").append(nombre);
        toret.append("\n");
        toret.append("\nElige una opcion: ");
        toret.append("\n1. Jugar");
        toret.append("\n2. Salir\n");

        System.out.println(toret.toString());
        
        int indice;
        do{    
            indice = leerNumero("Selecciona: ");
        }while(indice <= 0 || indice > 2);

        return indice;
    }


    /* Funcion en la que se realizaran los distintos intentos de palabras */
    private void bucleIntentosPalabras(Wordle objetoRemoto){
        numIntentos = 0;
        boolean acierto = false;

        while(numIntentos < 5 && !acierto){ // 0-4, 5 intentos

            String palabra;
            do{
                palabra = leerCadena("Ingrese nueva palabra (5 letras): ");
            }while(palabra.length() != 5); // comprobamos palabra tenga 5 letras

            try{
               String respuestaServidor = objetoRemoto.play(nombre,palabra);
               String mostrarPantalla = visualizar(palabra,respuestaServidor);
               System.out.println(mostrarPantalla);

               if(!esCorrecta(respuestaServidor)){
                   numIntentos++;
               }else{
                   acierto = true;
               }
            }catch(RemoteException re){
                System.out.println("Error remoto.");
            }

            StringBuilder toret = new StringBuilder();
            toret.append("Intentos: ").append(numIntentos);
            System.out.println(toret.toString());
        }
        System.out.println("---------------------------------\n");
        System.out.println("\n");
    }

    private boolean esCorrecta(String respuesta){
        return(respuesta.equals("VVVVV"));
    }


}