package edu.nmsu.cs.webserver;



/**
 * Web worker: an object of this class executes in its own new thread to receive and respond to a
 * single HTTP request. After the constructor the object executes on its "run" method, and leaves
 * when it is done.
 *
 * One WebWorker object is only responsible for one client connection. This code uses Java threads
 * to parallelize the handling of clients: each WebWorker runs in its own thread. This means that
 * you can essentially just think about what is happening on one client at a time, ignoring the fact
 * that the entirety of the webserver execution might be handling other clients, too.
 *
 * This WebWorker class (i.e., an object of this class) is where all the client interaction is done.
 * The "run()" method is the beginning -- think of it as the "main()" for a client interaction. It
 * does three things in a row, invoking three methods in this class: it reads the incoming HTTP
 * request; it writes out an HTTP header to begin its response, and then it writes out some HTML
 * content for the response content. HTTP requests and responses are just lines of text (in a very
 * particular format).
 * 
 * @author Jon Cook, Ph.D.
 *
 **/

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
//import java.util.StringTokenizer;
import java.util.TimeZone;
import java.io.*;

public class WebWorker implements Runnable
{

	private Socket socket;
	private File webFile;
    private String path;
    private String fileType;
    //private String type;
    //private String fileName;
    //private FileInputStream input;
    //private boolean fileExists = false;
	/**
	 * Constructor: must have a valid open socket
	 **/
	public WebWorker(Socket s)
	{
		socket = s;
		//webFile = new File("");
	}

	/**
	 * Worker thread starting point. Each worker handles just one HTTP request and then returns, which
	 * destroys the thread. This method assumes that whoever created the worker created it with a
	 * valid open socket object.
	 **/
	public void run()
	{
		System.err.println("Handling connection...");		
		try
		{
			InputStream is = socket.getInputStream();
		    OutputStream os = socket.getOutputStream();
			readHTTPRequest(is);	
			
			webFile = new File(path);
			fileType = path.substring(path.lastIndexOf(".")+ 1);
			System.out.println(path);
			
			if(webFile.exists() && webFile.isFile()){
				
				if (fileType.equals("png")) {
					writeHTTPHeader(os, "image/png");	                
	            } 
				
				else if (fileType.equals("jpg")) {
					writeHTTPHeader(os, "image/jpeg");
				}
				
				else if (fileType.equals("gif")) {
					writeHTTPHeader(os, "image/gif");
				}
				else if (fileType.equals("ico")) {
					writeHTTPHeader(os, "image/x-con");
				}
				else {
					writeHTTPHeader(os, "text/html");
				}
				writeContent(os);
			}
			else {
				writeHTTPHeader(os, "text/html");
				os.write("<html><head></head><body>\n".getBytes());
				os.write("<h1>Error 404</h1></div>\n".getBytes());
			    os.write("<h3>The page you are trying to access does not exist.</h3></div>\n</body>\n</html>".getBytes());
			}
			os.flush();													
			socket.close();		    
		}
        catch (Exception e) {        	
    	    System.err.println("Output error: " + e); 		
	    }
		
	    System.err.println("Done handling connection.");
	    return;
	}

	
	/**
	 * Read the HTTP request header.
	 **/
	private void readHTTPRequest(InputStream is)
	{
		String line;
		BufferedReader r = new BufferedReader(new InputStreamReader(is));
			
		while (true)
		{
			try
			{
				while (!r.ready())
				    Thread.sleep(1);
			    line = r.readLine();	
			
			    System.err.println("Request line: (" + line + ")");		
			    if (line.length() == 0)
				    break;
			
			    // if searches for Request line: (GET /favicon.ico HTTP/1.1) line in order to 
			    // Determine the HTML file being served exists, this will lead to 404 no found
			    if( line.substring(0,3).equals("GET") ) 
			    {	
				    String[] section =  line.split(" ");
				    path =  "www" + section[1];				
				    System.out.println(path);
				
			    	if(path.equals("./")){
				        System.out.println("Connection Successful");				       
				    }
				
				    webFile = new File(new File("."), path);
           
				    if( webFile.isFile() && webFile.exists()) {
                	    System.out.println("SUCCESS");
                    }
				
				System.out.println(webFile.getAbsolutePath());
				System.out.println(webFile.exists());
				
			}//end outer if		
		
		}
		catch (Exception e)
		{
			System.err.println("Request error: " + e);
			break;
		}
	}
	return;
	}//end readHTTPRequest

	/**
	 * Write the HTTP header lines to the client network connection.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 * @param contentType
	 *          is the string MIME content type (e.g. "text/html")
	 **/
	private void writeHTTPHeader(OutputStream os, String contentType) throws Exception
	{
		Date d = new Date();
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		// if conditional to check if the file exists or not
		// Will print out the correct HTTP header response depending on scenario
		if (webFile.exists() && webFile.isFile()) 
		{
			os.write("HTTP/1.1 200 OK\n".getBytes());
		}
		else
		{
			os.write("HTTP/1.1 404 Not Found\n".getBytes());
		}
		
		os.write("Date: ".getBytes());
		os.write((df.format(d)).getBytes());
		os.write("\n".getBytes());
		os.write("Server:Isaias' Broken Web Server\n".getBytes());
		// os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
		// os.write("Content-Length: 438\n".getBytes());
		os.write("Connection: close\n".getBytes());
		os.write("Content-Type: ".getBytes());
		os.write(contentType.getBytes());
		os.write("\n\n".getBytes()); // HTTP header ends with 2 newlines
		return;
	}

	/**
	 * Write the data content to the client network connection. This MUST be done after the HTTP
	 * header has been written out.
	 * 
	 * @param os
	 *          is the OutputStream object to write to
	 **/
	private void writeContent(OutputStream os) throws Exception
	{
		
		if(fileType.equals("html")) {
			
			BufferedReader r = new BufferedReader(new FileReader(webFile));
			Date d = new Date();
			DateFormat df = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
			df.setTimeZone(TimeZone.getTimeZone("GMT"));							
			String line;
			
			 while ((line = r.readLine()) != null)
			  {							  
				    if(line.contains("<cs371date>"))			
						line = line.replaceAll("<cs371date>", df.format(d));
									
					if(line.contains("<cs371server>"))					
						line = line.replaceAll("<cs371server>", "Isaias' WebServer");
				
					os.write("<html><head><title>Isaias' Broken WebServer!!!</title>".getBytes());
					os.write("</head>".getBytes());
					os.write(line.getBytes());
					
			 }//end while			
		     r.close();
		}
		else {
			
			FileInputStream pic = new FileInputStream(path);
			int n = pic.available();
			byte[] B = new byte[n];
			pic.read(B);
			pic.close();
			os.write(B);
	   }
				 			      	
	 }//end writeContent			
     
   }//end class
		
	
	
	

