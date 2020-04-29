import java.net.*;
import javax.net.ssl.*; 
import java.io.*; 
import java.util.*; 
import java.lang.*; 



/*
Création du serveur sur le port 8500
*/
class Server {

	public static void main(String[] args){
		
		ServerSocket socket;
		try {
			socket = new ServerSocket(8600);
		Thread t = new Thread(new Client(socket));
		t.start();
		System.out.println("Le serveur est prêt !");
		
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}