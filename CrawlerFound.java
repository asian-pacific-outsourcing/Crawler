package com.shanebow.tools.Crawler;

import java.net.URL;

public class CrawlerFound
	{
	public enum What { HIT, LINK, ERROR }

	What   m_what;
	URL    m_url;
	String m_data;

	public CrawlerFound( URL url, What what)
		{
		m_url = url;
		m_what = what;
		}

	public CrawlerFound( URL url, String hit )
		{
		m_url = url;
		m_what = What.HIT;
		m_data = hit;
		}

	public boolean isLink()  { return m_what == What.LINK; }
	public boolean isHit()   { return m_what == What.HIT; }
	public boolean isError() { return m_what == What.ERROR; }

	public String toString()
		{
		return m_url.toString();
		}
	}