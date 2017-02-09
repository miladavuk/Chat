package server;


//klasa koja olaksava baratanje korisnicima
/**
 * 
 * @author Milada
 *
 */
public class Korisnik {
	
	private String ime;
	private String pol;
	private String status;
	
	
	public Korisnik(String ime, String pol, String status) {		
		this.ime = ime;
		this.pol = pol;
		this.status = status;
	}
	public String getIme() {
		return ime;
	}
	public void setIme(String ime) {
		this.ime = ime;
	}
	public String getPol() {
		return pol;
	}
	public void setPol(String pol) {
		this.pol = pol;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return ime.toUpperCase() + "\n";
	}
	
	
	

}
