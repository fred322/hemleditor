/**
 * 
 */
package net.fredncie.hemleditor.editors;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 */
public class HemlAutocompletionProvider {

    private Document fLoadedDocument;
    private final Map<String, XsdElement> fMapNodes = new HashMap<String, XsdElement>();
    private final XsdTypes fXsdTypes = new XsdTypes();
    
    public void loadXsd(InputStream stream) {
        fMapNodes.clear();
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            fLoadedDocument = builder.parse(stream);
            fillMapNodes(fLoadedDocument.getFirstChild());
        }
        catch (SAXException | IOException e) {
            e.printStackTrace();
        }
        catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
    
    public String[] getAttributes(HemlElement element) {
        String[] ret = null;
        if (element != null) {
            XsdElement xsdElement = getElement(element);
            if (xsdElement != null) {
                XsdType type = fXsdTypes.getType(xsdElement.getType());
                if (type != null) {
                    ret = type.getAttributes().stream().map(el -> el.getName()).toArray(String[]::new);                    
                }
                else {
                    System.err.println("Cannot find the type : " + xsdElement.getType());
                }
            }
            else {
                System.err.println("Cannot found element " + element.getQualifier() + " in XSD");
            }
        }
        return ret == null ? new String[0] : ret;
    }
    
    public String[] getElements(HemlElement element) {
        String[] ret = null;
        if (element != null) {
            XsdElement xsdElement = getElement(element);
            if (xsdElement != null) {
                XsdType type = fXsdTypes.getType(xsdElement.getType());
                if (type != null) {
                    ret = type.getElements().stream().map(el -> el.getName()).toArray(String[]::new);                    
                }
                else {
                    System.err.println("Cannot find the type : " + xsdElement.getType());
                }
            }
            else {
                System.err.println("Cannot found element " + element.getQualifier() + " in XSD");
            }
        }
        return ret == null ? new String[0] : ret;
    }
    
    private XsdElement getElement(HemlElement element) {
        if (element.getParent() != null) {
             XsdElement xsdParent = getElement(element.getParent());
             if (xsdParent != null) {
                 String parentType = xsdParent.getType();
                 XsdType parentXsdType = fXsdTypes.getType(parentType);
                 return parentXsdType != null ? parentXsdType.getElement(element.getQualifier()) : null;
             }
             else {
                 System.err.println("Cannot found parent in xsd for " + element.getQualifier());
             }
             return null;
        }
        else return fMapNodes.get(element.getQualifier());
    }
    
    private void fillMapNodes(Node parentNode) {
        NodeList nodes = parentNode.getChildNodes();
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Node node = nodes.item(idx);
            String nodeName = node.getNodeName();
            if ("complexType".equals(nodeName)) {
                fXsdTypes.addType(node);
            }
            else if ("element".equals(nodeName)) {
                XsdElement element = XsdElement.create(node);
                fMapNodes.put(element.getName(), element);
            }
        }
    }
}
