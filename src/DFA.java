

import java.util.ArrayList;
import java.util.HashSet;

/**
 * 
 * A class that creates a DFA for an inputed grammar. The grammar is expected to show
 * the following (reasonable) properties:
 * 1. Grammar is augmented
 * 2. Start symbol is S
 * 3. First production (in the list of productions) is S -> ....
 * 4. There is only one production for S
 *
 * The output is a DFA with following properties:
 * 1. Maintains which state is reduce state
 * 2. Maintains a separate copy of each production in each state.
 */
public class DFA 
{
	/**
	 * 
	 * @author admin
	 * A class that represents a DFA state
	 * Inner classes: Production, State
	 *  
	 *  Maintains a pointer to the head of the DFA, that is the initial state
	 *  Maintains a list of states too:
	 *  Useful when a check needs to be done to see if a state already exists
	 *
	 */
	public class State
	{
		/**
		 * 
		 * @author Vedavyas Bhat
		 * A class that is used to link two states of the DFA
		 * Has a transition element
		 *
		 */
		public class Link
		{
			State ptr;
			String transition;
			
			public Link(State ptr, String transition)
			{
				this.ptr = ptr;
				this.transition = transition;
			}
		}
		
		/**
		 * 
		 * @author Vedavyas Bhat
		 * 
		 * A class which represents a production within a DFA state
		 * Extends Grammar.Production
		 * Adds a dot and lookahead set to the parent class
		 * Has a function to calculate the lookahead for the productions 
		 * that will be expanded from it
		 * 
		 * The dot parameter is a pointer to the arrayrhs
		 */
		public class Production extends Grammar.Production
		{	
			private int dot;
			private HashSet<String> lookahead;
			
			/*
			 * Creates a DFA production from a Grammar production and a set of lookaheads
			 */
			public Production(Grammar.Production p, HashSet<String> looks)
			{
				grammar.super(p);
				dot = 0;
				lookahead = new HashSet<String>(looks);
			}
			
			/*
			 * Creates a DFa production from an existing DFA production
			 * NOTE: Since the only time a DFA production is created form another one 
			 * is during a transition, the dot is automatically incremented in this constructor
			 */
			public Production(Production p)
			{
				grammar.super(p);
				
				dot = p.dot + 1;
				lookahead = new HashSet<String>(p.lookahead);
				
				System.out.println("Constructed "+this+" from "+p);
			}
			
			public HashSet<String> getFirstset()
			{
				return lookahead;
			}
			
			public String getSymbolAfterDot()
			{
				return arrayrhs.get(dot);	//may not work
			}
			
			public boolean dotAtEnd()
			{
				return dot >= arrayrhs.size();
			}
			
			public HashSet<String> getLookaheadForChildren()
			{
				/*Since we assume that non-terminals aren't NULLABLE, we just take the 
				 * first of the symbol after the symbol after the dot
				 * if no such symbol, simply return current ones lookahead
				 */
				
				String nexttonext = null;
				if(dot + 1 < arrayrhs.size())
				{
					dot++;
					nexttonext = getSymbolAfterDot();
					dot--;
					
					if(grammar.isNonTerminal(nexttonext))
						return grammar.getFirstSet(nexttonext);
					else
					{
						HashSet<String> set = new HashSet<String>();
						set.add(nexttonext);
						return set;
					}
				}
				else
					return lookahead;
			}
			
			/*
			 * Simple function that does string manipulations 
			 * @see ParserGenerator.Grammar.Production#toString()
			 */
			@Override
			public String toString()
			{
				String str = lhs + "->";
				
				for(int i=0; i<arrayrhs.size(); i++)
				{
					if(i == dot)
						str = str + ".";
					
					str = str + arrayrhs.get(i);
				}
				
				if(dot == arrayrhs.size())
					str = str + ".";
				
				str = str + "    " + lookahead;
				
				return str;
			}
			
			@Override
			public boolean equals(Object o)
			{
				if(o instanceof Production)
				{
					Production temp = (Production) o;
					if(temp.lhs.equals(this.lhs) && temp.stringrhs.equals(this.stringrhs) && temp.dot == this.dot && lookahead.equals(temp.lookahead))
						return true;
				}
				
				return false;
			}
		}
		
		ArrayList<Production> productions;
		ArrayList<Link> links;
		int number = -1;
		boolean isreducestate;
		
		public State()
		{
			productions = new ArrayList<Production>();
			links = new ArrayList<Link>();
			isreducestate = false;
		}
		
		public void setNumber(int number)
		{
			this.number = number;
		}
		
		public void addProduction(Production p)
		{
			productions.add(p);
		}
		
		public boolean containsProduction(Production production)
		{
			return productions.contains(production);
		}
		
		public Production[] getAllProductions()
		{
			Production[] prods = new Production[productions.size()];
			
			int i = 0;
			for(Production d: productions)
				prods[i++] = d;
			
			return prods;
			
		}
		
		public void addLink(Link link)
		{
			links.add(link);
		}
		
		public Link[] getAllLinks()
		{
			Link[] alllinks = new Link[links.size()];
			
			int i = 0;
			for(Link d : links)
				alllinks[i++] = d;
			
			return alllinks;
		}
		
		public Production getProduction(int index)
		{
			return productions.get(index);
		}
		
		public int productionCount()
		{
			return productions.size();
		}
		
		private boolean totallyEqual(State first, State second)
		{
			Production[] firstprods = first.getAllProductions();
			Production[] secondprods = second.getAllProductions();
			boolean foundequalprod;
			
			for(Production firstprod: firstprods)
			{
				foundequalprod = false;
				for(Production secondprod: secondprods)
				{
					if(firstprod.equals(secondprod))
					{
						foundequalprod = true;
						break;
					}
				}
				if(!foundequalprod)
					return false;
			}
			return true;
		}
		
		@Override
		public boolean equals(Object o)
		{
			if(o instanceof State)
			{
				State st = (State) o;
				return totallyEqual(this, st) && totallyEqual(st, this);
			}
			
			return false;
		}
	}
	
	private Grammar grammar;
	private ArrayList<State> states;
	State initial;
	int statecount = 0;
	StringBuffer output;
	
	public DFA(Grammar grammar)
	{
		this.grammar = grammar;
		states = new ArrayList<State>();
		buildDFA();
		numberStates(initial);
	}
	
	public Grammar getGrammar()
	{
		return grammar;
	}
	
	public State getInitialState()
	{
		return initial;
	}
	
	public State getState(int number)
	{
		for(State state: states)
		{
			if(state.number == number)
				return state;
		}
		return null;
	}
	
	private State getState(State copy)
	{
		for(State state: states)
		{
			if(state.equals(copy))
				return state;
		}
		return null;
	}
	
	public ArrayList<State> getStateList()
	{
		return states;
	}
	
	private void buildDFA()
	{
		Grammar.Production[] productions = grammar.getAllProductions();
		
		initial = new State();
		//initial.setNumber(statecount++);	//dfs numbering
		HashSet<String> init = new HashSet<String>();
		init.add("$");
		State.Production startprod = initial.new Production(productions[0], init);	//start production
		initial.addProduction(startprod);
		
		constructState(initial);
		states.add(initial);
		
		String[] symbols = grammar.getAllSymbols();
		generateNextStates(initial, symbols);
	}
	
	/*
	 * Constructs a state that contains at least one production
	 * Generates new productions based on position of the dot in current productions
	 */
	private void constructState(State state)
	{
		State.Production[] allprods = state.getAllProductions();
		
		for(int i=0; i<allprods.length; i++)
		{
			//check if this prod can be expanded
			State.Production prod = state.getProduction(i);
			
			if(prod.dotAtEnd())
				continue;
			
			String next = prod.getSymbolAfterDot();
			
			if(grammar.isNonTerminal(next))
			{
				//prod can be expanded! So find lookahead for children
				HashSet<String> lookaheadforchildren = prod.getLookaheadForChildren();
				
				Grammar.Production[] prods = grammar.getProductionsWithLHS(next);
				for(Grammar.Production pr: prods)
				{
					State.Production addable = state.new Production(pr, lookaheadforchildren);
					
					if(!state.containsProduction(addable))
						state.addProduction(addable);
				}
				allprods = state.getAllProductions();
			}
		}
		
		//Mark if this state is a reduce state. Parameter will be used by Table
		for(State.Production p: state.getAllProductions())
		{
			if(p.dotAtEnd())
				state.isreducestate = true;
		}
	}
	
	/*
	 * Generates new states based on transitions of productions in current state
	 */
	private void generateNextStates(State state, String[] symbols)
	{
		for(String transsym: symbols)
		{
			State newstate = new State();
			for(State.Production p: state.getAllProductions())
			{
				if(p.dotAtEnd())
					continue;
				
				if(p.getSymbolAfterDot().equals(transsym))
					newstate.addProduction(newstate.new Production(p));
			}
			
			//If state for this transition is not empty
			if(newstate.productionCount() != 0)
			{
				//finish adding the rest of the productions to this state
				constructState(newstate);
				
				//if the state already exists, find the old state and link to that
				if(states.contains(newstate))
				{
					State oldstate = getState(newstate); //will never be null... oh but it is :/
					
					State.Link link = state.new Link(oldstate, transsym);
					state.addLink(link);
				}
				
				else
				{
					//newstate.setNumber(statecount++);		//used for the dfs numbering of states
					State.Link link = state.new Link(newstate, transsym);
					state.addLink(link);
					states.add(newstate);
					
					generateNextStates(newstate, symbols);
				}
			}
		}
	}

	//Numbers the states in a bfs manner
	private void numberStates(State state)
	{
		state.number = statecount++;
		
		for(State.Link link: state.getAllLinks())
		{
			if(link.ptr != null && link.ptr.number == -1)
				numberStates(link.ptr);
		}
	}
	
	private void printStateInfo(State state, boolean[] done)
	{
		if(done[state.number])
			return;
		
		done[state.number] = true;
		
		output.append("\nState "+state.number+"\n");
		System.out.println("\nState "+state.number);
		
		output.append("--------\n");
		System.out.println("--------");
		
		for(State.Production p: state.getAllProductions())
		{
			output.append(p+"\n");
			System.out.println(p);
		}
		
		for(State.Link link: state.getAllLinks())
		{
			if(link.ptr != null)		//is it really necessary? Prediction: link.ptr will never be null
			{
				output.append("Goes to "+link.ptr.number+" on "+link.transition+"\n");
				System.out.println("Goes to "+link.ptr.number+" on "+link.transition);
			}
		}
		
		for(State.Link link: state.getAllLinks())
		{
			if(link.ptr != null)
				printStateInfo(link.ptr, done);
		}
	}
	
	public StringBuffer printDFA()
	{
		boolean[] done = new boolean[statecount];
		
		output = new StringBuffer();
		printStateInfo(initial, done);
		
		return output;
	}
	
	public static void main(String[] args)
	{
		Alphabet a = new Alphabet();
		a.add("+");
		a.add("*");
		a.add("(");
		a.add(")");
		a.add("id");
		a.add("x");
		a.add("=");
		a.add("m");
		a.add("d");
		//String[] p = {"P->S$", "S->L=R", "S->R", "L->*R", "L->id", "R->L"};
		
		//String[] p = {"S->E$", "E->E+T", "E->T", "T->T*F", "T->F", "F->(E)", "F->id"};
		
		String[] p = {"S->P$", "P->V=E", "P->E", "E->V", "V->x", "V->*E"};
		
		//String[] p = {"S->P$", "P->CC", "C->mC", "C->d"};
		
		Grammar g = new Grammar(a, p);
		g.printFirstSets();
		
		DFA dfa = new DFA(g);
		StringBuffer op = dfa.printDFA();
		System.out.println(op);
	}
}