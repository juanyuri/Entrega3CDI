package rmiclient;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import static rmiclient.ES.*;
import rmiinterface.Wordle;

public class Client{
    String nombreCliente;
    int numIntentos; 

    public Client(String nombreCliente){
        this.nombreCliente = nombreCliente;
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

    /** Método que traduce el código dado por el servidor en información entendible por el usuario */
    private String visualizar(String palabraSend, String respuesta) { // VVGVV
        StringBuilder resultado = new StringBuilder();

        if(respuesta.length() == 5){
            for (int i = 0; i < respuesta.length(); i++) {
                switch (respuesta.charAt(i)) {
                    case 'G': //Si la letra no está
                        resultado.append(" -->  ").append(palabraSend.toUpperCase().charAt(i)).append(" inexistente\n");
                        break;
                    case 'A': //Si la letra no está en la pos correcta
                        resultado.append(" -->  ").append(palabraSend.toUpperCase().charAt(i)).append(" desordenada\n");
                        break;
                    case 'V': //Si la letra está bien posicionada
                        resultado.append(" -->  ").append(palabraSend.toUpperCase().charAt(i)).append(" correcta\n");
                        break;
                }
            }
        }else{
            resultado.setLength(0); 
            resultado.append(respuesta); //Si en lugar de un código se envía un mensaje
        }

        if(esCorrecta(respuesta)){ //Si llega un VVVVV, ha ganada
            resultado = new StringBuilder();
            resultado.append("La palabra "+palabraSend.toUpperCase()+" es correcta. Has ganado.");
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
        String nombreCliente= args[0]; //Nombre del cliente
        String nombreServidor= args[1]; //Nombre al que quiere conectarse
        String ipServidor= args[2]; //Ip tablon registry
        int portServidor= Integer.parseInt(args[3]); //Puerto registry

        try {
            Registry reg = LocateRegistry.getRegistry(ipServidor,portServidor); //Accede al registry especificado con esa ip y ese puerto
            Wordle objetoRemoto = (Wordle) reg.lookup(nombreServidor);

            seleccionOpcion(nombreCliente,objetoRemoto); 

        } catch (NotBoundException nbe){
            System.out.println("El servidor al que intentas conectarte no existe");
        } catch (RemoteException re){
            System.out.println("Host unreachable");
        }
        
    }



    /* Permite seleccionar una opcion, primeramente muestra menu, despues usuario elige */
    public static void seleccionOpcion(String nombreCliente, Wordle objetoRemoto){
        int op;
        
        Client cliente = new Client(nombreCliente);
        try {
            String reply = objetoRemoto.iniciarConexion(nombreCliente); // jugador recibe puedes comenzar a jugar
            System.out.println("(Server) " + reply);
        }catch(RemoteException ex){
            System.out.println("Host unreachable.");
        }

        // BUCLE PRINCIPAL
        do {
            op = cliente.mostrarMenuYEscoger();
            
            switch(op) {
                case 1:
                    try {
                        boolean reply = objetoRemoto.asociarPalabra(nombreCliente); 
                        System.out.println("(Server) " + "Palabra asociada al jugador " + nombreCliente + " correctamente.");

                        if(reply){ // Si se pudo asociar correctamente la palabra
                        System.out.println("\n---------------------------------");
                        cliente.bucleIntentosPalabras(objetoRemoto); // dentro esta el play
                    }else{
                        System.out.println("No se ha generado todavia una palabra, espera unos segundos ...");
                    }
                    }catch(RemoteException ex){
                        System.out.println("Host unreachable.");
                    }
                    
                    
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
        toret.append("Jugador: ").append(nombreCliente);
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

        while(numIntentos < 5 && !acierto){ // 5 intentos

            String palabra;
            do{
                palabra = leerCadena("Ingrese nueva palabra (5 letras): ");
            }while(palabra.length() != 5); // comprobamos palabra tenga 5 letras

            try{
               String respuestaServidor;
               respuestaServidor = objetoRemoto.play(nombreCliente,palabra);
               String mostrarPantalla = visualizar(palabra,respuestaServidor);
               System.out.println(mostrarPantalla);

               if(!esCorrecta(respuestaServidor) && !mensaje(respuestaServidor)){ //Si no es correcta y no es un mensaje
                   numIntentos++;
               }else if(esCorrecta(respuestaServidor) || ((mensaje(respuestaServidor) && respuestaServidor.contains("perdido"))) ){ //Si ha ganado o perdido
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

    /** Comprueba si la palabra es correcta */
    private boolean esCorrecta(String respuesta){
            return (respuesta.equals("VVVVV"));
    }

    /** Comprueba si la respuesta del servidor es un mensaje */
    private boolean mensaje(String respuesta){
        if(respuesta.length()>5){
            return true;
        }else{
            return false;
        }
    }

}