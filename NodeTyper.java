import org.w3c.dom.Node;


public class NodeTyper {

   public static String getTypeName(Node node) {
    
    int type = node.getNodeType();
    /* Yes, getNodeType() returns a short, but Java will
       almost always upcast this short to an int before
       using it in any operation, so we might as well just go
       ahead and use the int in the first place. */
    
    switch (type) {
      case Node.ELEMENT_NODE: return "Element";
      case Node.ATTRIBUTE_NODE: return "Attribute";
      case Node.TEXT_NODE: return "Text";
      case Node.CDATA_SECTION_NODE: return "CDATA Section";
      case Node.ENTITY_REFERENCE_NODE: return "Entity Reference";
      case Node.ENTITY_NODE: return "Entity";
      case Node.PROCESSING_INSTRUCTION_NODE: return "Processing Instruction";
      case Node.COMMENT_NODE: return "Comment";
      case Node.DOCUMENT_NODE: return "Document";
      case Node.DOCUMENT_TYPE_NODE: return "Document Type Declaration";
      case Node.DOCUMENT_FRAGMENT_NODE: return "Document Fragment";
      case Node.NOTATION_NODE: return "Notation";
      default: return "Unknown Type"; 
   /* It is possible for the default case to be
      reached. DOM only defines 12 kinds of nodes, but other
      application specific DOMs can add their own as well.
      You're not likely to encounter these while parsing an
      XML document with a standard parser, but you might
      encounter such things with custom parsers designed for
      non-XML documents. DOM Level 3 XPath does define a 
      thirteenth kind of node, XPathNamespace. */
    }
    
  }

}
