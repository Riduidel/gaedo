package com.dooapp.gaedo.blueprints;

import java.util.regex.Pattern;

/**
 * Constants used to have a semantic graph
 * @author ndx
 *
 */
public interface SemanticGraphConstants {

	/**
	 * Context property, used for named graphs.
	 */
	public static final String CONTEXT_PROPERTY = "c";
	/**
	 * Predicate property, declared in a way that totally matches Sail graph one
	 */
	public static final String PREDICATE_PROPERTY = "p";

	public static final String CONTEXT_PREDICATE_PROPERTY = CONTEXT_PROPERTY + PREDICATE_PROPERTY;

	public static final String NULL_CONTEXT_NATIVE = "N";

	public static final String URI_PREFIX = "U";
	/**
	 * Compiled pattern used to match strings such as
	 *
	 * <pre>
	 * U https://github.com/Riduidel/gaedo/visible  U http://purl.org/dc/elements/1.1/description
	 * </pre>
	 *
	 * or
	 *
	 * <pre>
	 * N U http://purl.org/dc/elements/1.1/description
	 * </pre>
	 *
	 * or even
	 *
	 * <pre>
	 * N
	 * </pre>
	 *
	 * You know why I do such a pattern matching ? Because sail graph named
	 * graph definintion goes by concatenating contexts URI in edges properties.
	 * This is really douchebag code !
	 */
	public static final Pattern CONTEXTS_MATCHER = Pattern.compile("(N|U ([\\S]+))+");

}
