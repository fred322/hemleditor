/**
 * 
 */
package net.fredncie.hemleditor.editors;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Node;

/**
 * 
 */
public class XsdTypes {
    private Map<String, XsdType> fMapTypes = new HashMap<String, XsdType>();
    
    public boolean addType(Node node) {
        boolean ret = false;
        XsdType newType = XsdType.create(node);
        if (newType != null) {
            fMapTypes.put(newType.getName(), newType);
            ret = true;
        }
        return ret;
    }
    
    public XsdType getType(String typeName) {
        return fMapTypes.get(typeName);
    }
}
