package com.shanebow.tools.Crawler;
/********************************************************************
* @(#)_SukhumwitCrawler.java	1.00 04/13/10
* Copyright 2010-2011 by Richard T. Salamone, Jr. All rights reserved.
*
* _SukhumwitCrawler: This is the main container frame for the app
*
* @author Rick Salamone
* @version 1.00 04/13/10
* 20100510 rts Removed tree of spidered pages (not viable)
* 20100510 rts Added output tab TabFailedURLList along with file
*              menu option to clear the list
* 20100615 rts removed new contact menu item
* 20100627 rts extended licence
* 20110113 rts using SBMenuBar & SBProperties
* 20110127 rts using SBMenuBar & SBProperties
* 20110616 rts remembers split locations
*******************************************************/
import com.shanebow.spider.SpiderOptions;
import com.shanebow.dao.DBStatement;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.menu.*;
import com.shanebow.ui.SBTextPanel;
import com.shanebow.ui.SplitPane;
import com.shanebow.ui.LAF;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class _SukhumwitCrawler
	extends JFrame
	implements ActionListener
	{
	private static long blowUp = 0; // SBDate.toTime("20110131  23:59");

	public static final String CMD_CONTACTS_SAVEAS="Save Contacts As...";
	public static final String CMD_FAILURES_SAVEAS="Save Failures As...";

	private final TabFailedURLList tabFailures = new TabFailedURLList();
	private final ContactsList  m_contactsList = new ContactsList();

	public _SukhumwitCrawler()
		{
		super();
		SBProperties props = SBProperties.getInstance();
		setTitle(props.getProperty("app.name")
		        + " " + props.getProperty("app.version"));
		setBounds(props.getRectangle("usr.app.bounds", 25,25,975,680));
		DBStatement.connect( "", "" );
		buildContent();
		buildMenus();
		setVisible(true);
		}

	protected void buildContent()
		{
		getContentPane().add( mainPanel(), BorderLayout.CENTER );
//		getContentPane().add( btnPanel(), BorderLayout.SOUTH );
		}

	private JComponent mainPanel()
		{
		SplitPane upper = new SplitPane ( SplitPane.HSPLIT, 	ulTabs(), urTabs());

		upper.setDividerLocation("usr.top.split", 450);
		SplitPane mainPane = new SplitPane ( SplitPane.VSPLIT, upper, m_contactsList );
		mainPane.setDividerLocation("usr.split", 375);

		ContactCrawler._hitList = m_contactsList;
		ContactCrawler._failList = tabFailures;
		return mainPane;
		}

	private JTabbedPane ulTabs() // upper left tabs
		{
		SpiderOptions options = new SpiderOptions();
		options.workload = SpiderOptions.WORKLOAD_RAM;
		options.maxDepth = 0;

		ImageIcon icon = new ImageIcon("image/byellow.gif");
		final JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Run",      icon, new TabInputs(options));
		tabbedPane.addTab("Settings", icon, new TabSettings(options));
		return tabbedPane;
		}

	private JTabbedPane urTabs() // upper right tabs
		{
		ImageIcon icon = new ImageIcon("image/byellow.gif");
		final JTabbedPane tabbedPane = new JTabbedPane();
		SBTextPanel log = (SBTextPanel)SBLog.getLog();
		tabbedPane.addTab("Log",      icon, log,  "Execution Trace" );
		tabbedPane.addTab("Failures", icon, tabFailures, "List of Failed URLs" );
		return tabbedPane;
		}

	protected void buildMenus()
		{
		SBMenuBar menuBar = new SBMenuBar();
		JFrame f = this;

		menuBar.addMenu(this, "File",
			CMD_CONTACTS_SAVEAS,
			CMD_FAILURES_SAVEAS,
			null,
		//	new SBViewLogAction(f), null,
			LAF.setExitAction(new com.shanebow.ui.SBExitAction(f)
				{
				public void doApplicationCleanup()
					{
				//	m_connection.disconnect();
					DBStatement.disconnect();
					}
				}));
		menuBar.add(menuBar.getThemeMenu());
		menuBar.addMenu("Help",
			new SBActHelp(), null,
			new SBAboutAction(f, SBProperties.getInstance()));
		setJMenuBar(menuBar);
		}

	public void actionPerformed(ActionEvent e)
		{
		String cmd = e.getActionCommand();
		if ( cmd.equals(CMD_CONTACTS_SAVEAS))      m_contactsList.save();
		else if ( cmd.equals(CMD_FAILURES_SAVEAS)) tabFailures.save();
		}

	public static void main( String[] args )
		{
		SBProperties.load(_SukhumwitCrawler.class );
		LAF.initLAF(blowUp, true);
		JFrame frame = new _SukhumwitCrawler();
		}
	} // 206
