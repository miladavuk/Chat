package server;

import java.io.*;
import java.net.*;
import java.util.GregorianCalendar;
import java.util.LinkedList;

//ova nit opsluzuje klijenta, komunikacija sa svakim klijentom se odvija
//u okviru posebne niti
/**
 * 
 * @author Milada
 *
 */
public class ServerNit extends Thread{
	
	//ulazni i izlazni tok
	BufferedReader ulazniTokOdKlijenta = null;
	PrintStream izlazniTokKaKlijentu = null;
	
	Socket soketZaKom = null;
	//atributi korisnika
	String ime = null;
	String pol = null;
	String status = null;

	
	
	
	//niz svih klijenata na serveru
	ServerNit[] klijenti;
	
	public ServerNit(Socket soket, ServerNit[] klijent){
		this.soketZaKom = soket;
		this.klijenti = klijent;
		
	}
	
	public void run() {
		
		String linija;
		
		try {
			//inicijalizacija ulazno-izlaznih tokova
			ulazniTokOdKlijenta = new BufferedReader
					(new InputStreamReader(soketZaKom.getInputStream()));
			izlazniTokKaKlijentu = new PrintStream(soketZaKom.getOutputStream());
			
			//ucitava se ime klijenta
			izlazniTokKaKlijentu.println("Unesite ime");
			ime=ulazniTokOdKlijenta.readLine();	
			
			//ucitava se pol klijenta sa proverom unosa
			do{
			izlazniTokKaKlijentu.println("Unesite pol (M ili Z)");
			pol=ulazniTokOdKlijenta.readLine().toUpperCase();
			}while(!pol.equals("M") && !pol.equals("Z"));
			
			//ucitava se status klijenta sa proverom unosa
			do{
			izlazniTokKaKlijentu.println("Unesite status (/dostupan ili /zauzet)."
					+ "\n Status mozete promeniti u svakom trenutku");
			status=ulazniTokOdKlijenta.readLine();
			}while(!status.equals("/dostupan") && !status.equals("/zauzet"));
			
			
			
			izlazniTokKaKlijentu.println("Dobrodosao/la "+ime+".\nZa izlaz unesite /quit");
			
			
			
			//obavestavaju se  korisnici suprotnog pola o dolasku novog korisnika
			for (int i = 0; i <= 9; i++) {
				if(klijenti[i]!=null && klijenti[i]!=this && !klijenti[i].pol.equals(this.pol) ){
					klijenti[i].izlazniTokKaKlijentu.println
					("*** Novi korisnik: "+ime+ " je usao u chat sobu!!! ***");
				}
				
			}
			while(true){
				//ucitava se linija teksta od korisnika
				linija=ulazniTokOdKlijenta.readLine();
				
				//pravi se lista korisnika za slanje
				LinkedList<Korisnik> korisniciZaSlanje = new LinkedList<Korisnik>();
				
				//ako pocinje sa /quit izlazi se iz petlje
				if(linija.startsWith("/quit")){
					break;
				}
				
				//proverava se da li klijent menja status
				if(linija.equals("/dostupan") || linija.equals("/zauzet") ){
					this.status=linija;
					izlazniTokKaKlijentu.println("Promenili ste status na "+status);
					
				}else{
				
				//pronalazi se spisak korisnika suprotnog pola
					
				
				LinkedList<Korisnik> pronadjeniKorisnici = new LinkedList<>();
				for (int i = 0; i <= 9; i++) {
					if(klijenti[i]!=null && klijenti[i]!=this && 
							!klijenti[i].pol.equals(this.pol)&& klijenti[i].status.equals("/dostupan") ){
						pronadjeniKorisnici.add(new Korisnik
								(klijenti[i].ime, klijenti[i].pol, klijenti[i].status));						
					}
					
				}
				if(pronadjeniKorisnici.isEmpty()){
					izlazniTokKaKlijentu.println("Nema online korisnika suprotnog pola");					
				}else{
					//
					
					//salje se spisak preko UDP
					
				
					izlazniTokKaKlijentu.println("*** ONLINE KORISNICI ***");
					byte[] podaciOdKlijenta = new byte[1024];
					byte[] odgovorZaKlijenta =  new byte[1024];
					
					//pravimo String za slanje
					
					String onlineKorisnici="";
					for (Korisnik onlineKorisnik : pronadjeniKorisnici) {
						if(onlineKorisnik!=null)
						onlineKorisnici= onlineKorisnici + onlineKorisnik.toString();
					}
					

					try {
						DatagramSocket datagramSoket = new DatagramSocket(2222);
						while(true){

							DatagramPacket paketOdKlijenta = 
									new DatagramPacket(podaciOdKlijenta, podaciOdKlijenta.length);
							//primamo nebitan String od klijenta koji samo zapocinje komunikaciju
							//ne radimo nista sa njim
							datagramSoket.receive(paketOdKlijenta);
							InetAddress IPAdresa= paketOdKlijenta.getAddress();
							int UDPPortKlijenta = paketOdKlijenta.getPort();
							
							odgovorZaKlijenta = onlineKorisnici.getBytes();
							DatagramPacket paketZaKlijenta = 
									new DatagramPacket(odgovorZaKlijenta, odgovorZaKlijenta.length,
											IPAdresa, UDPPortKlijenta);
							datagramSoket.send(paketZaKlijenta);
							datagramSoket.close();
						
							
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				//klijent bira kome ce da salje
				izlazniTokKaKlijentu.println
				("Izaberite dostupne osobe za chat unosom njihovog imena."
						+ " Za kraj izbora unesite /done");
				if(this.status.equals("/zauzet"))
					izlazniTokKaKlijentu.println("NAPOMENA: status Vam je /zauzet pa mozete slati poruke, ali ih ne mozete primati");
				boolean krajIzbora = false;
				String odabranoIme = null;
				String izbor = null;
				while(!krajIzbora){
					
					izbor=ulazniTokOdKlijenta.readLine();
					if(izbor.equals("/done")){
						krajIzbora = true;
						break;												
					}
					else{
						
						odabranoIme=izbor.toUpperCase();
						for (Korisnik onKorisnik : pronadjeniKorisnici) {
							if(onKorisnik!=null && !onKorisnik.getPol().equals(this.pol)
									&&onKorisnik.getIme().equalsIgnoreCase(odabranoIme)){
								Korisnik korisnikZaSlanje = 
										new Korisnik(onKorisnik.getIme(), onKorisnik.getPol(), onKorisnik.getStatus());
								korisniciZaSlanje.add(korisnikZaSlanje);
							}				
						}
						if(korisniciZaSlanje.isEmpty()){
							izlazniTokKaKlijentu.println
							("Odabrani korisnici nisu dostupni ili niste pravilno uneli");
						}
						
					}					
					
				}
			//
				}
				if(!korisniciZaSlanje.isEmpty()){
				//salje se poruka odabranim korisnicima i samom posiljaocu,
				//posto posiljalac vidi svoju poruku u konzoli, zna da je poslata	
				//takodje, zapisuju se sve poruke u tekst fajl	
				for (int i = 0; i <= 9; i++) {
					for(int j=0; j< korisniciZaSlanje.size();j++){
					
					GregorianCalendar vremePrijama;
					String vreme;
					String primalac = null;
					if((klijenti[i]!=null && 
							klijenti[i].ime.equalsIgnoreCase(korisniciZaSlanje.get(j).getIme()))
							|| (klijenti[i]!=null && klijenti[i]==this)){
						klijenti[i].izlazniTokKaKlijentu.println("<" + ime + ">" + linija);
						vremePrijama = new GregorianCalendar();
						vreme = vremePrijama.getTime().toString();
						
						
						if(klijenti[i]!=this)
						   primalac = klijenti[i].ime;
						else
							primalac = "sam-sebi";
						
							try {
							    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("logPoruka.txt", true)));
							    out.println("Posiljalac: "+"<" + ime + "> " + linija+" , Primalac: "+"<"+primalac+ "> "+
							    		" , Vreme: "+ vreme);
							    out.close();
							} catch (IOException e) {
							    e.printStackTrace();
							}
						
					  }
				   }
			   }
			}
		}
	}
			//obavestavaju se svi korisnici suprotnog pola da korisnik napusta chat
			for (int i = 0; i <= 9; i++) {
				if(klijenti[i]!=null && klijenti[i]!=this 
						&& !klijenti[i].pol.equals(this.pol)){
					klijenti[i].izlazniTokKaKlijentu.println
					("*** Korisnik " + ime + " izlazi iz chat sobe!!! ***");
				}
				
			}
			
			izlazniTokKaKlijentu.println("*** Dovidjenja " + ime + "***");
			
			//zatvaramo soket
			soketZaKom.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//nit trazi referencu na sebe medju klijentima
		//i podesava je na null kako bi oslobodila mesto za novog klijenta
		for (int i = 0; i <= 9; i++) {
			if(klijenti[i]==this){
				klijenti[i]=null;
			}
			
		}
		
	}	
	

}
