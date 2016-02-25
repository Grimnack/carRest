package main.java.car.tp2;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * Exemple de ressource REST accessible a l'adresse :
 * 
 * 		http://localhost:8080/rest/tp2/ftp
 * 
 * @author Lionel Seinturier <Lionel.Seinturier@univ-lille1.fr>
 */

@Path("/ftp")
public class FTPResource {
	protected Socket sckt;
	
	@GET
	@Produces("text/html")
	public String sayHello() {
		return "<h1>FTP Matthieu Quentin</h1>";
	}
	
	@GET
	@Path("/connect/{ip}/{port}")
	public String connect(@PathParam("ip") String ip,@PathParam("port") int port) throws UnknownHostException, IOException
	{
		this.sckt = new Socket(InetAddress.getByName(ip), port);
		if(this.sckt.isConnected())
			return this.sayHello()+"<br/><p>La connection au serveur est établie";
		else
			return this.sayHello()+"<br/><p>La connection au serveur n'est pas établie";
	}

}
