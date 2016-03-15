package main.java.car.tp2;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

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
	protected BufferedInputStream filebuffer;
	
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
		String check = this.read(this.sckt);
		if(check.startsWith("200"))
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
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response download(@PathParam ("file") String file) throws IOException
	{
		if(!this.sckt.isConnected())
			return Response.status(403).build(); //this.header()+"<p>Vous devez vous connecter � un serveur FTP pour continuer</p>";
		this.scktTransfert = this.pasv();
		this.write("RETR "+file+"\n", this.sckt);
		String code = this.read(this.sckt);
		System.out.println("code dl = "+code);
		if(code.startsWith("550"))
		{
			return Response.status(550).build();//this.header()+"<p>Erreur 550</p>";
			
		}
		else if(code.startsWith("125"))
		{
			System.out.println("xsvdvsqvfq");
			String code2 = this.read(this.sckt) ;//je crois que ça bloque ici.
//			System.out.println(code2);
			if(code2.startsWith("226")){
				return this.readFile(this.scktTransfert, file);
//				System.out.println(this.read(this.scktTransfert));
//				return "sauce" ;
			}
			return Response.status(500).build();
		
		}
			
		else
			 return Response.status(500).build();
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
			String s = "" ;
			s = buff.readLine();
			System.out.println("bibo");
			System.out.println(s);
			
			return s;

		} catch (IOException e) {
			return "iofail" ;
		}
	}
	
	public Response readFile(Socket socket, String filename){
		String[] infoFile = filename.split(Pattern.quote("."));
		System.out.println(infoFile[1]);
		System.out.println(filename);
		int cursor =0;
		
		try {
			int b;
			this.is = socket.getInputStream();
			this.isr = new InputStreamReader(is);
			this.filebuffer = new BufferedInputStream(is);
			System.out.println(this.filebuffer.available());
			String s = "" ;
			String tmp ;
			while((b=this.filebuffer.read())!=-1 ) {
				//data[cursor]=(byte)b;
				//System.out.println(cursor);
				cursor++;
			}
			this.filebuffer.close();
			this.filebuffer = new BufferedInputStream(is);
			byte[] data = new byte[cursor];
			cursor = 0;
			while((b=this.filebuffer.read())!=-1 ) {
				data[cursor]=(byte)b;
				System.out.println("dfsfsq"+cursor);
				cursor++;
			}
			buff.close();
			isr.close();
			is.close();
			this.filebuffer.close();
			System.out.println(s);
			
			//On crée un fichier temporaire
			File tempFile = new File(filename);
			FileOutputStream fos = new FileOutputStream(tempFile);
			fos.write(data);
			fos.close();
			ResponseBuilder rb = Response.ok(tempFile);
			rb.header("content-disposition", "attachement; filename="+tempFile.getName());
			Response response = rb.build();
			System.out.println("test");
			return response;
			
			//return s ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return Response.status(500).build();
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
