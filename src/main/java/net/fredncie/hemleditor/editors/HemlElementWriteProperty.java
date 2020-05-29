/**
 * 
 */
package net.fredncie.hemleditor.editors;

/**
 * properties for the write process.
 */
public class HemlElementWriteProperty {
    private boolean firstLineRead = false;
    private boolean subIndentAdded = false;
    private boolean inline = false;
    private HemlIndenter indenter;
    
    private boolean nextHasLastLine = false;
    private boolean nextHasNewLine = false;
    
    public HemlElementWriteProperty(HemlIndenter indentation, boolean isInline) {
        this.indenter = indentation;
        this.inline = isInline;
    }

    public void setFirstLineRead(boolean firstLineRead) {
        this.firstLineRead = firstLineRead;
    }
    
    /**
     * @return the firstLineRead
     */
    public boolean isFirstLineRead() {
        return firstLineRead;
    }
    
    /**
     * @param subIndentAdded the subIndentAdded to set
     */
    public void setSubIndentAdded(boolean subIndentAdded) {
        this.subIndentAdded = subIndentAdded;
    }
    
    /**
     * @return the subIndentAdded
     */
    public boolean isSubIndentAdded() {
        return subIndentAdded;
    }
    
    /**
     * @param nextHasLastLine the fNextHasLastLine to set
     */
    public void setNextHasLastLine(boolean nextHasLastLine) {
        this.nextHasLastLine = nextHasLastLine;
    }
    
    /**
     * @return the nextHasLastLine
     */
    public boolean isNextHasLastLine() {
        return nextHasLastLine;
    }
    
    /**
     * @return the inline
     */
    public boolean isInline() {
        return inline;
    }
    
    /**
     * @return the indenter
     */
    public HemlIndenter getIndenter() {
        return indenter;
    }

    
    /**
     * @param nextHasNewLine the nextHasNewLine to set
     */
    public void setNextHasNewLine(boolean nextHasNewLine) {
        this.nextHasNewLine = nextHasNewLine;
    }
    
    /**
     * @return the nextHasNewLine
     */
    public boolean isNextHasNewLine() {
        return nextHasNewLine;
    }
}
