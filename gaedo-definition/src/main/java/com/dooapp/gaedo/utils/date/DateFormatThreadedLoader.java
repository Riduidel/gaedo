package com.dooapp.gaedo.utils.date;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A thread local loader optimizing the use of a DateFormat (which has the inconvenience of being both slow to start and not thread-safe
 * @author ndx
 *
 */
public class DateFormatThreadedLoader {
	/**
	 * Format container allowing lazy loading
	 */
	private transient final ThreadLocal<SoftReference<DateFormat>> format = new ThreadLocal<SoftReference<DateFormat>>();
	/**
	 * Format string defining how this format is to be applied
	 */
	private final String formatString;

	public DateFormatThreadedLoader(String formatString) {
		super();
		this.formatString = formatString;
	}

	public DateFormat getFormat() {
		SoftReference<DateFormat> softRef = format.get();
		if (softRef != null) {
			final DateFormat result = softRef.get();
			if (result != null) {
				return result;
			}
		}
		final DateFormat result = new SimpleDateFormat(formatString);
		softRef = new SoftReference<DateFormat>(result);
		format.set(softRef);
		return result;
	}
	
	public String format(Date date) {
		return getFormat().format(date);
	}
	
	public Date parse(String string) throws ParseException {
		return getFormat().parse(string);
	}
}