/**
 * 
 */
package net.fredncie.hemleditor.editors;

import org.w3c.dom.Node;

/**
 * 
 */
public class XsdElement extends XsdNode {
    private String type;
    
    private XsdElement() {}
    
    /**
     * tyhe type of the element.
     * @return the type
     */
    public String getType() {
        return type;
    }
    
    public static XsdElement create(Node node) {
        XsdElement ret = new XsdElement();
        return ret.load(node) ? ret : null;
    }
    
    @Override
    protected boolean load(Node node) {
        boolean ret = super.load(node);
        if (ret) {
            this.type = getAttributeValue("type");
            if (this.type != null) {
                this.type = this.type.substring(this.type.lastIndexOf(':') + 1);
            }
            ret = this.type != null;
        }
        return ret;
    }
}
