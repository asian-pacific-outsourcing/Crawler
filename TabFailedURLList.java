package com.shanebow.tools.Crawler;
/*
* @(#)AppFrame.java	1.00 05/10/10
* Copyright 2010 by Richard T. Salamone, Jr. All rights reserved.
*
* This is the main container frame for the app
*
* @version 1.00 05/10/10
* @author Rick Salamone
*
* 20100510 RTS Removed tree of spidered pages (not viable)
* 20100510 RTS Added output tab TabFailedURLList along with file
*              menu option to clear the list
*/
import com.shanebow.ui.SBTextPanel;

public class TabFailedURLList
	extends SBTextPanel
	{
	public TabFailedURLList()
		{
		super( "Failed URLs", false, java.awt.Color.RED );
		setTimeStamp(false);
		}
	}
