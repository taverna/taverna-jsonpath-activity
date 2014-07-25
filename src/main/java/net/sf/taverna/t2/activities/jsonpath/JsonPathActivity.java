package net.sf.taverna.t2.activities.jsonpath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;
import net.sf.taverna.t2.activities.jsonpath.utils.JSONPathUtils;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.processor.activity.AbstractAsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivity;
import net.sf.taverna.t2.workflowmodel.processor.activity.AsynchronousActivityCallback;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

/**
 * Enhanced XPath activity.
 * 
 * @author Sergejs Aleksejevs
 */
public class JsonPathActivity extends
		AbstractAsynchronousActivity<JsonPathActivityConfigurationBean>
		implements AsynchronousActivity<JsonPathActivityConfigurationBean>
{
  
	private static final String NULL_VALUE_FOR_STRING = "";
	// These ports are default ones (and only ones - JsonPath activity will not have dynamic ports)
  private static final String IN_JSON = "json_text";
  private static final String OUT_TEXT = "nodelist";
	private static final String OUT_JSON = "nodelistAsJSON";
	  private static final String SINGLE_VALUE_TEXT = "firstNode";
		private static final String SINGLE_VALUE_JSON = "firstNodeAsJSON";
	
	// Configuration bean for this activity - essentially defines a particular instance
	// of the activity through the values of its parameters
	private JsonPathActivityConfigurationBean configBean;
	
	
	@Override
  public JsonPathActivityConfigurationBean getConfiguration() {
    return this.configBean;
  }
	
	
	@Override
	public void configure(JsonPathActivityConfigurationBean configBean) throws ActivityConfigurationException
  {
	  // Check configBean is valid
	  if (! configBean.isValid()) {
	    throw new ActivityConfigurationException("Invalid configuration of JsonPath activity...");  // TODO - check this
	  }
	  
		// Store for getConfiguration()
		this.configBean = configBean;
		
		// (Re)create input/output ports depending on configuration
		configurePorts();
	}
	
	
	protected void configurePorts()
	{
		// ---- REMOVE OLD PORTS ----
	  
	  // In case we are being reconfigured - remove existing ports first to avoid duplicates
		removeInputs();
		removeOutputs();
		
		
		// ---- CREATE NEW INPUTS AND OUTPUTS ----
		
		// all ports in this activity are static, so no dependency on the values in config bean
		
		// single input port: the input JSON text will be treated as String for now
	  addInput(IN_JSON, 0, true, null, String.class);
		

	  addOutput(SINGLE_VALUE_TEXT, 0);
	  addOutput(SINGLE_VALUE_JSON, 0);
		addOutput(OUT_TEXT, 1);
		addOutput(OUT_JSON, 1);

	}
	
	
	
	/**
	 * This method executes pre-configured instance of JsonPath activity.
	 */
	public void executeAsynch(final Map<String,T2Reference> inputs, final AsynchronousActivityCallback callback)
	{
		// Don't execute service directly now, request to be run asynchronously
		callback.requestRun(new Runnable() {
			public void run() {
			  
				InvocationContext context = callback.getContext();
				ReferenceService referenceService = context.getReferenceService();
				
				// ---- RESOLVE INPUT ----
				
				String jsonInput = (String) referenceService.renderIdentifier(inputs.get(IN_JSON), String.class, context);
				
				
				// ---- DO THE ACTUAL SERVICE INVOCATION ----
				
				// only attempt to execute JsonPath expression if there is some input data
				if ((jsonInput == null) || jsonInput.length() == 0)
				{
					callback.fail("Empty input");
					return;
				}
  				// JsonPath configuration is taken from the config bean
  				JsonPath expr = null;
  				String expressionText = configBean.getJsonPathAsString();
  		    try {
  		      expr = JsonPath.compile(expressionText);
  		    }
  		    catch (InvalidPathException e)
  		    {
  		      callback.fail("Incorrect JsonPath Expression -- JsonPath processing library " +
  		      		"reported the following error: " + e.getMessage(), e);
  		      
            // make sure we don't call callback.receiveResult later
  		      return;
  		    }
  		    
  		  Object jsonObject;
  		  try {
			jsonObject = JSONValue.parseWithException(jsonInput);
		} catch (ParseException e) {
			callback.fail(e.getMessage(), e);
			return;
		}
			Object resultValue = JSONPathUtils.read(expr, jsonObject);
			List<?> resultValues = null;
			if (!(resultValue instanceof List)) {
				resultValues = Collections.singletonList(resultValue);
			} else {
				resultValues = (List) resultValue;
			}
				
				
		    // --- PREPARE OUTPUTS ---
				
		    List<String> outNodesText = new ArrayList<String>();
		    List<String> outNodesJSON = new ArrayList<String>();
		    
			for (Object o : resultValues) {
				outNodesText.add((o == null ? NULL_VALUE_FOR_STRING : o.toString()));
				outNodesJSON.add(JSONValue.toJSONString(o));
			}			
				// ---- REGISTER OUTPUTS ----
		    
				Map<String, T2Reference> outputs = new HashMap<String, T2Reference>();
				
				Object textValue;
				Object jsonValue;
				if (outNodesText.isEmpty()) {
					ErrorDocumentService errorDocService = referenceService.getErrorDocumentService();
	
					textValue = errorDocService.registerError("No value produced" , 
							0, callback.getContext());
				} else {
					textValue = outNodesText.get(0);
				}
				
				if (outNodesJSON.isEmpty()) {
					ErrorDocumentService errorDocService = referenceService.getErrorDocumentService();
	
					jsonValue = errorDocService.registerError("No value produced" , 
							0, callback.getContext());
				} else {
					jsonValue = outNodesJSON.get(0);
				}
				
				T2Reference firstNodeAsText = referenceService.register(textValue, 0, true, context);
				T2Reference firstNodeAsJson = referenceService.register(jsonValue, 0, true, context);
				outputs.put(SINGLE_VALUE_TEXT, firstNodeAsText);
				outputs.put(SINGLE_VALUE_JSON, firstNodeAsJson);
				

					T2Reference outNodesAsText = referenceService.register(outNodesText, 1, true, context);
					outputs.put(OUT_TEXT, outNodesAsText);
				
					T2Reference outNodesAsXML = referenceService.register(outNodesJSON, 1, true, context);
					outputs.put(OUT_JSON, outNodesAsXML);

				// return map of output data, with empty index array as this is
				// the only and final result (this index parameter is used if
				// pipelining output)
				callback.receiveResult(outputs, new int[0]);
			}
		});
	}

}
