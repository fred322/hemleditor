/**
 * 
 */
package net.fredncie.hemleditor.editors;

import org.w3c.dom.Node;

/**
 * 
 */
public class XsdNode {
    private String name;
    private Node node;
    
    /**
     *  the name of the node
     * @return the name
     */
    public String getName() {
        return name;
    }   
    
    /**
     * the node.
     * @return the node
     */
    public Node getNode() {
        return node;
    }
    
    protected boolean load(Node node) {
        this.node = node;
        
        this.name = getAttributeValue("name");
        return this.name != null;
    } 
    
    protected String getAttributeValue(String attributeName) {
        String ret = null;
        if (node.hasAttributes()) {
            Node attributeNode = node.getAttributes().getNamedItem(attributeName);
            ret = attributeNode != null ? attributeNode.getNodeValue() : null;            
        }
        return ret;
    }
}
