package de.uma.dcsim.utilities;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class Constants {
	
	public static final String DECIMAL_SEPARATOR = ",";
	
	public static final String CSV_SEPARATOR = ";";
	
	public static final SimpleDateFormat getDateFormat() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.GERMAN);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return dateFormat;
	}

}
