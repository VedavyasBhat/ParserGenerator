import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * Simple class that reads a DFA and converts it into table format for the parser
 * Data does not lose integrity even in the case of conflicts
 */
public class Table 
{
	/*
	 * A class that represents an entry in the table
	 * Has a type: s (shift), r (reduce) or g (goto)
	 * Has a rule number parameter
	 */
	public class Entry
	{
		private char type;
		private int rulenumber;
		
		public Entry(char type, int rulenumber)
		{
			this.type = type;
			this.rulenumber = rulenumber;
		}
		
		public char getEntryType()
		{
			return type;
		}
		
		public int getRuleNumber()
		{
			return rulenumber;
		}
		
		@Override
		public String toString()
		{
			return type + String.valueOf(rulenumber);
		}
	}
	
	private DFA dfa;
	private Entry[][] table;
	private int rows, columns;
	private HashMap<String, Integer> map;
	private ArrayList<Entry> conflictentries;
	private ArrayList<Integer> conflictrows;
	private ArrayList<Integer> conflictcolumns;
	StringBuffer output;

	public Table(DFA dfa)
	{
		this.dfa = dfa;
		this.rows = dfa.statecount;	//starts from zero
		this.columns = dfa.getGrammar().getAllSymbols().length + 1;	// +1 because $ is not included as a terminal
		
		map = new HashMap<String, Integer>();
		table = new Entry[this.rows][this.columns];
		conflictentries = new ArrayList<Entry>();
		conflictrows = new ArrayList<Integer>();
		conflictcolumns = new ArrayList<Integer>();
		
		buildMap(map);
		buildTable();
	}
	
	public DFA getDFA()
	{
		return dfa;
	}
	
	public HashMap<String, Integer> getColumnMap()
	{
		return map;
	}
	
	private void buildMap(HashMap<String, Integer> map)
	{
		int i=0;
		String[] allsymbols = dfa.getGrammar().getAllSymbols();
		
		for(String s: allsymbols)
			map.put(s, i++);
		
		map.put("$", i);	//handle this while printing table
							//otherwise this causes no problems
	}
	
	private void buildTable()
	{
		for(int i=0; i<rows; i++)
		{
			DFA.State state = dfa.getState(i);
			fillRow(state);
		}
	}
	
	private void fillRow(DFA.State state)
	{
		//Fill in shift and goto
		for(DFA.State.Link link: state.getAllLinks())
		{
			int rulenumber = link.ptr.number;
			char type;
			
			if(dfa.getGrammar().isNonTerminal(link.transition))
				type = 'g';
			else
				type = 's';
			
			Table.Entry entry = new Entry(type, rulenumber);
			setEntry(entry, state.number, link.transition);
		}
		
		//Fill in reduce
		if(state.isreducestate)
		{
			for(DFA.State.Production prod: state.getAllProductions())
			{
				if(prod.dotAtEnd())
				{
					Entry entry = new Entry('r', prod.getRuleNumber());
					for(String first: prod.getFirstset())
						setEntry(entry, state.number, first);
				}
			}
		}
	}
	
	public Table(int rows, int columns)
	{
		this.rows = rows;
		this.columns = columns;
		
		table = new Entry[this.rows][this.columns];
		conflictentries = new ArrayList<Entry>();
		conflictrows = new ArrayList<Integer>();
		conflictcolumns = new ArrayList<Integer>();
	}
	
	public void setEntry(Entry entry, int row, String col)
	{
		int column = map.get(col);
		
		if(table[row][column] == null)
			table[row][column] = entry;
		else
		{
			conflictentries.add(entry);
			conflictrows.add(row);
			conflictcolumns.add(column);
		}
	}
	
	public Entry getEntry(int row, int column)
	{
		if(table[row][column] != null)
		{
			//return which value?? make policy
			return table[row][column];
		}
		return null;
	}
	
	public boolean hasConflict()
	{
		return !conflictentries.isEmpty();
	}
	
	public StringBuffer printTable()
	{
		output = new StringBuffer();
		
		output.append("     ");
		System.out.print("     ");
		
		for(String s: dfa.getGrammar().getAllSymbols())
		{
			output.append(s+"         ");
			System.out.print(s+"      ");
		}
		
		output.append("$\n");
		System.out.println("$");
		
		for(int i=0; i<rows; i++)
		{
			output.append(i+"     ");
			System.out.print(i+"   ");
			for(int j=0; j<columns; j++)
			{
				if(getEntry(i, j) != null)
				{
					if(conflictrows.contains(i) && conflictcolumns.contains(j))
					{
						output.append("X       ");
						System.out.print("X     ");
					}
					else
					{
						output.append(getEntry(i, j)+"       ");
						System.out.print(getEntry(i, j)+"     ");
					}
				}
				else
				{
					output.append("---       ");
					System.out.print("--     ");
				}
			}
			System.out.println();
			output.append("\n");
		}
		
		conflictWarning();
		
		return output;
	}
	
	private void conflictWarning()
	{
		if(hasConflict())
		{
			output.append("WARNING\n---------------\nThe table has conflicts! Parser will choose entry arbitrarily. The conflicts are:\nCell          Entries\n");
			System.out.println("WARNING");
			System.out.println("-------");
			System.out.println("The parser has the following conflicts: ");
			System.out.println("Cell 	Entries");
			
			ArrayList<Integer> newrow = new ArrayList<Integer>();
			ArrayList<Integer> newcol = new ArrayList<Integer>();
			
			for(int i=0; i<conflictentries.size(); i++)
			{
				int row = conflictrows.get(i);
				int col = conflictcolumns.get(i);
				
				if(newrow.size() == 0)
				{
					newrow.add(row);
					newcol.add(col);
				}
				
				for(int j=0; j<newrow.size(); j++)
				{
					if(!(newrow.get(j) == row && newcol.get(j)== col))
					{
						newrow.add(row);
						newcol.add(col);
						break;
					}
				}
				
				System.out.println(row+", "+col+"       "+conflictentries.get(i));
				output.append(row+", "+col+"            "+conflictentries.get(i)+"\n");
			}
			
			for(int i=0; i<newrow.size(); i++)
			{
				int row = newrow.get(i);
				int col = newcol.get(i);
				System.out.println(row+", "+col+"       "+getEntry(row, col));
				output.append(row+", "+col+"            "+getEntry(row, col)+"\n");
			}
		}
	}
	
	public static void main(String[] args) 
	{
		Alphabet a = new Alphabet();
		a.add("m");
		a.add("d");
		String[] p = {"S->P$", "P->CC", "C->mC", "C->d"};
		//String[] p = {"S->E$", "E->E+T", "E->T", "T->T*F", "T->F", "F->(E)", "F->id"};
		
		
		Grammar g = new Grammar(a, p);
		DFA dfa = new DFA(g);
		dfa.printDFA();
		
		Table table = new Table(dfa);
		StringBuffer str = table.printTable();
		
		System.out.println(str);
	}
}