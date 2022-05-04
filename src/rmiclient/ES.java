package rmiclient;
import java.util.Scanner;

public class ES{
    public static Scanner entrada = new Scanner(System.in);

    /* ES - STRING */
    public static String leerCadena(String msg) {
        String cadena = "";

        do{
            System.out.print(msg);
            cadena = entrada.nextLine().trim();
        }while(cadena.equals(""));
        
        return cadena;
    }

    /* ES - ENTERO */
    public static int leerNumero(String msg){
        boolean repite;
        int toret = 0;
        
        do {
            repite = false;
            System.out.print( msg );

            try {
                toret = Integer.parseInt( entrada.nextLine().trim() );
            } catch (NumberFormatException exc) {
                repite = true;
            }
        } while( repite );

        return toret;
    }
    
    
}