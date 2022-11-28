package com.shanebow.tools.Crawler.parsers;
/****************************************************
* @(#)ParseNZWhitePages.java	1.00 05/11/10
* Copyright (c) 2010 Richard T. Salamone, Jr.  All rights reserved.
*
* Parse New Zealand White Pages
*
* This class parses contact info from an HTML search results
* page on http://yellow.co.nz/.
*
* @author Rick Salamone
* @version 1.0 05/11/10
*
************/
import com.shanebow.util.SBLog;
import java.io.*;
import java.net.*;
import java.util.*;
import com.shanebow.web.html.*;
import com.shanebow.spider.*;
import com.shanebow.spider.work.*;
import com.shanebow.tools.Crawler.ContactParser;

public class ParseNZWhitePages extends ContactParser
	{
	public static final String CONTACT_TBODY_CLASS = "resultBody";
	public static int _maxDesc = 100;
	String m_csv = null;

	public ParseNZWhitePages(WorkRecord work, SpiderInputStream is, Spider spider)
		throws WorkException
		{
		super( work, is, spider );
		}

	public String nextLink()
		{
		try
			{
			int ch;
			while ((ch = read()) != -1)
				{
				if ( ch == 0 )
					{
					HTMLTag tag = getTag();
					String tagName = tag.getName().toLowerCase();
					if (tagName.equals("/thead"))
						return null;
					else if (tagName.equals("a"))
						{
						String attrValue = tag.getAttributeValue("class");
						if ((attrValue != null) && attrValue.equals("nextLink"))
							return tag.getAttributeValue("href");
						}
					}
				}
			}
		catch ( Exception e ) {} // oh well, guess we don't get a next page
		return null;
		}

	private int appendTo(StringBuilder s)
		throws IOException
		{
		int ch;
		while ((ch = read()) > 0)
			if (((char)ch != ',' ) && ((char)ch != '\r') && ((char)ch != '\n'))
				s.append((char)ch);
		return ch;
		}

	private String grabContactInfo()
		throws IOException
		{
		int ch;
		StringBuilder buffer = new StringBuilder();
		String type = "";
		String name = "";
		String company = "";
		String webSite = "";
		String phone = "";
		String desc = "";
		String address = "";

		HTMLTag tag = getTag();
		String attrValue = tag.getAttributeValue("class");
		if ( !attrValue.startsWith(CONTACT_TBODY_CLASS))
			return null;
		attrValue = tag.getAttributeValue("id");
		type = ( attrValue.endsWith("_RES") ? "R"
		     :   attrValue.endsWith("_BUS") ? "B" : "?" );

		if ( !advanceTo( "a", 1 ))
			return null;
		tag = getTag();
		webSite = tag.getAttributeValue("href");
		ch = appendTo (buffer);
		if ( type.equals("R"))
			name = buffer.toString();
		else
			company = buffer.toString();
/***************/
		boolean inAddress = false;
		while ( ch != -1 )
			{
			if ( ch == 0 ) // got a tag
				{
				tag = getTag();
				String tagName = tag.getName().toLowerCase();
				if (tagName.equals("/tbody"))
					break;
				else if ( tagName.equals("dd"))
					{
					attrValue = tag.getAttributeValue("class");
					if ( !attrValue.equals("bizAddr"))
						{
						ch = read();
						continue;
						}
					buffer.setLength(0);
					ch = appendTo (buffer);
					address = buffer.toString().trim();
					}
				else if ( tagName.equals("span"))
					{
					attrValue = tag.getAttributeValue("class");
					if ( attrValue.indexOf("phoneStatic") < 0 )
						{
						ch = read();
						continue;
						}
					buffer.setLength(0);
					ch = appendTo (buffer);
					phone = buffer.toString().trim();
					}
				else ch = read();
				}
			else ch = read();
			}
/***************/
		return type + "," + name + "," + company + "," + phone + "," + desc + "," + webSite + "," + address;
		}

	public boolean findContact()
		{
		m_csv = null;
		try
			{
			while ( advanceTo( "tbody", 1 ))
				{
				m_csv = grabContactInfo();
				if ( m_csv != null )
					return true;
				}
			}
		catch (Exception e) {}
		return false;
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
		ParseNZWhitePages parser = new ParseNZWhitePages( url, is, null );
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