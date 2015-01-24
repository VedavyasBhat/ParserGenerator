

import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

/**
 * 
 * A parser that takes a table as input.
 * 
 * NOTE: A state number pushed into the stack is prepended and appended with a dot, 
 * to distinguish it from a terminal, in case the grammar has numbers as terminals
 */

public class Parser 
{
	private Table table;
	HashMap<String, Integer> map;
	DFA dfa;
	StringBuffer output;
	
	public Parser(Table table)
	{
		this.table = table;
		dfa = table.getDFA();
		map = table.getColumnMap();
	}
	
	public Table getTable()
	{
		return table;
	}
	
	public StringBuffer parse(String input)
	{
		output = new StringBuffer();
		input = input + "$";
		//System.out.println("Parsing input: "+input+" of length "+input.length());
		
		Stack<String> stack = new Stack<String>();
		
		stack.push("$");
		stack.push(".0.");
		
		int i = 0;
		while(i < input.length())
		{
			String token = getNextToken(input, i);
			//System.out.println("\n\nNext token is: "+token+", i is "+i);
			if(token == null)
			{
				output.append("Input: "+input+"\nString REJECTED: Unidentified symbol");
				System.out.println("String rejected: Unidentified symbol.");
				return output;
			}
			
			int row = stackStateToNum(stack.peek());
			int column = map.get(token);
			
			Table.Entry entry = table.getEntry(row, column);
			if(entry == null)
			{
				output.append("Input: "+input+"\nString REJECTED: Table entry null");
				System.out.println("String rejected: No entry in the table");
				return output;
			}
			char action = entry.getEntryType();
			
			//System.out.println("Table value: "+action+" "+entry.getRuleNumber());
			
			switch(action)
			{
				case 's': int rule = entry.getRuleNumber();
						  stack.push(token);
						  stack.push(numToStackState(rule));
						  
						  //System.out.println("Stack after shifting: "+stack);
						  break;
						  
				case 'r': int reducerule = entry.getRuleNumber();
						  if(reducerule == 0)
						  {
							  output.append("Input: "+input+"\nString ACCEPTED");
							  System.out.println("String accepted!");
							  return output;
						  }
						  String rhs = dfa.getGrammar().getProduction(reducerule).getRHSAsString();
						  //System.out.println("RHS is "+rhs+" for production "+dfa.getGrammar().getProduction(reducerule));
						  String temp = "";
						  while(true)
						  {
							  if(stack.isEmpty())
							  {
								  output.append("Input: "+input+"\nString REJECTED: Stack is empty while trying to reduce");
								  System.out.println("String rejected: Stack is empty while trying to reduce");
								  return output;
							  }
							  
							  if(!isStackState(stack.peek()))
								  temp = stack.pop() + temp;
							  else
								  stack.pop();
							  
							  //System.out.println("temp is now "+temp);
							  
							  if(temp.equals(rhs))
							  {
								  //find lhs of this rhs
								  String lhs = "";
								  for(Grammar.Production p: dfa.getGrammar())
								  {
									  if(p.getRHSAsString().equals(rhs))
										  lhs = p.getLHS();
								  }
								  
								  int temptop = stackStateToNum(stack.peek());
								  stack.push(lhs);
								  stack.push(numToStackState(table.getEntry(temptop, map.get(lhs)).getRuleNumber()));
								  //System.out.println("Reduce "+rhs+" to "+lhs+", stack is now: "+stack);
								  
								  break;
							  }
						  }
						  continue;
			}
			
			if(!token.equals("$"))
				i += token.length();
		}
		
		return output;
	}
	
	private String numToStackState(int num)
	{
		return "." + String.valueOf(num) + ".";
	}
	
	private int stackStateToNum(String state)
	{
		return Integer.parseInt(state.substring(1, state.length() - 1));
	}
	
	private boolean isStackState(String string)
	{
		if(string.length() < 3)
			return false;
		
		if(string.charAt(0) == '.' && string.charAt(string.length() - 1) == '.')
			return true;
		else
			return false;
	}
	
	private String getNextToken(String input, int from)
	{
		String temp = "";
		for(int i=from; i<input.length(); i++)
		{
			temp = temp + input.charAt(i);
			if(dfa.getGrammar().getAlphabet().contains(temp) || temp.equals("$"))
				return temp;
		}
		
		return null;
	}
	
	public static void main(String[] args)
	{
		Alphabet a = new Alphabet();
		
		/*String[] p = {"S->P$", "P->XX", "X->aX", "X->b"};	//a*ba*b
		a.add("a");
		a.add("b");
		
		String[] p = {"S->P$", "P->V=E", "P->E", "E->V", "V->x", "V->*E"};
		
		String[] p = {"S->P$", "P->aA", "A->bA", "A->a"};
		a.add("a");
		a.add("b");
		
		String[] p = {"S->A$", "A->0A1", "A->B", "B->#"};
		a.add("0");
		a.add("1");
		a.add("#");
		
		String[] p = {"S->E$", "E->E+E", "E->E-E", "E->E*E", "E->E/E", "E->id"};
		a.add("+");
		a.add("-");
		a.add("*");
		a.add("/");
		a.add("id");
		
		String[] p = {"S->E$", "E->E+T", "E->E-T", "E->T", "T->T*F", "T->T/F", "T->F", "F->(E)", "F->id"};
						//"id->0", "id->1", "id->2", "id->3", "id->4", "id->5", "id->6", "id->7", "id->8", "id->9"};
		*/
		
		String[] p = {"S->R$","R->Tt", "T->t", "T->tt"};
		a.add("t");
		
		Grammar g = new Grammar(a, p);
		DFA dfa = new DFA(g);
		dfa.printDFA();
		
		for(Grammar.Production pr: g.getAllProductions())
			System.out.println(pr.getRuleNumber()+": "+pr);
		
		Table table = new Table(dfa);
		
		Scanner sc = new Scanner(System.in);
		
		table.printTable();
		
		sc.close();
	}
}