package com.shanebow.tools.Crawler;
/*****************************************************************
*
* @(#)TabSettings.java	1.0 10/04/13
* 
* Copyright (c) 2010 Rick Salamone, All Rights Reserved.
* 
* This dialog allows users to choose crawler preferences.
* It is essentially just a UI for the SpiderOptions object.
*
* @author Rick Salamone
* @version 1.00 04/13/10
*
**************************/
import com.shanebow.tools.Crawler.parsers.*;

import com.shanebow.spider.*;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.layout.LabeledPairLayout;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.border.*;

public class TabSettings extends JPanel
	implements ActionListener
	{
	private JButton btnApply;
	private JButton btnCancel;
	private SpiderOptions m_options;

	// Spider Panel Fields
	private JTextField tfTimeout;
	private JTextField tfUserAgent;
	private JTextField tfCorePoolSize;
	private JTextField tfMaxPoolSize;
	private JTextField tfKeepAliveTime;

	// Workload Panel Fields
	private static final String WORK_RAM = "Memory Based";
	private static final String WORK_SQL = "Database";
	private static final String[] WORK_MANAGERS = { WORK_RAM, WORK_SQL };
	JComboBox cbWorkManager = new JComboBox( WORK_MANAGERS );
	public  static final String URL_SPIDERWORK = "jdbc:odbc:APO";
	private JTextField tfdbURL;
	private JTextField tfdbUID;
	private JTextField tfdbPWD;
	private JTextField tfdbClass;

	// Debugging Settings
	private JTextField tfaddMax;    // maximum # of URLs to search

	// Parser Settings
	private static final String LBL_MAX_DESC = "Max Desc";
	private JTextField tfMaxDesc;   // max desc width

	public TabSettings( SpiderOptions options )
		{
		super(new BorderLayout());
		m_options = options;
		setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel buttonPanel = new JPanel( new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add( btnCancel = makeButton("Cancel"));
		buttonPanel.add( btnApply = makeButton("Apply"));
//		getRootPane().setDefaultButton(btnApply);

		add(new JScrollPane(mainPanel()), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		initFields();
		}

	private JPanel mainPanel()
		{
		JPanel p = new JPanel( new LabeledPairLayout());
		p.add(new JLabel("Workload", JLabel.RIGHT), "label");
		p.add(cbWorkManager, "field");
		cbWorkManager.addActionListener(this);

		addSectionHead(p, "Web Connections" );
		tfTimeout       = addLabeledTextField(p, "Timeout");
		tfUserAgent     = addLabeledTextField(p, "User Agent");
		tfaddMax        = addLabeledTextField(p, "Max URLs");

		addSectionHead(p, "Background Tasks" );
		tfCorePoolSize  = addLabeledTextField(p, "Core Pool");
		tfMaxPoolSize   = addLabeledTextField(p, "Max Pool");
		tfKeepAliveTime = addLabeledTextField(p, "Keep Alive");

		addSectionHead(p, "Database Settings" );
		tfdbURL   = addLabeledTextField(p, "URL");
		tfdbClass = addLabeledTextField(p, "Driver");
		tfdbUID   = addLabeledTextField(p, "User ID");
		tfdbPWD   = addLabeledTextField(p, "Password");

		addSectionHead(p, "Parser Settings" );
		tfMaxDesc = addLabeledTextField(p, LBL_MAX_DESC );
		return p;
		}

	private void addSectionHead( JPanel p, String heading )
		{
		p.add(new JLabel("", JLabel.RIGHT), "label");
		p.add(new JLabel(heading + ":", JLabel.LEFT), "field");
		}

	private JTextField addLabeledTextField( JPanel p, String label )
		{
		JTextField it = new JTextField(35);
		p.add(new JLabel(label + ":", JLabel.RIGHT), "label");
		p.add(it, "field");
		return it;
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
		if ( source.equals(btnApply))
			onApply();
		else if ( source.equals(btnCancel))
			onCancel();
		else if ( source.equals(cbWorkManager))
			onWorkManager();
		}

	private void onWorkManager()
		{
		boolean useDB = cbWorkManager.getSelectedItem().equals(WORK_SQL);
		tfdbClass.setEditable(useDB);
		tfdbURL.setEditable(useDB);
		tfdbUID.setEditable(useDB);
		tfdbPWD.setEditable(useDB);
		}

	public void onCancel()
		{
		initFields();
		}

	public boolean onApply()
		{
		try
			{
			String descMax = tfMaxDesc.getText().trim();
			int maxDesc = Integer.parseInt(descMax);
			if ( maxDesc < 1 )
				return SBDialog.inputError( LBL_MAX_DESC + " must be positive" );

  			int timeout = Integer.parseInt( tfTimeout.getText());
			int corePoolSize = Integer.parseInt( tfCorePoolSize.getText());
			int maxPoolSize = Integer.parseInt( tfMaxPoolSize.getText());
			long keepAliveTime = Long.parseLong(tfKeepAliveTime.getText());

			m_options.timeout = timeout;
			String addMax = tfaddMax.getText();
			ContactCrawler.m_addMax = (addMax.isEmpty()? -1 : Integer.parseInt(addMax));

			m_options.corePoolSize = corePoolSize;
			m_options.maximumPoolSize = maxPoolSize;
			m_options.keepAliveTime = keepAliveTime;
			ParseFinda._maxDesc = maxDesc;
			}
		catch ( Exception e )
			{
			return SBDialog.inputError( "Number format error" );
			}
		m_options.userAgent = getText(tfUserAgent);
		boolean useDB = cbWorkManager.getSelectedItem().equals(WORK_SQL);
		m_options.workload = (useDB? SpiderOptions.WORKLOAD_SQL
		                                  : SpiderOptions.WORKLOAD_RAM); 
		m_options.dbURL = getText(tfdbURL);
		m_options.dbUID = getText(tfdbUID);
		m_options.dbPWD = getText(tfdbPWD);
		m_options.dbClass = getText(tfdbClass);
		return true;
		}

	private String getText( JTextField tf )
		{
		String it = tf.getText().trim();
		if ( it.isEmpty()) it = null;
		return it;
		}

	private void initFields()
		{
		// Spider Panel Fields
		tfTimeout.setText( "" + m_options.timeout );
		tfUserAgent.setText( m_options.userAgent );
		tfCorePoolSize.setText( "" + m_options.corePoolSize );
		tfMaxPoolSize.setText( "" + m_options.maximumPoolSize );
		tfKeepAliveTime.setText( "" + m_options.keepAliveTime );

		// Workload Panel Fields
		boolean useDB = m_options.workload.equals(SpiderOptions.WORKLOAD_SQL);
		cbWorkManager.setSelectedItem(useDB? WORK_SQL : WORK_RAM );
		tfdbURL.setText( m_options.dbURL );
		tfdbUID.setText( m_options.dbUID );
		tfdbPWD.setText( m_options.dbPWD );
		tfdbClass.setText( m_options.dbClass );

		// Parser Settings
		tfMaxDesc.setText("" + ParseFinda._maxDesc );

		// Debug Panel Fields
		if ( ContactCrawler.m_addMax >= 0 )
			tfaddMax.setText("" + ContactCrawler.m_addMax );
		onWorkManager();
		}
	}
