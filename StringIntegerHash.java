import java.util.*;

public class StringIntegerHash {

	Map<String, Integer> WordOcc;

	public StringIntegerHash(){this.WordOcc = new HashMap<String, Integer>();}

  // Argument: A string.
  // Task: Add a word in the HashMap or increase its frequency if it's already in.
	public synchronized void AddToHash(String string){

		if(this.WordOcc.containsKey(string)){
			this.WordOcc.put(string, this.WordOcc.get(string)+1);
			return;
		}
		if(string.length() != 0){this.WordOcc.put(string, 1);}
		return;
	}


	// Argument: A list of strings.
  // Task: Add words from a list in the HashMap or increase their frequencies if they are already in.
	public synchronized void AddToHash(List<String> Stringlist){

		for(String string : Stringlist){
			if(this.WordOcc.containsKey(string)){
				this.WordOcc.put(string, this.WordOcc.get(string)+1);
				continue;
			}
			if(string.length() != 0){this.WordOcc.put(string, 1);}
		}
		return;
	}


	// Argument: An integer.
  // Task: Find the most frequent words in a HashMap.
  // Return: The x most frequent words in the HashMap with their frequencies formated as a string, where x is the integer given as argument.
	public String GetStringOfMostFrequent(Integer num){
		if(this.WordOcc.size() == 0){return "There is no word to diplay.";}
		if(num > this.WordOcc.size()){num = this.WordOcc.size();}
		HashMap<String, Integer> MapCopy = new HashMap<>();
		Integer tmpi = 0; String tmps = "";
		String strtoreturn = "\n\r\n The " + Integer.toString(num) + " most frequent words are: \n\n";
		MapCopy.putAll(this.WordOcc);

		for(Integer i = 0; i<num ; i++){
			for(Map.Entry<String, Integer> Temp : MapCopy.entrySet()){
				if(Temp.getValue() > tmpi){
					tmps = Temp.getKey();
					tmpi = Temp.getValue();
				}
			}
			strtoreturn += tmps + " " + Integer.toString(tmpi) + "\n";
			MapCopy.remove(tmps);
			tmpi = 0;
		}
		return strtoreturn;
	}
}
