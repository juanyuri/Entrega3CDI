package rmiserver;

public class ServerPlayer{
    int numIntentos;
    String palabraAsociada;
    boolean jugando;

    public ServerPlayer(){
        this.numIntentos = 0;
        this.palabraAsociada = null;
        this.jugando = false;
    }

    public int getNumIntentos(){
        return numIntentos;
    }

    public String getPalabraAsociada(){
        return palabraAsociada;
    }

    public boolean estaJugando(){
        return this.jugando;
    }


    public void setNumIntentos(int nuevoNumIntentos){
        this.numIntentos = nuevoNumIntentos;
    }

    public void setPalabraAsociada(String nuevaPalabraAsociada){
        this.palabraAsociada = nuevaPalabraAsociada;
    }

    public void aumentarIntento(){
        this.numIntentos += 1;
        
    }

    public void setJugando(boolean jugando){
        this.jugando = jugando;
    }

}