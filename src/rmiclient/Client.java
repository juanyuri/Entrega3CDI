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

public class Client{
    static String nombre;
    static String palabraSend;
    static int intento;

    public Client(){
        this.nombre="";
        this.intento=0;
        this.palabraSend="";
    }

    static String comprobarSolucion(String respuesta){ //VVGVV
        StringBuilder resultado= new StringBuilder();

        for(int i=0;i<respuesta.length();i++){
            switch(respuesta.charAt(i)){
                case 'G':
                    resultado.append(palabraSend.toUpperCase().charAt(i)+" no esta presente en la palabra propuesta\n");
                    break;
                case 'A':
                    resultado.append(palabraSend.toUpperCase().charAt(i)+" esta presente pero no en esa posicion\n");
                    break;
                case 'V':
                    resultado.append(palabraSend.charAt(i)+" esta presente en esa posicion\n");
                    break;
            }
        }
        return resultado.toString();
    }

    public static void showReply(String reply){
        System.out.println("(Server) " + reply);
    }

    public static void main(String[] args) {
        //No se puede exportar nada si el servidor no está levantado
        String reply="";
        try{
        Registry reg= LocateRegistry.getRegistry(1099);
        Wordle replyobj=(Wordle)reg.lookup("MyServer");
        
        System.out.println("Que nombre quieres utilizar en el juego?"); //Se pregunta el nombre
        Scanner scn= new Scanner(System.in);
        nombre= scn.nextLine();

        reply=replyobj.iniciarPartida(nombre);
        showReply(reply);

        do{
            //Procedemos a jugar
            System.out.println("Introduce una palabra: ");
            palabraSend=scn.nextLine();
            reply=replyobj.play(nombre,palabraSend);
            System.out.println("La palabra "+palabraSend+" se ha enviado");
            showReply(reply);
            System.out.println(comprobarSolucion(reply)); //Traduce la solución
            intento++;
        }while(intento<5);

        }catch(NotBoundException nbe){
            System.out.println("Server does not exist");
        }catch(RemoteException re){
            System.out.println("Host unreachable");
        }
    }
}