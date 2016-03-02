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

import javax.ws.rs.FormParam;
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
	protected Socket sckt, scktTransfert;
	protected InputStream is;
	protected InputStreamReader isr;
	protected OutputStream os;
	protected DataOutputStream dos;
	protected BufferedReader buff;
	
	public String header()
	{
		return "<h1>FTP Matthieu Quentin</h1>";
	}
	
	@GET
	@Produces("text/html")
	public String sayHello() {
		return this.header()+"<p>Indiquez les coordonnées du serveur pour vous connecter.</p>" +
				"<form name=\"fconnect\" method=\"post\" action=\"connect\">"+
				"<input type=text name=ip /><input type=text name=port />"+
				"<button type=submit>Se connecter</button>";
	}
	
	@POST
	@Path("/connect")
	public String connect(@FormParam("ip") String ip,@FormParam("port") int port) throws UnknownHostException, IOException
	{
		this.sckt = new Socket(InetAddress.getByName(ip), port);
		this.read(this.sckt);
		if(this.sckt.isConnected())
			return this.header()+"<br/><p>La connection au serveur est établie"+
			"<p>Indiquez vos identifiants du serveur pour vous loguer.</p>" +
			"<form name=\"fconnect\" method=\"post\" action=\"login\">"+
			"<input type=text name=user /><input type=text name=pass />"+
			"<button type=submit>Se connecter</button>";
		else
			return this.header()+"<br/><p>La connection au serveur n'est pas établie";
	}
	
	@POST
	@Path("/login")
	public String login (@FormParam("user") String user, @FormParam("pass")String pass) throws IOException, InterruptedException
	{
		String userRes,passRes;
		
		if(!this.sckt.isConnected() || this.sckt.isClosed())
			return this.header()+"<p>Vous n'étes pas connecté au serveur.</p>";
		else
		{
			
			this.write("USER "+user+"\n",this.sckt);
			Thread.sleep(1000);
			userRes=this.read(this.sckt);
			System.out.println("user res = "+userRes);
			if(userRes.startsWith("331"))
			{
				this.write("PASS "+pass+"\n", this.sckt);
				Thread.sleep(1);
				passRes= this.read(this.sckt);
				System.out.println("pass res = "+passRes);
				if(passRes.startsWith("230"))
				{
					System.out.println("Ore da yo");
					return this.header()+"<p>Connexion réussie.<br/>Bienvenue"+user+"</p>";
				}
				else
					return this.header()+"<p>1Erreur d'authentification</p>";
			}
			else
				return this.header()+"<p>2Erreur d'authentification</p>";
			
			
		}
	}
	
	@GET
	@Path("/list")
	public String list() throws IOException
	{
		this.scktTransfert=this.pasv();
		if(this.scktTransfert != null && this.scktTransfert.isConnected())
		{
			String res="";
			if(!this.sckt.isConnected() || this.sckt.isClosed())
				return this.header()+"<p>Vous n'étes pas connecté au serveur.</p>";
			else
			{
				this.write("LIST\n", this.sckt);
				res = this.read(this.sckt);
				if(res.startsWith("125"))
				{
					res=this.read(this.scktTransfert);
					System.out.println(res);
				}
				return res;
			}
		}
		else
		{
			return this.header()+"<p>Erreur lors du passage en mode passif.<br/>Désolé</p>";
		}
		
		
		
	}
	
	public Socket pasv() throws IOException
	{
		String response ="";
		this.write("PASV\n",this.sckt);
		response =this.read(this.sckt);
		
		System.out.println("res = "+response);
		String ip = response.substring("227 Entering passive mode (".length() , "227 Entering passive mode (127,0,0,1,170,126)".length()-1);
		System.out.println(ip);
		String[] liste = ip.split(",");
		String addr = liste[0]+'.'+liste[1]+'.'+liste[2]+'.'+liste[3] ;
		int partie1 = Integer.parseInt(liste[4]);
		int partie2 = Integer.parseInt(liste[5]);
		int port = partie1*256 + partie2 ;
		
		System.out.println(addr);
		System.out.println(port);
		
		Socket socket = new Socket(InetAddress.getByName(addr), port);
		return socket ;
	}
	
	public String read(Socket socket){
		try {
			this.is = socket.getInputStream();
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
		
		this.os = socket.getOutputStream();
		this.dos = new DataOutputStream(this.os);
		this.dos.write(msg.getBytes());
		this.dos.flush();
	}

}
