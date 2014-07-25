package net.sf.taverna.t2.activities.jsonpath;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;

/**
 * A {@link HealthChecker} for a {@link JsonPathActivity}.
 *
 * @author Sergejs Aleksejevs
 */
public class JsonPathActivityHealthChecker implements HealthChecker<JsonPathActivity>
{
  public boolean canVisit(Object subject) {
    return (subject instanceof JsonPathActivity);
  }
  
  
  public VisitReport visit(JsonPathActivity activity, List<Object> ancestors)
  {
    // collection of validation reports that this health checker will create
    List<VisitReport> reports = new ArrayList<VisitReport>();
    
    
    JsonPathActivityConfigurationBean configBean = activity.getConfiguration();
    if (configBean.isValid()) {
      reports.add(new VisitReport(JsonPathActivityHealthCheck.getInstance(), activity, 
                                  "JsonPath Activity is configured correctly", 
                                  JsonPathActivityHealthCheck.CORRECTLY_CONFIGURED, Status.OK));
    }
    else {
      int xpathStatus = JsonPathActivityConfigurationBean.validateJsonPath(configBean.getJsonPathAsString());
      if (xpathStatus == JsonPathActivityConfigurationBean.JSONPATH_EMPTY) {
        reports.add(new VisitReport(JsonPathActivityHealthCheck.getInstance(), activity, 
                                    "JsonPath Activity - JsonPath expression is missing", 
                                    JsonPathActivityHealthCheck.EMPTY_JSONPATH_EXPRESSION, Status.SEVERE));
      }
      else if (xpathStatus == JsonPathActivityConfigurationBean.JSONPATH_INVALID) {
        reports.add(new VisitReport(JsonPathActivityHealthCheck.getInstance(), activity, 
                                    "JsonPath Activity - JsonPath expression is invalid", 
                                    JsonPathActivityHealthCheck.INVALID_JSONPATH_EXPRESSION, Status.SEVERE));
      }
      else {
        reports.add(new VisitReport(JsonPathActivityHealthCheck.getInstance(), activity, 
                                    "JsonPath Activity - bad configuration", 
                                    JsonPathActivityHealthCheck.GENERAL_CONFIG_PROBLEM, Status.SEVERE));
      }
    }
    
    
    // warn if there is no example JSON document
    if (configBean.getJsonText() == null || configBean.getJsonText().trim().length() == 0) {
      reports.add(new VisitReport(JsonPathActivityHealthCheck.getInstance(), activity, 
                                  "JsonPath activity - no example JSON document", 
                                  JsonPathActivityHealthCheck.NO_EXAMPLE_DOCUMENT, Status.WARNING));
    }
    
    
    // collect all reports together
    Status worstStatus = VisitReport.getWorstStatus(reports);
    VisitReport report = new VisitReport(JsonPathActivityHealthCheck.getInstance(), activity, 
                                         "JsonPath Activity Report", HealthCheck.NO_PROBLEM, worstStatus, reports);
    
    return report;
  }
  
  
  
  /**
   * Health check for the XPath activity only involves
   * verifying details in the configuration bean -
   * that is quick.
   */
  public boolean isTimeConsuming() {
    return false;
  }

}
