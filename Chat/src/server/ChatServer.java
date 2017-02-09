package server;

import java.io.*;
import java.net.*;

/**
 * 
 * @author Milada
 *
 */
public class ChatServer {
	
	//mesta za klijente	
	static ServerNit klijenti[]= new ServerNit[10];
	static boolean pokreniUDP = false;
	static boolean pokreniTCP = true;

	
	
	
	public static void main(String[] args) {		
		
		//broj porta koji cemo zauzati
		int port=2222;
		
		//preko argumenata komandne linije moze se uneti alternativni broj porta
		if(args.length>0){
			port=Integer.parseInt(args[0]);
		}
		
		//deklaracija soketa klijenta koji ce doci na server
		Socket klijentSoket = null;
		try {
			//zauzimamo port
			ServerSocket serverSoket = new ServerSocket(port);
			while(true){
				//cekamo klijenta
				klijentSoket = serverSoket.accept();
				for (int i = 0; i <= 9; i++) {
					//na slobodnom mestu pravimo nit koja ce raditi sa klijentom i
					//pokrecemo je
					if (klijenti[i]==null){
						klijenti[i]=new ServerNit(klijentSoket, klijenti);
						klijenti[i].start();
						break;
					}
					
				}
			}
			
		} catch (IOException e) {
			System.out.println(e);
		}
		
	}
	
	

}
