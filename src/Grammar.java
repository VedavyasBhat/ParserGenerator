

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 
 * A class that represents a formal grammar. Important functions:
 * 1. Parses the strings of productions and converts them to Production objects
 * 2. Maintains which symbols are non-terminals
 * 3. Has important get methods used by various other classes
 * 
 * 
 */
public class Grammar implements Iterable<Grammar.Production> 
{
	/**
	 * 
	 * A simple class that represents a production.
	 * 
	 */
	public class Production
	{
		String lhs;
		String stringrhs;
		ArrayList<String> arrayrhs;
		int rulenumber;
		
		public Production(String production)
		{
			String[] temp = production.split("->");
			lhs = temp[0];
			stringrhs = temp[1];
			arrayrhs = new ArrayList<String>();
			rulenumber = productioncount++;
			
			String r;
			
			//Parsing the rhs to store each element in the ArrayList
			for(int i=0; i<stringrhs.length(); i++)
			{
				r = "";
				for(int j=i; j<stringrhs.length(); j++)
				{
					r = r + stringrhs.charAt(j);
					if(alphabet.contains(r) || nonterminals.contains(r))
					{
						arrayrhs.add(new String(r));
						i += r.length() - 1;
						break;
					}
				}
			}
		}
		
		/*Creates a production from a given production
		 * Not used directly, but used by the child class' (DFA.Production class') constructor 
		 */
		public Production(Production production)
		{
			this.lhs = production.lhs;
			this.stringrhs = production.stringrhs;
			this.arrayrhs = production.arrayrhs;
			this.rulenumber = production.rulenumber;
		}
		
		public String getLHS()
		{
			return lhs;
		}
		
		public String getRHSAsString()
		{
			return stringrhs;
		}
		
		public String[] getRHSAsStringArray()
		{
			String[] r = new String[arrayrhs.size()];
			
			int i = 0;
			for(String s: arrayrhs)
				r[i++] = s;
			
			return r;
		}
		
		public int getRuleNumber()
		{
			return rulenumber;
		}
		
		@Override
		public String toString()
		{
			return lhs + "->" + stringrhs;
		}
	}
	
	private Alphabet alphabet;
	private int productioncount;
	private ArrayList<Production> productions;
	private Set<String> nonterminals;
	StringBuffer output;
	
	private HashMap<String, HashSet<String>> firstmap;
	
	public Grammar(Alphabet alphabet, String[] productions)
	{
		this.alphabet = alphabet;
		nonterminals = new HashSet<String>();
		this.productions = new ArrayList<Production>();
		
		//find all nonterminals
		for(String prod: productions)
			nonterminals.add(prod.split("->")[0]);
		
		//create productions
		for(String prod: productions)
			this.productions.add(new Production(prod));
		
		firstmap = new HashMap<String, HashSet<String>>();
		
		//for every non terminals, create an empty first set
		for(String non: nonterminals)
		{
			HashSet<String> firstset = new HashSet<String>();
			firstmap.put(non, firstset);
		}
		
		computeFirsts();
	}
	
	private void addFirstSet(String symbol, HashSet<String> set)
	{
		firstmap.get(symbol).addAll(set);
	}
	
	private void addFirstSymbol(String symbol, String firstelement)
	{
		firstmap.get(symbol).add(firstelement);
	}
	
	
	public int productionCount()
	{
		return productioncount;
	}
	
	public boolean isNonTerminal(String string)
	{
		return nonterminals.contains(string);
	}
	
	public Production getProduction(int rule)
	{
		for(Production p: productions)
		{
			if(p.getRuleNumber() == rule)
				return p;
		}
		
		return null;
	}
	
	public Alphabet getAlphabet()
	{
		return alphabet;
	}
	
	public String[] getAllNonTerminals()
	{
		String[] nons = new String[nonterminals.size()];
		
		int i = 0;
		for(String s: nonterminals)
			nons[i++] = s;
		
		return nons;
	}
	
	//Used by the DFA to expand a production
	public Production[] getProductionsWithLHS(String lhs)
	{
		int count = 0;
		for(Production prod: productions)
			if(prod.getLHS().equals(lhs))
				count++;
		
		Production[] prods = new Production[count];
		
		int i = 0;
		for(Production prod: productions)
			if(prod.getLHS().equals(lhs))
				prods[i++] = prod;
		
		return prods;
	}
	
	public String[] getAllSymbols()
	{
		String[] ans = new String[nonterminals.size() + alphabet.size()];
		
		int i = 0;
		for(String s: nonterminals)
			ans[i++] = s;
		
		for(String s: alphabet)
			ans[i++] = s;
		
		return ans;
	}
	
	public Production[] getAllProductions()
	{
		return productions.toArray(new Production[productions.size()]);
	}
	
	public HashSet<String> getFirstSet(String nonterminal)
	{
		if(nonterminal == null)
			return null;
		
		return firstmap.get(nonterminal);
	}
	
	/*
	 * Very important function
	 * Sorts the productions so that calculating the first sets becomes easy
	 * Order of productions:
	 * 1. Productions that have a terminal at the beginning of the rhs
	 * 2. Other productions of the above non-terminals (which have non-terminals
	 * 		 at beginning of rhs)
	 * 3. Every other production which has the above non-terminals at beginning of rhs   
	 */
	private ArrayList<Production> getSortedProductions()
	{
		ArrayList<Production> sorted = new ArrayList<Production>();
		
		HashSet<String> lhs = new HashSet<String>();
		
		//Finds productions with terminals at beginning of rhs
		for(Production prod: productions)
		{
			String[] rhs = prod.getRHSAsStringArray();
			
			if(alphabet.contains(rhs[0]))
			{
				lhs.add(prod.getLHS());
				sorted.add(prod);
			}
			
		}
		
		ArrayList<Production> newsorted = new ArrayList<Production>(sorted);
		
		//Finds productions of the above non-terminals
		for(Production prod: productions)
		{
			if(!sorted.contains(prod))
			{
				String anlhs = prod.getLHS();
				for(Production sortedprod: sorted)
				{
					String sortedlhs = sortedprod.getLHS();
					if(anlhs.equals(sortedlhs))
					{
						newsorted.add(prod);
						lhs.add(anlhs);
					}
				}
			}
		}
		
		sorted = newsorted;
		
		//Fills in the rest of the productions
		while(sorted.size() != productions.size())
		{
			for(Production p: productions)
			{
				if(!sorted.contains(p))
				{
					if(lhs.contains(p.getRHSAsStringArray()[0]))
					{
						sorted.add(p);
						lhs.add(p.getLHS());
					}
				}
			}
		}
		
		return sorted;
	}
	
	//Simply add first of beginning of rhs to lhs
	private void computeFirsts()
	{
		ArrayList<Production> sortedprods = getSortedProductions();
		
		System.out.println("Sorted prods: ");
		for(Production p: sortedprods)
			System.out.println(p);
		
		for(Production prod: sortedprods)
		{
			String lhs = prod.getLHS();
			String firstrhs = prod.getRHSAsStringArray()[0];
			
			if(isNonTerminal(firstrhs))
				addFirstSet(lhs, firstmap.get(firstrhs));
			else
				addFirstSymbol(lhs, firstrhs);
		}
		
		for(Production prod: sortedprods)
		{
			String lhs = prod.getLHS();
			String firstrhs = prod.getRHSAsStringArray()[0];
			
			if(isNonTerminal(firstrhs))
				addFirstSet(lhs, firstmap.get(firstrhs));
			else
				addFirstSymbol(lhs, firstrhs);
		}
	}
	
	public StringBuffer printFirstSets()
	{
		output = new StringBuffer();
		for(String non: nonterminals)
		{
			output.append(non+" : "+firstmap.get(non)+"\n");
			System.out.println(non+" : "+firstmap.get(non));
		}
		
		return output;
	}
	
	@Override
	public Iterator<Grammar.Production> iterator() 
	{
		return productions.iterator();
	}
	
	public static void main(String[] args)
	{
		Alphabet a = new Alphabet();
		a.add("a");
		a.add("b");
		a.add("+");
		a.add("*");
		a.add("(");
		a.add(")");
		a.add("id");
		
		String[] prods = {"S->P$", "P->A", "A->B", "B->C", "C->P", "C->a", "C->b"};
		//String[] prods = {"P->S$", "S->XX", "X->aX", "X->b"};
		
		
		Grammar g = new Grammar(a, prods);
		
		System.out.println("FIRST SETS: ");
		g.printFirstSets();
	}
}
