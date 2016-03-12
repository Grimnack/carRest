package main.java.car.tp2;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
		System.out.println(this.scktTransfert);
		if (this.scktTransfert==null||this.scktTransfert.isClosed()||(!this.scktTransfert.isConnected()))
		{
			System.out.println("on demande un pasv");
			this.scktTransfert=this.pasv();
		}
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
					String rest=this.read(this.scktTransfert);
					System.out.println(res);
					res = this.read(this.sckt);
					if (res.startsWith("226"))
						return this.header()+this.generateList(rest);
					else 
						return this.header()+"<p> erreur transfert </p>" ;
				}else {
					return this.header()+"<p> erreur debut transfert </p>" ;
				}
			}
		}
		else
		{
			return this.header()+"<p>Erreur lors du passage en mode passif.<br/>Désolé</p>";
		}
		
		
		
	}
	
	@GET
	@Path("/download/{file}")
	public String download(@PathParam ("file") String file) throws IOException
	{
		if(!this.sckt.isConnected())
			return this.header()+"<p>Vous devez vous connecter � un serveur FTP pour continuer</p>";
		if (this.scktTransfert==null||this.scktTransfert.isClosed()||(!this.scktTransfert.isConnected()))
		{
			System.out.println("on demande un pasv pou dl");
			this.scktTransfert=this.pasv();
		}
		this.write("RETR "+file+"\n", this.sckt);
		String code = this.read(this.sckt);
		System.out.println("code dl = "+code);
		if(code.startsWith("550"))
		{
			return this.header()+"<p>Erreur 550</p>";
			
		}
		else if(code.startsWith("125"))
		{
			System.out.println("xsvdvsqvfq");
			String code2 = this.read(this.sckt) ;
			System.out.println(code2);
			if(code2.startsWith("226")){
				//return this.read(this.scktTransfert);
				//System.out.println(this.read(this.scktTransfert));
				return "sauce" ;
			}
			return "pas sauce" ;
		
		}
			
		else
			return "error "+code;
	}
	
	public Socket pasv() throws IOException
	{
		String response ="";
		this.write("PASV\n",this.sckt);
		response =this.read(this.sckt);
		
		System.out.println("res = "+response);
		String[] ip = response.split("[^0-9]+") ;
		String addr = ip[1]+'.'+ip[2]+'.'+ip[3]+'.'+ip[4] ;
		int partie1 = Integer.parseInt(ip[5]);
		int partie2 = Integer.parseInt(ip[6]);
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
			//System.out.println("buff="+s);
			
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
	
	private String generateList(String text)
	{
		String listhtml = "<ul>";
		String[] list = text.split("\t");
		for(String element : list)
		{
			listhtml+="<li><a href=\"download/"+element+"\">"+element+"</a>";
			listhtml+="</li>";
			
		}
		listhtml+="</ul>";
		return listhtml;
	}

}
