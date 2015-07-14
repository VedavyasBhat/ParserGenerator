import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 
 * A simple class that maintains a set of terminal symbols
 * Each symbol is assumed to be a string
 * It is basically a wrapper over an ArrayList of Strings, for easy understanding
 */
public class Alphabet implements Iterable<String>
{
	private List<String> alphabet;
	public Alphabet()
	{
		alphabet = new ArrayList<String>();
	}
	
	public void add(String symbol)
	{
		if(!alphabet.contains(symbol))
			alphabet.add(symbol);
	}
	
	public boolean contains(String symbol)
	{
		return alphabet.contains(symbol);
	}
	
	public int size()
	{
		return alphabet.size();
	}

	@Override
	public Iterator<String> iterator() 
	{
		return alphabet.iterator();
	}
}