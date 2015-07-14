import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class GUI 
{
	private JFrame frmLrParserGenerator;
	
	private JTextField textParse;
	
	private JTextArea textareaInputProductions;
	private JTextArea textareaInputAlphabet;
	private JTextArea textareaOutput;
	
	private JButton btnParse;
	private JButton btnDisplayFirstSets;
	private JButton btnDisplayDfa;
	private JButton btnParseTable;
	
	private JScrollPane scrollPane;
	
	
	private Alphabet alphabet;
	private Grammar grammar;
	private DFA dfa;
	private Table table;
	private Parser parser;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					GUI window = new GUI();
					window.frmLrParserGenerator.setVisible(true);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() 
	{
		initialize();
	}
	
	private void initStuff()
	{
		initAlphabet();
		initGrammar();
		initDFA();
		initTable();
		initParser();
	}
	
	public void initAlphabet()
	{
		alphabet = new Alphabet();
		
		String symbols = textareaInputAlphabet.getText();
		
		String symbol = "";
		for(int i=0; i<symbols.length(); i++)
		{
			if(symbols.charAt(i) != '\n')
				symbol += symbols.charAt(i);
			else
			{
				alphabet.add(new String(symbol));
				symbol = "";
			}
		}
		alphabet.add(symbol);
	}
	
	private void initGrammar()
	{
		ArrayList<String> prods = new ArrayList<String>();
		
		String input = textareaInputProductions.getText();
		String prod = "";
		
		for(int i=0; i<input.length(); i++)
		{
			if(input.charAt(i) != '\n')
				prod += input.charAt(i);
			else
			{
				prods.add(new String(prod));
				prod = "";
			}
		}
		prods.add(prod);
		
		grammar = new Grammar(alphabet, prods.toArray(new String[prods.size()]));
	}
	
	private void initDFA()
	{
		dfa = new DFA(grammar);
	}
	
	private void initTable()
	{
		table = new Table(dfa);
	}
	
	private void initParser()
	{
		parser = new Parser(table);
	}
	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() 
	{
		frmLrParserGenerator = new JFrame();
		frmLrParserGenerator.setResizable(false);
		frmLrParserGenerator.setTitle("LR(1) parser generator");
		frmLrParserGenerator.setBounds(50, 50, 700, 500);
		frmLrParserGenerator.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmLrParserGenerator.getContentPane().setLayout(null);
		
		
		String instructions = "<html><body><u>Rules for use:</u><br>1. Grammar must be augmented<br>" +
				"2. First production must be the augmented production<br></body></html>";
		
		JLabel labelInstructions = new JLabel(instructions);
		labelInstructions.setBounds(12, 0, 303, 90);
		frmLrParserGenerator.getContentPane().add(labelInstructions);
		
		textareaInputProductions = new JTextArea();
		textareaInputProductions.setLineWrap(true);
		textareaInputProductions.setText("Enter the productions line by line");
		textareaInputProductions.setBounds(450, 252, 228, 153);
		frmLrParserGenerator.getContentPane().add(textareaInputProductions);
		
		btnParse = new JButton("Parse string");
		btnParse.setBounds(450, 59, 228, 26);
		frmLrParserGenerator.getContentPane().add(btnParse);
		
		btnDisplayFirstSets = new JButton("Display first sets");
		btnDisplayFirstSets.setBounds(450, 32, 228, 26);
		frmLrParserGenerator.getContentPane().add(btnDisplayFirstSets);
		
		btnDisplayDfa = new JButton("Display DFA");
		btnDisplayDfa.setBounds(450, 4, 228, 26);
		frmLrParserGenerator.getContentPane().add(btnDisplayDfa);
		
		textParse = new JTextField();
		textParse.setText("Enter string to be parsed");
		textParse.setBounds(450, 417, 228, 43);
		frmLrParserGenerator.getContentPane().add(textParse);
		textParse.setColumns(10);
		
		textareaInputAlphabet = new JTextArea();
		textareaInputAlphabet.setText("Enter the alphabet line by line");
		textareaInputAlphabet.setLineWrap(true);
		textareaInputAlphabet.setBounds(450, 124, 228, 116);
		frmLrParserGenerator.getContentPane().add(textareaInputAlphabet);
		
		textareaOutput = new JTextArea();
		textareaOutput.setEditable(false);
		textareaOutput.setText("Output");
		textareaOutput.setLineWrap(true);
		
		scrollPane = new JScrollPane(textareaOutput);
		scrollPane.setBounds(12, 95, 426, 365);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		frmLrParserGenerator.getContentPane().add(scrollPane);
		
		btnParseTable = new JButton("Display parsing table");
		btnParseTable.setBounds(450, 86, 228, 26);
		frmLrParserGenerator.getContentPane().add(btnParseTable);
		
		setDisplayDFAListener();
		setDisplayFirstSetsListener();
		setDisplayParseTableListener();
		setParseListener();
		setDisplayParseTableListener();
	}
	
	private void setDisplayDFAListener()
	{
		btnDisplayDfa.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent event) 
							{
								initStuff();
								textareaOutput.setText(dfa.printDFA().toString());
							}
						});
	}
	
	private void setDisplayFirstSetsListener()
	{
		btnDisplayFirstSets.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent event) 
							{
								initStuff();
								textareaOutput.setText(grammar.printFirstSets().toString());
							}
						});
	}
	
	private void setDisplayParseTableListener()
	{
		btnParseTable.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent event) 
			{
				initStuff();
				textareaOutput.setText(table.printTable().toString());
			}
		});
	}
	
	private void setParseListener()
	{
		btnParse.addActionListener(new ActionListener()
						{
							@Override
							public void actionPerformed(ActionEvent event) 
							{
								initStuff();
								textareaOutput.setText(parser.parse(textParse.getText()).toString());
							}
						});
	}
}