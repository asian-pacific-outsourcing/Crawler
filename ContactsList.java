package com.shanebow.tools.Crawler;
/*
* @(#)ContactsList.java 1.00 04/13/10
* Copyright 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* Lists the contacts and potential contacts found by the crawler
*
* @version 1.0 04/17/10
* @author Rick Salamone
*
* 20100510 RTS Changed to have time stamps off by default 
*/
import com.shanebow.ui.SBTextPanel;

public class ContactsList
	extends SBTextPanel
	{
	public ContactsList() { this("Contacts Found"); }
	public ContactsList(String title)
		{
		super( title, false, java.awt.Color.CYAN );
		setTimeStamp(false);
		}

	public void add(String csv)
		{
		super.add(csv);
		}
	}
