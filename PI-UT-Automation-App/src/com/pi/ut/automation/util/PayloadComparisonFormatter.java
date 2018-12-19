package com.pi.ut.automation.util;

import org.w3c.dom.Node;
import org.xmlunit.diff.Comparison;
import org.xmlunit.diff.ComparisonFormatter;
import org.xmlunit.diff.ComparisonType;
import org.xmlunit.diff.Comparison.Detail;

public class PayloadComparisonFormatter implements ComparisonFormatter {
	/**
	 * Outputs the comparison difference in an XML format for printing
	 */
	@Override
	 public String getDescription(Comparison difference) {
		ComparisonType type = difference.getType();
		String description = type.getDescription();
        Detail controlDetails = difference.getControlDetails();
        Detail testDetails = difference.getTestDetails();
		
        StringBuilder sXmlBuilder = new StringBuilder();
        sXmlBuilder.append("<ComparisonResult>");
        sXmlBuilder.append(String.format("<Control>%s</Control>",controlDetails.getXPath()));
        sXmlBuilder.append(String.format("<Test>%s</Test>",testDetails.getXPath()));
        
		if(type == ComparisonType.ATTR_NAME_LOOKUP ) {
			String sResult =  String.format("Expected %s '%s'",description, controlDetails.getXPath());
			sXmlBuilder.append(String.format("<Result>%s</Result>",sResult));
	    }else{
	    	String sResult = String.format("Expected %s '%s' but was '%s'",  description,getValue(controlDetails.getValue(), type), getValue(testDetails.getValue(), type)); 
	    	sXmlBuilder.append(String.format("<Result>%s</Result>",sResult));	
	    }
	    sXmlBuilder.append("</ComparisonResult>");
	    return sXmlBuilder.toString();
	 }

	@Override
	public String getDetails(Detail difference, ComparisonType type,	boolean formatXml) {
        try{
            if (difference.getTarget() == null) {
                return "<NULL>";
            }
        	return ApplicationUtil.xmlDocumentToString(difference.getTarget(),true);
        }catch(Exception ex){
        	return "<Exceptioon>"+ex.getMessage()+"</Exception>";
        }
        

	}
	   /**
     * May alter the display of a comparison value for {@link #getShortString} based on the comparison type.
     *
     * <p>This implementation returns {@code value} unless it is a comparison of node types in which case the numeric
     * value (one of the constants defined in the {@link Node} class) is mapped to a more useful String.</p>
     *
     * @param value the value to display
     * @param type the comparison type
     * @return the display value

     */
    protected Object getValue(Object value, ComparisonType type) {
        Object oVal = (type == ComparisonType.NODE_TYPE ? nodeType((Short) value) : value);
        return ApplicationUtil.escapeXMLChars(String.valueOf(oVal));
    }
    
    /**
     * Provides a display text for the constant values of the {@link Node} class that represent node types.
     *
     * @param type the node type
     * @return the display text
     *
     */
    protected String nodeType(short type) {
        switch(type) {
        case Node.ELEMENT_NODE:                return "Element";
        case Node.DOCUMENT_TYPE_NODE:          return "Document Type";
        case Node.ENTITY_NODE:                 return "Entity";
        case Node.ENTITY_REFERENCE_NODE:       return "Entity Reference";
        case Node.NOTATION_NODE:               return "Notation";
        case Node.TEXT_NODE:                   return "Text";
        case Node.COMMENT_NODE:                return "Comment";
        case Node.CDATA_SECTION_NODE:          return "CDATA Section";
        case Node.ATTRIBUTE_NODE:              return "Attribute";
        case Node.PROCESSING_INSTRUCTION_NODE: return "Processing Instruction";
        default: break; 
        }
        return Short.toString(type);
    }
    
}
