import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Arrays;
import java.net.MalformedURLException;

public class ServerTask {

    public StringIntegerHash wordFrequency = new StringIntegerHash();

    public ServerTask(Socket s, Integer maxconcurrency)throws MalformedURLException{

        List<String> urlList = GetURLFromClient(s);

        // Print the list of valid urls in the server terminal.
        System.out.println("Valid URLs are:");
        for(String url : urlList){System.out.println(url);}

        // Call a new worker for each url if a slot is available and wait for all of them to finish their job.
        ExecutorService executorService = Executors.newFixedThreadPool(maxconcurrency);
        for(String url : urlList){executorService.execute(new Worker(url, this.wordFrequency));}
        executorService.shutdown();
        while(!executorService.isTerminated()){}

        // Print the most frequent words in the server and host terminal.
        System.out.println(wordFrequency.GetStringOfMostFrequent(10));
        try{
            s.getOutputStream().write(this.wordFrequency.GetStringOfMostFrequent(10).getBytes());
        }catch(IOException e){e.printStackTrace();}
    }


    // Argument: A socket.
    // Task: Get URLs from the inputStream and convert it to a list of strings containing each a valid URL.
    // Return: Valid URLs (considered as starting with "http://") in the form of a list of strings.
    public List<String> GetURLFromClient(Socket s){

        String codemessage = "";

        try{
            InputStream in = s.getInputStream();
            s.setSoTimeout(60000);

            // Get the message from the client.
            while(true){
                try{codemessage += (char)in.read();}
                catch(IOException e){
                    String sup = "Exception occured in ServerTask. See next line: \r\n" + e;
                    System.out.println(sup);
                    try{s.getOutputStream().write(sup.getBytes());}
                    catch(IOException ee){break;}
                    break;
                }
                if(codemessage.indexOf("\r\n\r\n")!=-1){break;}
            }
        }catch(IOException e){e.printStackTrace();}

        // Check the validity of each URL.
        String[] coupecoupe = codemessage.split("\r\n");
        List<String> urlList = new ArrayList<>(Arrays.asList(coupecoupe));

        for(int i = 0 ; i < urlList.size(); i++){
            if(!PatternChecker(urlList.get(i))){
                urlList.remove(i);
            }
        }
        return urlList;
    }


    // Argument: A URL as string and a string pattern.
    // Task: Compare the starting parts of URLs to check their validity.
    // Return: True if the pattern Pat belongs to toCheck, false if it doesn't.
    public static boolean PatternChecker(String toCheck, String Pat){
        return Pattern.compile(Pat, Pattern.CASE_INSENSITIVE).matcher(toCheck).find();
    }

    public static boolean PatternChecker(String toCheck){
        return PatternChecker(toCheck, "\\b(http://)");
    }

}
