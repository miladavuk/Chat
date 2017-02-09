package klijent;

import java.io.*;
import java.net.*;


//klasa ChatKlijent implementira Runnable kako bi mogla da se pokrene kao nit
/**
 * 
 * @author Milada
 *
 */
public class ChatKlijent implements Runnable {
	
	//deklaracija promenljivih
	static Socket soketZaKomunikaciju = null;
	//ulaz-izlaz za soket
	static PrintStream izlazniTokKaServeru = null;
	static BufferedReader ulazniTokOdServera = null;
	//ulaz sa konzole
	static BufferedReader ulazKonzola=null;
	//da li je korisnik izasao iz chat sobe
	static boolean kraj = false;

	public static void main(String[] args) {
		try {
			//broj porta chat servera
			int port = 2222;
			
			//preko argumenata komandne linije moze se uneti alternativni broj porta
			if(args.length>0){
				port=Integer.parseInt(args[0]);
			}
			//povezujemo se na host
			soketZaKomunikaciju = new Socket("localhost", port);
			
			//inicijalizacija ulaza sa konzole
			ulazKonzola = new BufferedReader(new InputStreamReader(System.in));
			
			//inicijalizacija ulazno-izlaznih tokova
			izlazniTokKaServeru = new PrintStream(soketZaKomunikaciju.getOutputStream());
			ulazniTokOdServera = new BufferedReader
					(new InputStreamReader(soketZaKomunikaciju.getInputStream()));
			
			//pravi se nit koja ce da cita poruke
			new Thread(new ChatKlijent()).start();
			
			//dokle god nije kraj pise serveru ono sto ucitava sa konzole, liniju po liniju
			while(!kraj){
				izlazniTokKaServeru.println(ulazKonzola.readLine());
			}
			///zatvaramo soket
			soketZaKomunikaciju.close();
			
			
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host ");
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}

	}

	@Override
	public void run() {
		String linijaOdServera;
		try {
			//ucitavamo liniju od servera
			while((linijaOdServera=ulazniTokOdServera.readLine()) != null){
				System.out.println(linijaOdServera);
				if(linijaOdServera.indexOf("*** Dovidjenja") == 0){
					kraj = true;
					return;
				}
				//ako server hoce da posalje listu online korisnika, iniciramo UDP protokol
				if(linijaOdServera.indexOf("*** ONLINE") == 0){
					DatagramSocket datagramSoket = new DatagramSocket();
					InetAddress IPAdresa = InetAddress.getByName("localhost");
					byte[] odgovorOdServera = new byte[1024];
					byte[] podaciZaServer = new byte[1024];
					String recenica = "";
					podaciZaServer = recenica.getBytes();
					DatagramPacket paketZaServer = new DatagramPacket(podaciZaServer, podaciZaServer.length,IPAdresa,2222);
					datagramSoket.send(paketZaServer);
					DatagramPacket paketOdServera = new DatagramPacket(odgovorOdServera, odgovorOdServera.length);
					datagramSoket.receive(paketOdServera);
					String recenicaOdServera = new String(paketOdServera.getData(),0, paketOdServera.getLength());
					System.out.println(recenicaOdServera);
					datagramSoket.close();

					
				}
			}
		} catch (IOException e) {
			System.err.println("IOException: " + e);
		}
		
	}

}
