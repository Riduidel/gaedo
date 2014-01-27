package com.dooapp.gaedo.blueprints.transformers;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;

import com.dooapp.gaedo.blueprints.GraphDatabaseDriver;
import com.dooapp.gaedo.blueprints.ObjectCache;
import com.dooapp.gaedo.blueprints.Properties;
import com.dooapp.gaedo.utils.date.DateFormatThreadedLoader;
import com.tinkerpop.blueprints.Vertex;

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

	/**
	 * Map linking date format specification (a uri like xsd:date or http://www.w3.org/2001/XMLSchema#date) to the format string used to parse it.
	 */
	private static final Map<String, String> FORMATS = new TreeMap<String, String>();

	static {
		FORMATS.put("xsd:date", "yyyy-MM-dd'T'HH:mm:ssz");
		FORMATS.put("http://www.w3.org/2001/XMLSchema#date", "yyy-MM-dd");
	}


	/**
	 * A map allowing lazy loading of those inconvenient threaded loaders.
	 */
	private static final Map<String, DateFormatThreadedLoader> loaders = new TreeMap<String, DateFormatThreadedLoader>();

	/**
	 * Cache linking dates to toeir associated format definition.
	 * As the dates are in memory, and the formats are constants, this weak hash map should have very low memory impact.
	 */
	private static Map<Date, String> dateCache = new WeakHashMap<Date, String>();

	public static DateFormatThreadedLoader getLoader(String format) {
		if(!loaders.containsKey(format))
			loaders.put(format, new DateFormatThreadedLoader(format));
		return loaders.get(format);
	}

	public DateLiteralTransformer() {
		super(Date.class);
	}

	@Override
	public String valueToString(Date value) {
		// fallback value
		String format = getTypeOf(value);
		return getLoader(FORMATS.get(format)).format(value);
	}

	@Override
	public Date loadObjectFromVertex(GraphDatabaseDriver driver, Class valueClass, Vertex key, ObjectCache objectCache) {
		String property = driver.getValue(key).toString();
		String type = key.getProperty(Properties.type.name()).toString();
		try {
			Date returned = getLoader(FORMATS.get(type)).parse(property);
			dateCache.put(returned, type);
			return returned;
		} catch (ParseException e) {
			throw new BadLiteralException("\"" + property + "\" can't be efficiently parsed from a \""+type+"\" supposed to contain a date");
		}
	}

	/**
	 * Type of date depends upon the date
	 * @param value
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.AbstractLiteralTransformer#getTypeOf(java.lang.Object)
	 */
	@Override
	public String getTypeOf(Object value) {
		String format = "xsd:date";
		if(dateCache.containsKey(value)) {
			format = dateCache.get(value);
		}
		dateCache.put((Date) value, format);
		return format;
	}

	/**
	 * Load value using the first format that may work. This is unfortunatly not optimal.
	 * @param valueClass
	 * @param valueString
	 * @return
	 * @see com.dooapp.gaedo.blueprints.transformers.AbstractLiteralTransformer#loadValueFromString(java.lang.Class, java.lang.String)
	 */
	@Override
	protected Date loadValueFromString(Class valueClass, String valueString) {
		for(String format : FORMATS.values()) {
			try {
				return getLoader(format).parse(valueString);
			} catch(ParseException e) {
			}
		}
		// if value can't be loaded, well, delegate to superclass (it will fail for sure)
		return super.loadValueFromString(valueClass, valueString);
	}
}
