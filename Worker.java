import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.lang.*;
import java.net.URL;
import java.net.MalformedURLException;

public class Worker implements Runnable{
	URL url;
	String urlStr;
	Integer stopRedirection;
	Integer timer;
	StringIntegerHash wordFrequency;

	public Worker(String urlStr, StringIntegerHash wordFrequency) throws MalformedURLException{
		this.url = new URL(urlStr);
		this.stopRedirection = 20;
		this.timer = 1000;
		this.wordFrequency = wordFrequency;
		this.urlStr = urlStr;
	}


	// Task: try to get the webpage and add te words in its content in the wordFrequency object
	// Return: void but eventually modify the wordFrequency object
	public void run(){
		try{
			System.out.println("Worker for the url: " + this.urlStr + " launched");

			// Setup of the socket
			Socket s = new Socket(url.getHost() ,((url.getPort() < 0) ? 80 : url.getPort()));
			OutputStream out = s.getOutputStream(); InputStream in = s.getInputStream();
			s.setSoTimeout(20000);
			s.setTcpNoDelay(true);

			// Construction of the request
			String ToSend = "GET " + url.getPath();
			if(url.getQuery() != null){ToSend += "?" + url.getQuery();}
			ToSend += " HTTP/1.1\r\nHost: " + url.getHost() + "\r\nPort: "
			+ Integer.toString(((url.getPort() < 0) ? 80 : url.getPort())) + "\r\n\r\n";

			// Request sending
			out.write(ToSend.getBytes());
			out.flush();

			// Get the content of the webpage (if there is no error) as a list of string
			List<String> WordInThePageList = new ArrayList<>(Arrays.asList(GetWebPageAsStringList(in)));
			// Add the list of words in the wordFrequency object
			this.wordFrequency.AddToHash(WordInThePageList);

			System.out.println("worker for: " + this.urlStr + " done");
			s.close();
			return;

		// Catch the exception if there is one send to the server terminal the name of the worker and the exception
		}catch(Exception any){
			System.out.println("worker for: " + this.urlStr + " had an exception see next line.");
			System.out.println(any);
			return;
			}
	}

	// Argument: A string of a webpage
	// Task: remove all special characters, HTML tags,
	//       everything before the body of the page and
	//       the scripts and then cut the page into its words
	// Return: The list of words in the webpage
	public static String[] HTMLStringCutter(String string){
		//Remove everything before the body of the page
		while(true) {
			if (string.indexOf("</head>") != -1){
				string = string.substring(string.indexOf("</head>")+7,string.length()).trim();
				continue;
			}
			break;
		}

		//Filter the HTML tags
		//Remove the special characters like "&****;"
		while(true) {
			if ((string.indexOf('&') != -1) & (string.indexOf(';') != -1)){
				string = string.substring(0, string.indexOf('&')).trim() + " " + string.substring(string.indexOf(';')+1,string.length()).trim();
				continue;
			}
			break;
		}

		//Remove the scripts
		while(true) {
			if ((string.indexOf("<script") != -1) & (string.indexOf("</script>") != -1)){
				string = string.substring(0, string.indexOf("<script")).trim() + " " + string.substring(string.indexOf("</script>")+9,string.length()).trim();
				continue;
			}
			break;
		}

		//Remove the general HTML tags
		while(true) {
			if ((string.indexOf('<') != -1) & (string.indexOf('>') != -1)){
				string = string.substring(0, string.indexOf('<')).trim() + " " + string.substring(string.indexOf('>')+1,string.length()).trim();
				continue;
			}
			break;
		}

		// Split the webpage content into its words
		return string.replace("\n"," ").replace("\t"," ").split(" ");
	}




	// Argument: The input stream
	// Task: Try to get the webpage and divide its content into its words
	// Return: The list of words in the webpage
	public String[] GetWebPageAsStringList (InputStream in) throws IOException{

		// Get the HTML code
		String codemessage = "";
		while(true){
			try{codemessage += (char) in.read();
			if(codemessage.charAt(codemessage.length()-1) == '\r'){break;}
		}catch(IOException e){throw new IOException(e);}
		}
		codemessage = codemessage.replace("\r","").replace("HTTP/1.1 ","");
		Integer code = Integer.parseInt(codemessage.substring(0,3));




		// 400 and more: error
		// throw an error
		if(code > 399){
			codemessage += " is considered as an error";
			throw new IOException(codemessage);
		}

		//redirection
		if(code == 303){

			//Protection against a lot of redirection
			this.stopRedirection--;
			if(this.stopRedirection < 0){
				throw new IOException("Too much redirection: request denied");
			}
			try{wait(this.timer);}catch(InterruptedException e){System.out.println("Waiting for redirection failed");}

			// Get the new url for the redirection
			while(true){
				try{codemessage += (char) in.read();}catch(IOException e){throw new IOException(e);}
				if(codemessage.indexOf("Location: ") != -1){break;}
			}
			codemessage = "";
			while(true){
				try{codemessage += (char) in.read();}catch(IOException e){throw new IOException(e);}
				if(codemessage.charAt(codemessage.length()-1) == '\r'){break;}
			}
			URL url = new URL(codemessage.substring(0,codemessage.length()-1));
			System.out.println(codemessage.substring(0,codemessage.length()-1));

			// Setup a new socket and do a recursion
			// Setup of the socket
			Socket s = new Socket(url.getHost() ,((url.getPort() < 0) ? 80 : url.getPort()));
			OutputStream out = s.getOutputStream(); InputStream inp = s.getInputStream();
			s.setSoTimeout(60000);

      // Construction of the request
			String ToSend = "GET " + url.getPath();
			if(url.getQuery() != null){ToSend += "?" + url.getQuery();}
			ToSend += " HTTP/1.1\r\nHost: " + url.getHost() + "\r\nPort: "
			+ Integer.toString(((url.getPort() < 0) ? 80 : url.getPort())) + "\r\n\r\n";

      // Request sending
			out.write(ToSend.getBytes());
			out.flush();

			// recursion
			return GetWebPageAsStringList(inp);
		}

		//ok
		if(code == 200){

			// get the webpage content
			while(true){
				try{codemessage += (char) in.read();}catch(IOException e){throw new IOException(e);}
				if(codemessage.indexOf("</body>") != -1){break;}
			}

			// return the list of the words in the webpage
			return HTMLStringCutter(codemessage);
		}

		// code not handled so return nothing
		String[] nothing = {};
		return nothing;
	}
}
