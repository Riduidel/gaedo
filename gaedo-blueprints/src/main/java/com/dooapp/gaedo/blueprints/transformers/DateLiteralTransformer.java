package com.dooapp.gaedo.blueprints.transformers;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.Properties;
import com.tinkerpop.blueprints.pgm.Vertex;

/**
 * As SimpleDateFormat is not thread-safe and construction heavy, see here
 * (http:
 * //www.jmdoudoux.fr/java/dej/chap-utilisation_dates.htm#utilisation_dates-3)
 * for the why of this pattern
 * 
 * Furthermore, used pattern is ISO standard (see there : http://www.fileformat.info/tip/java/simpledateformat.htm)
 * 
 * @author ndx
 * 
 */
public class DateLiteralTransformer extends AbstractSimpleLiteralTransformer<Date> implements LiteralTransformer<Date> {
	public static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ssz";
	
	private static final ThreadLocal<SoftReference<DateFormat>> format = new ThreadLocal<SoftReference<DateFormat>>();

	private static DateFormat getDateFormat() {
		SoftReference<DateFormat> softRef = format.get();
		if (softRef != null) {
			final DateFormat result = softRef.get();
			if (result != null) {
				return result;
			}
		}
		final DateFormat result = new SimpleDateFormat(FORMAT);
		softRef = new SoftReference<DateFormat>(result);
		format.set(softRef);
		return result;
	}
	
	public DateLiteralTransformer() {
		super(Date.class);
	}

	@Override
	protected Object getVertexValue(Date value) {
		return getDateFormat().format(value);
	}

	public Date loadObject(GraphDatabaseDriver driver, Class valueClass, Vertex key) {
		String property = driver.getValue(key).toString();
		try {
			return getDateFormat().parse(property.toString());
		} catch (ParseException e) {
			throw new BadLiteralException("\"" + property + "\" can't be efficiently parsed to a date", e);
		}
	}
}
