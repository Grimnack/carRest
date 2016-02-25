package main.java.car.tp2;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

/**
 * Exemple de ressource REST accessible a l'adresse :
 * 
 * 		http://localhost:8080/rest/tp2/ftp
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@univ-lille1.fr>
 */

@Path("/ftp")
public class FTPResource {
	
	@GET
	@Produces("text/html")
	public String sayHello() {
		return "<h1>FTP Matthieu Quentin</h1>";
	}

}
