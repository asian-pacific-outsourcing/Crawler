package com.shanebow.tools.Crawler.parsers;

import com.shanebow.util.SBLog;
import java.io.*;
import java.net.*;
import java.util.*;
import com.shanebow.web.html.*;
import com.shanebow.spider.*;
import com.shanebow.spider.work.*;
import com.shanebow.tools.Crawler.ContactParser;

/**
 * Parse Alibaba
 *
 * This class parses contact info from an alibaba.com HTML page.
 *
 * Copyright 2010 by Rick Salamone(rtsalamone@yahoo.com)
 *
 * @author Rick Salamone
 * @version 1.0
 */
public class ParseAlibaba extends ContactParser
	{
	private static final String TITLE_PREFIX = "Contact Information for ";
	private static final String KEYWORDS_PREFIX = "Company Contact Information, ";
	String m_csv = null;

	public ParseAlibaba(WorkRecord work, SpiderInputStream is, Spider spider)
		throws WorkException
		{
		super( work, is, spider );
		}

	private String getTitleContactCo()
		throws IOException
		{
		if ( !advanceTo( "title", 1 ))
			return null;
		for ( int i = 0; i < TITLE_PREFIX.length(); i++ )
			if ( (char)(read()) != TITLE_PREFIX.charAt(i))
				return null;
		StringBuilder result = new StringBuilder();
		int ch;
		while ((ch = read()) != -1)
			{
			if ( ch != 0 )
				result.append((char)ch);
			else if ( getTag().getName().equalsIgnoreCase("/title"))
				return result.toString();
			}
		return null;
		}

	public boolean findContact()
		{
		m_csv = null;
		boolean foundMeta = false;
		try
			{
			String companyName = getTitleContactCo();
			if ( companyName == null )
				return false;
// System.out.println( "company: " + companyName );
			while ( advanceTo( "meta", 1 ))
				{
				HTMLTag tag = getTag();
				String attributeValue = tag.getAttributeValue("name");
				if (( attributeValue == null ) || !attributeValue.equals("keywords"))
					continue;
				attributeValue = tag.getAttributeValue("content");
				if ( attributeValue.startsWith(KEYWORDS_PREFIX))
					{
					m_csv = attributeValue.substring(KEYWORDS_PREFIX.length());
					foundMeta = true;
					break;
					}
				}
			}
		catch (Exception e) {}
		return (m_csv != null);
		}

	public String getCSV() { return m_csv; }

	/**
	* This method is called once for each table row located, it 
	* contains a list of all columns in that row.  The method provided
	* simply prints the columns to the console.
	* @param list Columns that were found on this row.
	*/
	private void processTableRow(List<String> list)
		{
		StringBuilder result = new StringBuilder();
		for (String item : list)
			{
			if (result.length() > 0)
				result.append(",");
			result.append('\"');
			result.append(item);
			result.append('\"');
			}
		System.out.println(result.toString());
		}

  /**
   * Called to parse a table.  The table number at the specified URL
   * will be parsed.
   * @param url The URL of the HTML page that contains the table.
   * @param tableNum The table number to parse, zero for the first.
   * @throws IOException Thrown if an error occurs while reading.
   */
	public void parseTable(int tableNum) throws IOException
		{
		StringBuilder buffer = new StringBuilder();
		List<String> list = new ArrayList<String>();
		boolean capture = false;
		
		if ( !advanceTo( "table", tableNum ))
			return;

		int ch;
		while ((ch = read()) != -1)
			{
			if (ch == 0)
				{
				HTMLTag tag = getTag();
				if (tag.getName().equalsIgnoreCase("tr"))
					{
					list.clear();
					capture = false;
					buffer.setLength(0);
					}
				else if (tag.getName().equalsIgnoreCase("/tr"))
					{
					if (list.size() > 0)
						{
						processTableRow(list);
						list.clear();
						}
					}
				else if (tag.getName().equalsIgnoreCase("td"))
					{
					if (buffer.length() > 0)
						list.add(buffer.toString());
					buffer.setLength(0);
					capture = true;
					}
				else if (tag.getName().equalsIgnoreCase("/td"))
					{
					list.add(buffer.toString());
					buffer.setLength(0);
					capture = false;
					}
				else if (tag.getName().equalsIgnoreCase("/table"))
					{
					break;
					}
				}
			else
				{
				if (capture)
					buffer.append((char) ch);
				}
			}
		}

  /**
   * The main method, create a new instance of the object and call
   * process.
   * @param args not used.
   */  
/***************
	public static void main(String args[])
		{
		String search =
			"file:/C:/apps/src/com/shanebow/tools/Crawler/resources/HelpFiles/contactinfo1.html";

		try
			{
			URL url = new URL( search );
		InputStream is = url.openStream();
		ParseAlibaba parser = new ParseAlibaba( url, is, null );
			if ( parser.findContact())
				System.out.println( "SUCCESS!!\n csv: " + parser.getCSV());
			}
		catch (Exception e)
			{
			e.printStackTrace();
			}
		}
***************/
	}