/**
 * Copyright (C) 2013, University of Manchester and University of Southampton
 *
 * Licensed under the GNU Lesser General Public License v2.1
 * See the "LICENSE" file that is distributed with the source code for license terms. 
 */
package net.sf.taverna.t2.activities.jsonpath.utils;

import java.util.LinkedList;

import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.PathToken;
import com.jayway.jsonpath.internal.PathTokenizer;
import com.jayway.jsonpath.internal.filter.PathTokenFilter;
import com.jayway.jsonpath.spi.JsonProvider;
import com.jayway.jsonpath.spi.JsonProviderFactory;

/**
 * Utility methods for JSONPath.
 * 
 * @author Mark Borkum
 * @version 0.0.1-SNAPSHOT
 * @see <a href="http://goessner.net/articles/JsonPath/">JSONPath Documentation</a>
 */
public final class JSONPathUtils {
	
	/**
	 * Default JSON provider (an interface used by JSONPath.)
	 */
	private static JsonProvider defaultProvider;
	
	/**
	 * Returns the default JSON provider.
	 * 
	 * @return  The default JSON provider.
	 */
	public static final JsonProvider createProvider() {
		if (defaultProvider == null) {
			defaultProvider = JsonProviderFactory.createProvider();
		}
		
		return defaultProvider;
	}
	
	/**
	 * Applies the given <code>jsonPath</code> to the given <code>jsonValue</code> using the default JSON provider.
	 * <p>
	 * Equivalent to: <code>read(createProvider(), jsonPath, jsonValue);</code>
	 * 
	 * @param <T>  expected return type
	 * @param jsonPath  The JSONPath expression.
	 * @param jsonValue  The JSON value.
	 * @return  The result of the application. 
	 * @see JsonPath#read(Object)
	 * @throws IllegalArguemntException  If <code>jsonPath == null</code>.
	 */
	public static final <T> T read(final JsonPath jsonPath, final Object jsonValue) throws IllegalArgumentException {
		return read(createProvider(), jsonPath, jsonValue);
	}
	
	/**
	 * Applies the given <code>jsonPath</code> to the given <code>jsonValue</code> using the given <code>jsonProvider</code>.
	 * 
	 * @param <T>  expected return type
	 * @param jsonProvider  The JSON provider.
	 * @param jsonPath  The JSONPath expression.
	 * @param jsonValue  The JSON value.
	 * @return  The result of the application. 
	 * @see JsonPath#read(Object)
	 * @throws IllegalArguemntException  If <code>jsonProvider == null || jsonPath == null</code>.
	 */
	public static final <T> T read(final JsonProvider jsonProvider, final JsonPath jsonPath, final Object jsonValue) throws IllegalArgumentException {
		if (jsonProvider == null) {
			throw new IllegalArgumentException(new NullPointerException("jsonProvider"));
		} else if (jsonPath == null) {
			throw new IllegalArgumentException(new NullPointerException("jsonPath"));
		} 
		
		if (!jsonProvider.isContainer(jsonValue)) {
			// For reasons unknown, JSONPath expressions can only be applied to lists or maps (referred to as "containers".) 
			throw new IllegalArgumentException("Invalid container object");
		}
		
		// Initialize the 'result' object to be the given 'jsonValue'.
		Object result = jsonValue;
		
		boolean arrayFilter = false;
		
		// This object must be an instance of LinkedList to satisfy the PathTokenFilter#filter(Object, JsonProvider, LinkedList<Filter>, boolean) interface.
		@SuppressWarnings("rawtypes")
		final LinkedList<Filter> contextFilters = new LinkedList<Filter>();
		
		for (final PathToken pathToken : new PathTokenizer(jsonPath.getPath()).getPathTokens()) {
			if (pathToken != null) {
				final PathTokenFilter pathTokenFilter = pathToken.getFilter();
				
				if (pathTokenFilter != null) {
					// Apply the next filter to the current 'result' object.
					result = pathTokenFilter.filter(result, jsonProvider, contextFilters, arrayFilter);
					
					arrayFilter = arrayFilter || pathTokenFilter.isArrayFilter();
				}
			}
		}
		
		@SuppressWarnings("unchecked")
		final T castResult = (T) result;
		
		return castResult;
	}
	
	/**
	 * Sole constructor.
	 */
	private JSONPathUtils() {
		super();
	}

}
