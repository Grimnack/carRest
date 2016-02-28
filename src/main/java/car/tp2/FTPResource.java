package main.java.car.tp2;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
	protected InputStream is;
	protected InputStreamReader isr;
	protected OutputStream os;
	protected DataOutputStream dos;
	protected BufferedReader buff;
	
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
			return this.sayHello()+"<br/><p>La connection au serveur est Ã©tablie";
		else
			return this.sayHello()+"<br/><p>La connection au serveur n'est pas Ã©tablie";
	}
	
	@GET
	@Path("/login/{user}/{pass}")
	public String login (@PathParam("user") String user, @PathParam("pass")String pass) throws IOException
	{
		
		if(!this.sckt.isConnected() || this.sckt.isClosed())
			return this.sayHello()+"<p>Vous n'êtes pas connecté au serveur.</p>";
		else
		{
			
			this.write("USER "+user+"\n",this.sckt);
			System.out.println(this.read());
			if(this.read().startsWith("331"))
			{
				this.write("PASS "+pass+"\n", this.sckt);
				System.out.println(this.read());
				if(this.read().startsWith("230"))
				{
					return this.sayHello()+"<p>Connexion réussie.<br/>Bienvenue"+user+"</p>";
				}
				else
					return this.sayHello()+"<p>1Erreur d'authentification";
			}
			else
				return this.sayHello()+"<p>2Erreur d'authentification";
			
			
		}
	}
	
	public String read(){
		try {
			this.is = this.sckt.getInputStream();
			this.isr = new InputStreamReader(is);
			this.buff = new BufferedReader(isr);

			String s = buff.readLine();
			//System.out.println(s);
			return s;

		} catch (IOException e) {
			return "iofail" ;
		}
	}
	
	public void write(String msg, Socket socket) throws IOException{
		
		OutputStream os = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		dos.write(msg.getBytes());
		dos.flush();
	}

}
