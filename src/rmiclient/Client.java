package rmiclient;

/*Para compilar: javac rmiinterface/*.java rmiserver/*.java rmiclient/*.java
  Para ejecutar: start rmiregistry
                 java rmiserver/Server
*/
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import rmiinterface.Wordle;

public class Client {
    static String nombre;
    static String palabraSend;
    static int intento;

    public Client() {
        this.nombre = "";
        this.intento = 0;
        this.palabraSend = "";
    }

    static String comprobarSolucion(String respuesta) { // VVGVV
        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < respuesta.length(); i++) {
            switch (respuesta.charAt(i)) {
                case 'G':
                    resultado.append(
                            palabraSend.toUpperCase().charAt(i) + " no esta presente en la palabra propuesta\n");
                    break;
                case 'A':
                    resultado.append(palabraSend.toUpperCase().charAt(i) + " esta presente pero no en esa posicion\n");
                    break;
                case 'V':
                    resultado.append(palabraSend.charAt(i) + " esta presente en esa posicion\n");
                    break;
            }
        }
        return resultado.toString();
    }

    public static void showReply(String reply) {
        System.out.println("(Server) " + reply);
    }

    private static void showMessage(String msg) {
        System.out.println("(" + nombre + ") " + msg);
    }

    public static void jugar(String reply, Wordle replyobj, Scanner scn, boolean gane){
        gane=false;
        do {
            // Procedemos a jugar
            System.out.println("Introduce una palabra: ");
            palabraSend = scn.nextLine();
            try {
                reply = replyobj.play(nombre, palabraSend);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            System.out.println("La palabra " + palabraSend + " se ha enviado");
            showReply(reply);
            System.out.println(comprobarSolucion(reply)); // Traduce la solución
            intento++;
            if (reply.equals("VVVVV")) { // Si ha averiguado la palabra en tiempo
                gane = true;
            }
        } while (intento < 5 && !gane);
    }

    public static void menu(String reply, Wordle replyobj, Scanner scn, boolean gane){
        if (gane) {
            showMessage("He ganado");
            // menu();
            System.out.println("Quieres seguir jugando (y/n)");
            String option = scn.nextLine().toUpperCase();

            switch (option) {
                case "Y":
                    try {
                        reply = replyobj.iniciarPartida(nombre);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    jugar(reply, replyobj, scn, gane);
                    menu(reply, replyobj, scn, gane);
                    showReply(reply);
                    gane=false;
                    break;
                case "N":
                    try {
                        reply = replyobj.noJuega(nombre);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    showReply(reply);
                    break;
                default:
                    try {
                        reply = replyobj.noJuega(nombre);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    showReply(reply);
                    break;
            }
        } else {
            showMessage("He perdido");
            // menu();
            System.out.println("Quieres seguir jugando (y/n)");
            String option = scn.nextLine().toUpperCase();

            switch (option) {
                case "Y":
                    try {
                        reply = replyobj.iniciarPartida(nombre);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    jugar(reply, replyobj, scn, gane);
                    menu(reply, replyobj, scn, gane);
                    showReply(reply);
                    break;
                case "N":
                    try {
                        reply = replyobj.noJuega(nombre);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    showReply(reply);
                    break;
            }
        }
    }
    public static void main(String[] args) {
        boolean gane = false;
        String reply = "";
        try {
            Registry reg = LocateRegistry.getRegistry(1099);
            Wordle replyobj = (Wordle) reg.lookup("MyServer");

            System.out.println("Que nombre quieres utilizar en el juego?"); // Se pregunta el nombre
            Scanner scn = new Scanner(System.in);
            nombre = scn.nextLine();

            reply = replyobj.iniciarPartida(nombre);
            showReply(reply);

            jugar(reply, replyobj, scn, gane);
            menu(reply, replyobj, scn, gane);

        } catch (NotBoundException nbe) {
            System.out.println("Server does not exist");
        } catch (RemoteException re) {
            System.out.println("Host unreachable");
        }
    }
}