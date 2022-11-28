package com.shanebow.tools.Crawler;
/*****************************************************************************
*
* Sukhumwit Crawler
* Copyright 2010 by Shane Bow, Inc.
* 
* @(#)TabInputs.java	1.00 10/04/01
*
* The user interface for basic inputs to the Crawler. User selects a host to
* search, specifies any excluded url patterns, and launches the search.
* To perform the search, this module creates a new ContactCrawler object and
* invokes it.
* 
* @author Rick Salamone
* @version 1.00
*
* 20100412 RTS added functionality to exclude urls containing specified
*              strings. Also automatically save (on start search) and
*              restore (on select host) strings to disk file.
* 20100425 RTS added functionality to specify starting url as a pattern
*              containing a single numeric variable range.
* 20100507 RTS added host finda.co.nz
* 20100511 RTS added host yellow.co.nz (New Zealand White Pages)
*              
*************************************************************************/
import com.shanebow.tools.Crawler.parsers.*;
import com.shanebow.spider.SpiderOptions;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.layout.LabeledPairLayout;
import com.shanebow.util.SBLog;
import java.awt.BorderLayout;
import java.awt.event.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

public final class TabInputs extends JPanel
	implements ActionListener, PropertyChangeListener
	{
	private static final String CRAWLER_SRC_PATH   // for debugging/developemnt
			= "file:/C:/apps/src/com/shanebow/tools/Crawler/";
	private static final String SAMPLE_PAGES = "file:/C:/apps/src/com/_god/parserSamples/";

	private static final String SETTINGS_PATH = ""; // settings file in same dir as jar
	private static final String SETTINGS_EXT = ".dat";
	private static final String START="Start";
	private static final String CANCEL="Cancel";
	private static final String RANGE0 = "aa";
	private static final String RANGE1 = "aa";

	private static final String NZ_WPAGE_PROMPT = "NZ White Pages";
	private static final String NZ_WPAGE_URL 
		= "http://yellow.co.nz/whitepages/search.ds?encodedRefinement=suburb..%3d..%5e%22Auckland+Central%22%24..%26..Auckland+Central&what=smith&where=new+zealand&stageName=Composite+search&activeRefinements=listingtype..%3D..Residential..%26..Residential..%7C..region..%3D..%5E%22Auckland+Region%22%24..%26..Auckland+Region..%7C..city..%3D..%5E%22Auckland+City%22%24..%26..Auckland+City&activeSort=sortname&alphaRefineable=AN5%7CBN5%7CCN5%7CDN1%7CEN2%7CFN3%7CGN1%7CHN21%7CIN0%7CJN2%7CKN3%7CLN7%7CMN18%7CNN3%7CON0%7CPN5%7CQN0%7CRN2%7CSN598%7CTN5%7CUN0%7CVN0%7CWN7%7CXN0%7CYN1%7CZN0%7C%5B0-9%5DN0";
	//	= "http://yellow.co.nz/whitepages/search/new+zealand/%s-1.html";
	//	= SAMPLE_PAGES + "NZWPaa-1.html"; // for development
	//	= SAMPLE_PAGES + "NZWPalan-residential.html";
	//	= SAMPLE_PAGES + "NZWPalan-1.html";
	private static final String FINDA_PROMPT = "finda";
	private static final String FINDA_URL 
//		= "http://www.finda.co.nz/business/%d/";
		= SAMPLE_PAGES + "finda2.html"; // for development

	private static final String ALIBABA_PROMPT = "Alibaba";
	private static final String ALIBABA_URL
		= "http://www.alibaba.com/corporations/all_products/%d.html";
	//	= http://www.alibaba.com/trade/search?SearchText=all+products"
	//	                + "&Country=&CatId=0&IndexArea=company_en&sq=y";

	static final String SHANE_URL
		= "file:/C:/www/shanebow.com/index.html";
	static final String CRAWLER_HELP_URL
			= CRAWLER_SRC_PATH + "resources/HelpFiles/toc.html";
	private static final String SHINZEN_URL
			= "http://www.shinzen.org/"; // FrameContents.htm";
	private static final String[][] Hosts =
		{
			{ NZ_WPAGE_PROMPT, NZ_WPAGE_URL },
			{ FINDA_PROMPT,    FINDA_URL },
			{ ALIBABA_PROMPT,  ALIBABA_URL },
			{ "Shinzen",       SHINZEN_URL },
			{ "Help Files",    CRAWLER_HELP_URL },
			{ "Shane Bow",     SHANE_URL },
		};

	private JComboBox      cbHost;      // list of available hosts to seach
	private JTextField     tfPattern;   // url to begin search on
	private JCheckBox      chkUseRange; // use range specifier
	private JTextField     tfRange0;    // if url contains a '%' these
	private JTextField     tfRange1;    //  specify range of #'s to plug in
	private JTextField     tfMaxDepth;  // how many levels of links to follow
	private JButton        btnClear;    // to launch or cancel the crawl
	private JButton        btnStart;    // to launch or cancel the crawl
	private JProgressBar   progressBar; // progress of this crawl
	private ContactCrawler m_crawler;   // controls the background crawl
	private SpiderOptions  m_options;   // the options for this crawl
	private JTextArea      taExcludes;  // skip any urls containing these strings

	public TabInputs(SpiderOptions options)
		{
		super(new BorderLayout());

		m_options = options;
		setBorder(new EmptyBorder(10, 10, 10, 10));
		add(searchPanel(), BorderLayout.NORTH);
		add(exludesPanel(), BorderLayout.CENTER);
		add( btnPanel(), BorderLayout.SOUTH);
		onSelectHost(0);
		}

	private void log ( String msg ) { SBLog.write( "##", msg ); }

	private JPanel searchPanel()
		{
		JPanel p = new JPanel(new LabeledPairLayout());
		cbHost = new JComboBox();
		for ( String[] pair : Hosts )
			cbHost.addItem( pair[0] );
		cbHost.addActionListener(this);

		addLabeledComponent(p, "Host:", cbHost);
		addLabeledComponent(p, "Root URL(s): ", tfPattern  = new JTextField());
		// addLabeledComponent(p, "Range: ", rangePanel());
		p.add(chkUseRange = new JCheckBox("Range: ", false), LabeledPairLayout.LABEL);
		p.add(rangePanel(), LabeledPairLayout.FIELD);
	onSelectUseRange();
		chkUseRange.addActionListener(this);
		return p;
		}

	private JPanel rangePanel()
		{
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		p.add(tfRange0 = makeNumField());
		p.add(new JLabel("-"));
		p.add(tfRange1 = makeNumField());
		add(Box.createHorizontalGlue());
		p.add(new JLabel(" Max Depth"));
		p.add(tfMaxDepth = makeNumField());
		if ( m_options.maxDepth > -1 )
			tfMaxDepth.setText("" + m_options.maxDepth );
// tfRange0.setText(RANGE0);tfRange1.setText(RANGE1);
		return p;
		}

	private JTextField makeNumField()
		{
		JTextField it = new JTextField(7);
		it.setHorizontalAlignment(JTextField.RIGHT);
		return it;
		}

	private void addLabeledComponent( JPanel p, String label, JComponent c )
		{
		p.add(new JLabel(label, JLabel.RIGHT), LabeledPairLayout.LABEL);
		p.add(c, LabeledPairLayout.FIELD);
		}

	private JComponent exludesPanel()
		{
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder( new TitledBorder("Exclude URLs containing...") );
		taExcludes = new JTextArea();
		taExcludes.setLineWrap(false);
		taExcludes.setTabSize(3);
		p.add( new JScrollPane(taExcludes), BorderLayout.CENTER);
		return p;
		}

	private JPanel btnPanel()
		{
		JPanel p = new JPanel();

		p.add(btnClear = makeButton("Clear"));
		p.add(btnStart = makeButton("Start"));
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);

		// Call setStringPainted now so that the progress bar height
		// stays the same whether or not the string is shown.
		progressBar.setStringPainted(true);
		p.add(progressBar);
		return p;
		}

	private JButton makeButton(String caption)
		{
		JButton b = new JButton(caption);
		b.addActionListener(this);
		return b;
		}

	public void actionPerformed(ActionEvent e)
		{
		Object source = e.getSource();
		if ( source.equals(btnStart))
			{
			if (btnStart.getText().equals(START)) onStart();
			else onStop();
			}
		else if ( source.equals(cbHost))
			{
			onSelectHost( cbHost.getSelectedIndex());
			}
		else if ( source.equals( chkUseRange ))
			onSelectUseRange();
		else if ( source.equals( btnClear ))
			tfPattern.setText("");
		}

	private void onSelectUseRange()
		{
		boolean useRange = chkUseRange.isSelected();
		tfRange0.setEnabled(useRange);
		tfRange1.setEnabled(useRange);
		}

	private void onSelectHost( int hostIndex )
		{
		SettingsFile.thaw(SETTINGS_PATH + Hosts[hostIndex][0] + SETTINGS_EXT, taExcludes );
		tfPattern.setText( Hosts[hostIndex][1] );
		}

	private boolean onStart()
		{
		int maxDepth;
		int hostIndex = cbHost.getSelectedIndex();
		String search = tfPattern.getText(); // = Hosts[hostIndex][1];
		log ( "Searching " + search );
// (new GetPage()).viewSource( search, taExcludes );
		SettingsFile.freeze( SETTINGS_PATH + Hosts[hostIndex][0] + SETTINGS_EXT, taExcludes );
		try
			{
			maxDepth = Integer.parseInt( tfMaxDepth.getText());
			if ( maxDepth < 0 )
				return SBDialog.inputError( "Max Depth must be non-negative" );
 			}
		catch (Exception e) { return SBDialog.inputError( "Max Depth must be numeric");  }
		List<String> urls = new Vector<String>();
		if ( chkUseRange.isSelected() && (search.indexOf("%") >= 0))
			{
			if ( !getRange(search, urls))
				return false;
			}
		else if ( search.startsWith("file:/")
		     &&   !(new java.io.File(search.substring(6)).exists()))
			{
			return SBDialog.inputError( "File not found on local machine:\n"
			                              + search.substring(6) );
			}
		else urls.add( search );
		m_options.maxDepth = maxDepth;
		if ( Hosts[hostIndex][0].equals(NZ_WPAGE_PROMPT))
			m_options.parserClass = ParseNZWhitePages.class;
		else if ( Hosts[hostIndex][0].equals(FINDA_PROMPT))
			m_options.parserClass = ParseFinda.class;
		else
			m_options.parserClass = ParseAlibaba.class;

		String excludeText = taExcludes.getText().trim();
		String[] excludes = excludeText.isEmpty()? null : excludeText.split("\n");
for ( String url : urls ) System.out.println(url);
		m_crawler = new ContactCrawler( urls, excludes, m_options );
		m_crawler.addPropertyChangeListener( this );
		m_crawler.execute();
		return true;
		}

	private boolean getRange(String pattern, List<String> urls )
		{
		String r0 = tfRange0.getText().trim().toLowerCase();
		String r1 = tfRange1.getText().trim().toLowerCase();
		try
			{
			switch ( pattern.charAt(pattern.indexOf("%") + 1))
				{
				default:	return SBDialog.inputError( "Invalid URL pattern" );
				case '0':
				case 'd':	int i = Integer.parseInt(r0);
								int max = Integer.parseInt(r1);
								if ((i < 0) || (max < i))
									return SBDialog.inputError( "Invalid numeric range" );
								while ( i <= max )
									urls.add( String.format( pattern, i++ ));
								return true;

				case 's':	if ( r0.compareTo(r1) > 0 )
									return SBDialog.inputError( "Invalid alpha range" );
								if ( r0.length() != r1.length())
									return SBDialog.inputError( "alpha range char width must match" );

								LetterMath x = new LetterMath(r0);
								while ( x.compareTo(r1) <= 0 )
									{
									urls.add( String.format( pattern, x ));
									x.increment();
									}
								return true;
				}
			}
		catch ( Exception e ) { return SBDialog.inputError( "Error parsing range:\n" + e); }
//		return true;
		}

	private void onStop()
		{
		if ( m_crawler != null )
			m_crawler.cancel();
		m_crawler = null;
		}

	public void propertyChange(PropertyChangeEvent evt)
		{
		String property = evt.getPropertyName();
		if (property.equals("progress"))
			{
			int progress = (Integer) evt.getNewValue();
			progressBar.setIndeterminate(false);
			progressBar.setValue(progress);
			// progressBar.setValue((Integer)evt.getNewValue());
			}
		else if (property.equals("state"))
			{
			String state = evt.getNewValue().toString();
			log( state );
		/*************
			if ( state.equals("DONE"))
				{
				java.awt.Toolkit.getDefaultToolkit().beep();
				}
		*************/
			if ( state.equals("STARTED"))
				{
				progressBar.setValue(0);
				progressBar.setIndeterminate(true);
				btnStart.setText(CANCEL);
				}
			else if ( state.equals("FINISHED"))
				{
				java.awt.Toolkit.getDefaultToolkit().beep();
				btnStart.setText(START);
				}
			}
		}
	}

class LetterMath
	{
	byte[] theBytes;

	public LetterMath( String abc )
		{
		theBytes = abc.toLowerCase().getBytes();
		}

	String increment()
		{
//		if ( s.isEmpty()) return "a";
		int x = theBytes.length;
		while ( x-- > 0 )
			if ( theBytes[x] == (byte)'z' )
				theBytes[x] = (byte)'a';
			else
				{
				++theBytes[x];
				break;
				}
		return new String( theBytes );
		}

	public String toString()
		{
		return new String( theBytes );
		}

	public int compareTo( String s )
		{
		return toString().compareTo(s);
		}
	public boolean lessThan( String s )
		{
		return compareTo(s) < 0;
		}
	}
