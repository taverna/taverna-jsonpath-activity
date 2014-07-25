package net.sf.taverna.t2.activities.jsonpath;

import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.sf.taverna.t2.lang.ui.ReadOnlyTextArea;
import net.sf.taverna.t2.visit.VisitKind;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.workbench.report.explainer.VisitExplainer;
import net.sf.taverna.t2.workbench.report.view.ReportViewConfigureAction;
import net.sf.taverna.t2.workflowmodel.Processor;

// import status constants
import static net.sf.taverna.t2.activities.jsonpath.JsonPathActivityHealthCheck.*;

/**
 * 
 * @author Sergejs Aleksejevs
 */
public class JsonPathActivityHealthCheckVisitExplainer implements VisitExplainer
{
  
  public boolean canExplain(VisitKind vk, int resultId) {
    return (vk instanceof JsonPathActivityHealthCheck);
  }
  
  
  /**
   * This class only handles {@link VisitReport} instances that are of
   * {@link JsonPathActivityHealthCheck} kind. Therefore, decisions on
   * the explanations / solutions are made solely by visit result IDs.
   */
  public JComponent getExplanation(VisitReport vr)
  {
    int resultId = vr.getResultId();
    String explanation = null;
    
    switch (resultId) {
      case CORRECTLY_CONFIGURED:
        explanation = "No problem found"; break;
        
      case EMPTY_JSONPATH_EXPRESSION:
        explanation = "JsonPath expression that this activity would apply to " +
        		          "the JSON document at its input is not set"; break;
        
      case INVALID_JSONPATH_EXPRESSION:
        explanation = "JsonPath expression that this activity would apply to " +
                      "the JSON document at its input is invalid or ill-formed"; break;
        
      case GENERAL_CONFIG_PROBLEM:
        explanation = "Configuration of this JsonPath activity is not valid"; break;
        
      case NO_EXAMPLE_DOCUMENT:
        explanation = "Current configuration of this JsonPath activity does not " +
        		          "contain an example JSON document, from the tree structure of " +
        		          "which the JsonPath expression could be generated automatically.\n\n" +
        		          "This means that you have manually added the JsonPath expression - " +
        		          "this is fine, but semantic mistakes can be easily introduced into " +
        		          "the JsonPath expression in this case."; break;
        
      default:
        explanation = "Unknown issue - no expalanation available"; break;
    }
    
    return new ReadOnlyTextArea(explanation);
  }
  
  
  
  /**
   * This class only handles {@link VisitReport} instances that are of
   * {@link JsonPathActivityHealthCheck} kind. Therefore, decisions on
   * the explanations / solutions are made solely by visit result IDs.
   */
  public JComponent getSolution(VisitReport vr)
  {
    int resultId = vr.getResultId();
    String explanation = null;
    boolean includeConfigButton = false;
    
    switch (resultId) {
      case CORRECTLY_CONFIGURED:
        explanation = "No change necessary"; break;
        
      case EMPTY_JSONPATH_EXPRESSION:
        explanation = "Enter the JsonPath expression manually or paste an example JSON document " +
        		          "and select desired element from the automatically-generated tree structure:";
                      includeConfigButton = true;
                      break;
        
      case INVALID_JSONPATH_EXPRESSION:
        explanation = "Please check correctness of the JsonPath expression:";
                      includeConfigButton = true;
                      break;
        
      case GENERAL_CONFIG_PROBLEM:
        explanation = "Please check configuration of the JsonPath activity:";
                      includeConfigButton = true;
                      break;
        
      case NO_EXAMPLE_DOCUMENT:
        explanation = "Example JSON document can be added in the configuration panel:";
                      includeConfigButton = true;
                      break;
        
      default:
        explanation = "Unknown issue - no expalanation available"; break;
    }
    
    
    JPanel jpSolution = new JPanel();
    jpSolution.setLayout(new BoxLayout(jpSolution, BoxLayout.Y_AXIS));
    
    ReadOnlyTextArea taExplanation = new ReadOnlyTextArea(explanation);
    taExplanation.setAlignmentX(Component.LEFT_ALIGNMENT);
    jpSolution.add(taExplanation);
    
    if (includeConfigButton)
    {
      JButton button = new JButton();
      Processor p = (Processor) (vr.getSubject());
      button.setAction(new ReportViewConfigureAction(p));
      button.setText("Open JsonPath Activity configuration dialog");
      button.setAlignmentX(Component.LEFT_ALIGNMENT);
      
      jpSolution.add(button);
    }
    
    
    return (jpSolution);
  }
  
}
