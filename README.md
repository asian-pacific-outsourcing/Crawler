# Sukhumwit Crawler

## Overview
Sukhumwit Crawler is a Java Swing-based application that mines potential contacts from Web Sites.  Once a contact page has been found it is parsed to extract the contact info into discrete fields which may be stored in an SQL database. This database will be shared by other apps in the suite which are designed to mercilessly fleece the contacts.

> The idea is to easily generate massive quantities of leads which are stored in a database for later use.

## Operation
This application begins at Web page specified by the user. It scans this page
 for links to other pages, which are each scanned in turn for more web pages.
 This scanning will continue until either no more links are found, or the
 maximum depth is reached.

Concurrent with the scan for links, a parser looks for contact information.
