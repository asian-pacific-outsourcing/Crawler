package com.shanebow.tools.Crawler;
/**
* @(#)ContactParser.java	1.00 05/11/10
* Copyright (c) 2010 Richard T. Salamone, Jr.  All rights reserved.
* 
* Contact Parser Interface
* 
* ContactParser: This interface defines a class that the
* spider call to search a page for contact information.
*
* @author Rick Salamone
* @version 1.0
*
* 20100408 RTS created
* 20100511 RTS added nextLink method
* 20100521 RTS modified to work with _sbspider
*/
import com.shanebow.spider.*;
import com.shanebow.spider.work.*;
import java.net.*;

public abstract class ContactParser
	extends SpiderParseHTML
	{
	public ContactParser(WorkRecord work, SpiderInputStream is, Spider spider)
		throws WorkException
		{
		super( work, is, spider );
		}

	public String nextLink()
		{
		return null;
		}

  /**
	* Called to parse an HTML page for contact info
	* 
	* @param stream
	*          The parsable HTML stream.
	* @return true if contact info was found, false otherwise
	* @throws IOException
	*           Thrown if an IO error occurs while processing
	*           the page.
	*/
	abstract public boolean findContact();
//      throws IOException;

	/**
	* Get the contact information found during the most recent parse.
	*
	* @return A comma separated vector of contact info if the
	*         most recent call to findContact was successfull;
	*         null otherwise.
	*/
	abstract public String getCSV();
	}