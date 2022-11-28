package com.shanebow.tools.Crawler;
/*
 * SettingsFile.java
 *
 */
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.SBFileSaver;
import com.shanebow.util.SBArray;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBLog;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import javax.swing.JTextArea;

public class SettingsFile
	{
	private static final String MODULE = "SettingsFile";
	private static final String SEPARATOR = ",";

	public static void thaw ( String FileName, JTextArea excludes )
		{
		excludes.setText("");
		BufferedReader stream = null;
		try { stream = new BufferedReader(new FileReader(FileName)); }
		catch (Exception e)
			{
			return;
			}
		String text;
		while ((text = getLine(stream)) != null )
			{
			text = text.trim();
			if ( text.isEmpty())
				continue;
			excludes.append( text + "\n" );
			}
		try { stream.close(); }
		catch (Exception e) {}
		}

	private static String getLine(BufferedReader stream)
		{
		try { return stream.readLine(); }
		catch (Exception e)
			{
			SBDialog.fatalError( MODULE + "> read error in settings file\n" + e.toString());
			}
		return null;
		}

	static void freeze(String filespec, JTextArea excludes )
		{
	Thread saver = new SBFileSaver(filespec, excludes.getDocument());
	saver.start();
	/***************
		try
			{
			PrintWriter file = new PrintWriter ( filespec );
			for ( PositionLeg leg : LegsList.allLegs )
				{
				Strategy strat = leg.getStrategy();
				if ( savedStrategies.indexOf(strat) > -1 ) // already saved
					continue;
				file.println ( "* " + strat.getTabName()
											+ SEPARATOR + strat.getClassName());
				}
			file.close();
			}
		catch (IOException e)
			{
			SBDialog.error( "Error Saving Positions", filespec + " Error: " + e.toString());
			return;
			}
		***************/
		SBLog.format( "%s(%s): Updated", MODULE, filespec );
		}
	}
