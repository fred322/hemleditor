/**
 * 
 */
package net.fredncie.hemleditor.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 */
public class XsdType extends XsdNode {
    
    private final Map<String, XsdElement> fElements = new HashMap<String, XsdElement>();
    private final Map<String, XsdElement> fAttributes = new HashMap<String, XsdElement>();

    private XsdType() {}
    
    public static XsdType create(Node type) {
        XsdType ret = new XsdType();
        return ret.load(type) ? ret : null;
    }
    
    public XsdElement getElement(String elementName) {
        return fElements.get(elementName);        
    }
    
    public List<XsdElement> getElements() {
        return fElements.values().stream().collect(Collectors.toList());
    }
    public List<XsdElement> getAttributes() {
        return fAttributes.values().stream().collect(Collectors.toList());
    }
    
    @Override
    protected boolean load(Node type) {
        boolean ret = super.load(type);
        NodeList nodes = type.getChildNodes();
        for (int idx1 = 0; idx1 < nodes.getLength(); idx1++) {
            Node subNode = nodes.item(idx1);
            if (subNode.hasChildNodes()) {
                NodeList subNodeNodes = subNode.getChildNodes();
                for (int idx = 0; idx < subNodeNodes.getLength(); idx++) {
                    Node node = subNodeNodes.item(idx);
                    XsdElement newElement = XsdElement.create(node);
                    if (newElement != null) {
                        fElements.put(newElement.getName(), newElement);
                    }
                }
            }
            else if ("attribute".equals(subNode.getNodeName())) {
                XsdElement newElement = XsdElement.create(subNode);
                if (newElement != null) {
                    fAttributes.put(newElement.getName(), newElement);
                }
            }
        }
        return ret;
    }
}
