package net.fredncie.hemleditor.editors;

import java.util.Stack;

/**
 * Object to compute the indentation for the creation of heml
 */
public class HemlIndenter {
    private static final int DEFAULT_INDENT_LENGTH = 4;
    private static final String DEFAULT_INDENT = String.format("%" + DEFAULT_INDENT_LENGTH + "s", "");
    
    private int originalCount = 0;
    private int originalIndentSize = 0;
    
    private int currentIndent = 0;
    private String currentIndentStr = "";
    // list of previous indentation
    private final Stack<Integer> itemsSpaceCount = new Stack<>();

    public HemlIndenter() {
    }
    
    private HemlIndenter(int firstIndentCount, int indentSize) {
        this.originalCount = firstIndentCount;
        this.originalIndentSize = indentSize;
        clearIndents();
    }

    public static String removeTabulation(String text) {
        return text.replace("\t", DEFAULT_INDENT); 
    }
    
    public boolean computeIndent(int previousIndent) {
        boolean changed = false;
        if (this.itemsSpaceCount.empty() || previousIndent > this.itemsSpaceCount.peek()) {
            this.itemsSpaceCount.add(previousIndent);
            changed = true;
        }
        else {
            changed = removeIfLesser(previousIndent);
        }
        if (changed) computeIndents();
        return changed;
    }
    
    /**
     * add a new indent
     */
    public void addIndent(int previousIndent) {
        this.itemsSpaceCount.add(previousIndent);
        computeIndents();
    }
     
    public void removeIndent() {
        if (!this.itemsSpaceCount.empty()) {
            this.itemsSpaceCount.pop();
            computeIndents();
        }
    }
    
    public boolean removeIfLesser(int previousIndent) {
        boolean ret = false;
        // +1 to keep the subindentation in a block
        // '<=' to remove the duplicates
        if (this.itemsSpaceCount.size() > this.originalCount + 1 && previousIndent <= this.itemsSpaceCount.peek()) {
            while (this.itemsSpaceCount.size() > this.originalCount + 1 && previousIndent <= this.itemsSpaceCount.peek()) {
                this.itemsSpaceCount.pop();
            }
            if (this.itemsSpaceCount.empty() || previousIndent > this.itemsSpaceCount.peek())
                this.itemsSpaceCount.add(previousIndent);
            
            computeIndents();
            ret = true;
        }
        return ret;
    }

    public void clearIndents() {
        this.itemsSpaceCount.clear();
        for (int idx = 1; idx < originalCount; idx++) addIndent(0);
        if (originalCount > 0) this.itemsSpaceCount.add(originalIndentSize);
        computeIndents();
    }
    
    public HemlIndenter createNew(int indentSize) {
        return new HemlIndenter(this.itemsSpaceCount.size(), indentSize);
    }
    
    public int getCurrentIndent() { return this.currentIndent; }
    public String getCurrentIndentStr() { return this.currentIndentStr; }
    
    /**
     * @return the originalIndentSize
     */
    public int getOriginalIndentSize() {
        return originalIndentSize;
    }
    
    private void computeIndents() {
        this.currentIndent = this.itemsSpaceCount.size() * DEFAULT_INDENT_LENGTH;
        this.currentIndentStr = this.itemsSpaceCount.empty() ? "" : String.format("%" + (this.itemsSpaceCount.size() * DEFAULT_INDENT_LENGTH) + "s", "");
    }
}
