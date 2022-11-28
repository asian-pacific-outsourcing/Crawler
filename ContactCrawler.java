package com.shanebow.tools.Crawler;

import com.shanebow.web.html.URLUtility;
import com.shanebow.spider.*;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.swing.SwingWorker;

public class ContactCrawler
	extends SwingWorker<Void, CrawlerFound>
	implements SpiderHook
	{
	static final String MODULE = "~"; // "ContactCrawler";

	// Debugging
	static final boolean DEBUG = false;
	static int m_addMax = -1; //1000; // for debugging, max #URLs to add, -1 disables

	// Inputs/Configuration: What to search & how
	private String[]      m_excludes; // strings which if found cause URL to be skipped
	String[] excludedHosts =  // exclude any hosts which contain these strings
		{                       // for instance yahoo.rick.en would be skipped
		"yahoo",
		"google",
		};

	// Outputs: Progress & Results
	public static ContactsList     _hitList = null;  // output of contacts found
	public static TabFailedURLList _failList = null; // output of failed URLs
	private int m_linksFound = 0;  // count links: includes html, nonHTML, excluded, etc
	private int m_linksDone = 0;   // count links successfully processed
	private int m_linksOther = 0;  // count nonHTML links
	private int m_linksAdded = 0;  // count links added to workload
	private int m_linksFailed = 0; // count links with failures
	private int m_numHits = 0;     // count contacts found
	private int m_numPotential = 0;// count, might contain a contact

	private void debug ( String format, Object... args )
		{
		if (DEBUG) SBLog.write ( MODULE + " DEBUG", String.format( format, args ));
		}
	private void log ( String format, Object... args )
		{
		SBLog.write ( MODULE, String.format( format, args ));
		}

Spider itsy;
	ContactCrawler( List<String> urls, String[] exclude, SpiderOptions options )
		{
		m_excludes = exclude;
		try // Create & Launch Spider
			{
 			itsy = new Spider( options, this ); // Construct the spider object
			for ( String url : urls ) // Add the root URLs for processing
				itsy.addURL( url );
			}
		catch ( Exception ex )
			{
			SBLog.error ( MODULE, ex.toString());
			}
		}

	@Override
	public Void doInBackground()
		{
		log("Thread Name: %s", Thread.currentThread().getName());

		try // Launch Spider
			{
	//		itsy.start();
			itsy.process();
			log ( itsy.getStatus());
			}
		catch ( Exception ex )
			{
			SBLog.error ( MODULE, ex.toString());
			}
		publish( (CrawlerFound)null); // causes progress bar to be updated with final count
		log ( "EXITING doInBackground(), Requests: %d / %d, Failures: %d Contacts: %d Manual Check: %d",
				m_linksDone, m_linksAdded, m_linksFailed, 	m_numHits, m_numPotential );
		firePropertyChange("state", "", "FINISHED" );
		return (Void)null;
		}

	@Override
	protected void process(List<CrawlerFound> chunks)
		{
		for ( CrawlerFound found : chunks )
			{
			if (found == null) continue;
			if ( found.isHit())
				{
				if ( _hitList != null )
					_hitList.add( found.toString() + "," + found.m_data);
				}
			else if ( found.isError())
				{
				if ( _failList != null )
					_failList.add( found.toString());
				}
			else // if ( found.isLink())
				{
				}
			}
		if ( m_linksAdded > 0 )
			setProgress( (m_linksDone + m_linksOther) * 100 / m_linksAdded );
		}

	public void cancel()
		{
log("CANCEL");
		itsy.cancel();
		cancel(true);
		}

	/****************************************************************************
	*
	* Implement SpiderHook
	*
	****************************************************************************/
	/**
	* Spider is starting up
	* @param spider - the spider that will be working with this object.
	*/
	public void initialized(Spider spider)
		{
		debug("init Spider");
		}

	/**
	* Spider encountered a URL: Should we parse it?
	* @param url    - URL that the spider found.
	* @param source - page that the URL was found on.
	* @param type   - type of link this URL is.
	* @return true if the spider should scan for links on this page.
	*/
	public boolean urlFound(URL url, URL source, URLType type)
		{
		++m_linksFound;
		debug( "%3d FOUND( %s, %s )", m_linksFound, url, source );
		if ((m_addMax >= 0) && (m_linksAdded >= m_addMax)) // for debugging
			return false;

		if ((type != URLType.HYPERLINK)
		||  isCancelled()
		||  isExcluded( url.toString().toLowerCase()))
			return false;
		return true;
		}

	private boolean isExcluded( String url )
		{
		if ( m_excludes == null ) return false;
		for ( String excluded : m_excludes )
			if ( url.indexOf(excluded) >= 0 )
				{
		//		log ( "%3d EXCLU( %s ): %s", m_linksFound, url, excluded );
				return true;
				}
		return false;
		}

	/**
	* Spider successfully added a URL to the workload.
	* @param url     The URL that the spider added.
	* @param source  The page that the URL was found on.
	* @param depth   The depth of this URL.
	*/
	public void urlAdded ( URL url, URL source )
		{
		++m_linksAdded;
		log( "%3d ADD (%s, %s)", m_linksAdded, url, source );
		publish ( new CrawlerFound(url, CrawlerFound.What.LINK));
		}

	/**
	* Process a NON-HTML URL: Ignored...
	* @param url     The URL that the spider found.
	* @param stream  An InputStream to read the page contents from.
	* @throws IOException
	*           Thrown if an IO error occurs while processing the page.
	*/
	public void urlProcess(URL url, InputStream is) throws IOException
		{
		debug( "Skip non-HTML: " + url.toString());
		++m_linksOther;
		}

	/**
	* Process an HTML URL: Parse for contact info - publish if found.
	* @param url    The URL that the spider is about to process.
	* @param parse  Allows you you to parse the HTML on this page.
	* @throws IOException if an IO error occurs while processing.
	*/
	public void urlProcess(URL url, SpiderParseHTML source)
		throws IOException
		{
	//	log( "%3d Parse(%s)", m_linksDone+1, url );
		ContactParser parser = (ContactParser)source;
		String nextPage = parser.nextLink();
		if (( nextPage != null ) && !isExcluded(nextPage))
			{
			URL nextURL = URLUtility.constructURL(url, nextPage,true);
/**************
log( "-------------------" );
log(nextURL.toString());
log( "-------------------" );
nextURL = URLUtility.constructURL(source.getBase(), nextPage,true);
log(nextURL.toString());
log( "-------------------" );
nextURL = URLUtility.constructURL(source.getBase(), nextPage,false);
log(nextURL.toString());
log( "-------------------" );
**************/
			try {itsy.addURL( nextURL, null ); log( "NO exception"); }
			catch ( Exception e )
				{
			log ( "error adding next URL: %s\n%s", nextURL, e.toString());
				publish( new CrawlerFound(nextURL, CrawlerFound.What.ERROR));
				}
			}

		if ( parser.findContact())
			{
			do
				{
				++	m_numHits;
				publish( new CrawlerFound ( url, parser.getCSV()));
				}
			while ( parser.findContact());
			}
		else if ( isLikelyContact( url.getPath()))
			{
			++	m_numPotential;
			publish( new CrawlerFound ( url, "Check manually"));
			}
		else source.readAll(); // causes links to be found on remainder of page
		++m_linksDone;
		log( "%3d DONE: %s", m_linksDone, url );
		}

	private boolean isLikelyContact( String path )
		{
		String lcPath = path.toLowerCase();
		int lastSlashAt = path.lastIndexOf("/");
		return (lastSlashAt > 2)
				&& (lcPath.substring(lastSlashAt+1).startsWith("contact")
		    || (lcPath.substring(0, lastSlashAt).indexOf("contact_information") >= 0 ));
		}

	/**
	* An error occured while processing a URL.
	*
	* @param url
	*          The URL that generated an error.
	*/
	public void urlError(URL url, String desc, ErrorLevel level)
		{
		++m_linksDone;
		++m_linksFailed;
		publish ( new CrawlerFound(url, CrawlerFound.What.ERROR));
		log( "%3d URL FAIL(%s): %s %s", m_linksDone, level, desc, url );
		}
	} // 284
