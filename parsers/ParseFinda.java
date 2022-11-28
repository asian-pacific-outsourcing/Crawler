package com.shanebow.tools.Crawler.parsers;
/****************************************************
* @(#)ParseFinda.java	1.00 10/05/07
* Copyright (c) 2010 Richard T. Salamone, Jr.  All rights reserved.
*
* Parse New Zealand White Pages
*
* This class parses contact info from an HTML search results
* page on http://finda.co.nz
*
* Company starts @<h2 class="fn org">
*					then href to web site enclosing company name
*		<div class="show" name="phone_number">(027) 4752445</div>
*
* @author Rick Salamone
* @version 1.0 05/07/10
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

public class ParseFinda extends ContactParser
	{
	public static int _maxDesc = 100;
	String m_csv = null;

	public ParseFinda(WorkRecord work, SpiderInputStream is, Spider spider)
		throws WorkException
		{
		super( work, is, spider );
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
		String company = "";
		String webSite = "";
		String phone = "";
		String desc = "";
		String address = "";
		if ( !advanceTo( "a", 1 ))
			return null;
		HTMLTag tag = getTag();
		webSite = tag.getAttributeValue("href");
		ch = appendTo (buffer);
		company = buffer.toString();
/***************/
		boolean inAddress = false;
		while ( ch != -1 )
			{
			if ( ch == 0 ) // got a tag
				{
				tag = getTag();
				String tagName = tag.getName().toLowerCase();
				if (tagName.equals("/li"))
					break;
				else if ( inAddress )
					{
					buffer.setLength(0);
					ch = appendTo (buffer);
					address = buffer.toString().trim();
					inAddress = false;
					continue;
					}
				else if ( !tagName.equals("div"))
					{
					ch = read();
					continue;
					}
				String divClass = tag.getAttributeValue("class");
				String divName = tag.getAttributeValue("name");
				buffer.setLength(0);
				ch = appendTo (buffer);
				if ( divClass.equals("adr"))
					{
					address = buffer.toString().trim();
					if ( address.isEmpty())
						inAddress = true;
					}
				else if ( divClass.equals("note"))
					{
					desc = buffer.toString().trim();
					if ( desc.length() > _maxDesc)
						desc = desc.substring(0,_maxDesc);
					}
				else if ( divClass.equals("show"))
					{
					if ( divName.equals("phone_number")) phone = buffer.toString().trim();
					}
				}
			else ch = read();
			}
/***************/
		return company + "," + phone + "," + desc + "," + webSite + "," + address;
		}

	public boolean findContact()
		{
		m_csv = null;
		try
			{
			while ( advanceTo( "h2", 1 ))
				{
				HTMLTag tag = getTag();
				String attributeValue = tag.getAttributeValue("class");
				if (( attributeValue == null ) || !attributeValue.equals("fn org"))
					continue;
				m_csv = grabContactInfo();
				break;
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
		ParseFinda parser = new ParseFinda( url, is, null );
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