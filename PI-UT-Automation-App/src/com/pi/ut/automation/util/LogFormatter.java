package com.pi.ut.automation.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class LogFormatter extends SimpleFormatter{
	private static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
	
	public String format(LogRecord record) {
		  SimpleDateFormat sdf = new SimpleDateFormat(PATTERN);
		  String sLogDate = sdf.format(new Date(record.getMillis())); 	
		  String sLogLoc = record.getSourceClassName()+"."+record.getSourceMethodName();
	      return String.format("%s %-7s %-85s %s\n",sLogDate, record.getLevel().getName(),sLogLoc, formatMessage(record));
	}
}
