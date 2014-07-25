package net.sf.taverna.t2.activities.jsonpath;

import java.io.Serializable;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

/**
 * 
 * @author Sergejs Aleksejevs 
 */
public class JsonPathActivityConfigurationBean implements Serializable
{
  // --- CONSTANTS ---
  public static final int JSONPATH_VALID = 1;
  public static final int JSONPATH_EMPTY = 0;
  public static final int JSONPATH_INVALID = -1;
  
  
	private String jsonText;
	private String jsonPathAsString;
	
	
	/**
	 * @return An instance of the {@link JsonPathActivityConfigurationBean} pre-configured with
	 *         default settings for all parameters.
	 * @throws DocumentException 
	 */
	public static JsonPathActivityConfigurationBean getDefaultInstance()
	{
	  // document will not be set
	  JsonPathActivityConfigurationBean defaultBean = new JsonPathActivityConfigurationBean();
	  defaultBean.setJsonPathAsString("$");
	  
    return (defaultBean);
	}
	
	
	
	/**
   * Validates an XPath expression.
   * 
   * @return {@link JsonPathActivityConfigurationBean#JSONPATH_VALID JSONPATH_VALID} - if the expression is valid;<br/>
   *         {@link JsonPathActivityConfigurationBean#JSONPATH_EMPTY JSONPATH_EMPTY} - if expression is empty;<br/>
   *         {@link JsonPathActivityConfigurationBean#JSONPATH_INVALID JSONPATH_INVALID} - if the expression is invalid / ill-formed.<br/>
   */
  public static int validateJsonPath(String jsonPathToValidate)
  {
    // no JsonPath expression
    if (jsonPathToValidate == null || jsonPathToValidate.trim().length() == 0) {
      return (JSONPATH_EMPTY);
    }
    
    try {
  	  JsonPath dummy = JsonPath.compile(jsonPathToValidate.trim());
      // ...success
      return (JSONPATH_VALID);
    }
    catch (InvalidPathException e) {
      // ...failed to parse the JsonPath expression: notify of the error
      return (JSONPATH_INVALID);
    }
  }
	
	
	
	/**
	 * Tests validity of the configuration held in this bean.
	 * 
	 * @return <code>true</code> if the configuration in the bean is valid;
	 *         <code>false</code> otherwise.
	 */
	public boolean isValid() {
	  return (jsonPathAsString != null &&
	          validateJsonPath(jsonPathAsString) == JSONPATH_VALID);
	}



	/**
	 * @return the jsonText
	 */
	public final String getJsonText() {
		return jsonText;
	}



	/**
	 * @param jsonText the jsonText to set
	 */
	public final void setJsonText(String jsonText) {
		this.jsonText = jsonText;
	}



	/**
	 * @return the jsonPathAsString
	 */
	public final String getJsonPathAsString() {
		return jsonPathAsString;
	}



	/**
	 * @param jsonPathAsString the jsonPathAsString to set
	 */
	public final void setJsonPathAsString(String jsonPathAsString) {
		this.jsonPathAsString = jsonPathAsString;
	}
	
}
