package org.apache.pdfbox.text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class TextPositionStripper extends PDFTextStripper
{
    /**
     * Instantiate a new TextPosition object.
     *
     * @throws IOException If there is an error loading the properties.
     */
    public TextPositionStripper() throws IOException
    {
    }

    private ArrayList<TextPosition> textPositionDetails = new ArrayList<TextPosition>();
    private StringBuilder sb = new StringBuilder();
    
    /**
     * Write full TextPosition information
     */
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException
    {    	
    	textPositionDetails.addAll(textPositions);
    	for(TextPosition textPosition : textPositions) {
    		sb.append(textPosition.getUnicode());
    	}
    	writeString(string);
    }
    
    public ArrayList<TextPosition> getTextPositionDetails() {
		return textPositionDetails;
	}
    
    public String getUnicodeText(){
    	return sb.toString();
    }
}