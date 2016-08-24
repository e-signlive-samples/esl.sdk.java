package com.silanis.esl.sdk.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.text.TextPositionStripper;

import com.silanis.esl.sdk.Document;
import com.silanis.esl.sdk.DocumentPackage;
import com.silanis.esl.sdk.Field;
import com.silanis.esl.sdk.Signature;
import com.silanis.esl.sdk.TextAnchor;
import com.silanis.esl.sdk.TextAnchorPosition;

public class DocumentPackageUtils {
	protected static final Logger log = Logger.getLogger(DocumentPackageUtils.class.getName());
	private static float ESL_SCALE = 1.305f;
	
	private static class StrippedPDFPage {
		private int eSLPageNumber = 0;
		private String pageOfText = "";
		private ArrayList<TextPosition> textPositionDetails = new ArrayList<TextPosition>();
		
		public StrippedPDFPage(int eSLPageNumber, String pageOfText, ArrayList<TextPosition> textPositionDetails) {
			this.eSLPageNumber = eSLPageNumber;
			this.pageOfText = pageOfText;
			this.textPositionDetails = textPositionDetails;
		}
		
		public int geteSLPageNumber() {
			return eSLPageNumber;
		}
		
		public String getPageOfText() {
			return pageOfText;
		}
		
		public ArrayList<TextPosition> getTextPositionDetails() {
			return textPositionDetails;
		}
	}

	public static DocumentPackage convertTextAnchorToXY(DocumentPackage pkg) {
		try {
			for(Document document : pkg.getDocuments()){
				byte[] content = document.getContent();
				
				PDDocument pdfDocument = PDDocument.load(content);
				
				TextPositionStripper stripper = new TextPositionStripper();
				ArrayList<StrippedPDFPage> pages = new ArrayList<StrippedPDFPage>(pdfDocument.getNumberOfPages());
				for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
					stripper.setStartPage(i);
					stripper.setEndPage(i);
					stripper.getText(pdfDocument); //to bootstrap the parsing of the document
					String text = stripper.getUnicodeText();
					ArrayList<TextPosition> textPositionDetails = stripper.getTextPositionDetails();
					StrippedPDFPage page = new StrippedPDFPage(i, text, textPositionDetails);
					pages.add(page);
				}
				
				for(Signature signature : document.getSignatures()){
					TextAnchor textAnchor = signature.getTextAnchor();
					if(textAnchor != null){
						String anchorText = textAnchor.getAnchorText();
						int character = textAnchor.getCharacter();
						int height = textAnchor.getHeight();
						int occurrence = textAnchor.getOccurrence();
						TextAnchorPosition position = textAnchor.getPosition();
						int width = textAnchor.getWidth();
						int xOffset = textAnchor.getXOffset();
						int yOffset = textAnchor.getYOffset();
						
						//We set the signature x-y coordinates by performing a client side extraction using PDFBox
						TextPosition xyPosition = textAnchorCoordinateExtract(pages, anchorText, character, occurrence);
						double x = Math.floor(xyPosition.getXDirAdj()) + xOffset;
						double charWidth = Math.floor(xyPosition.getWidthDirAdj());
						double y = Math.floor(xyPosition.getYDirAdj()) + yOffset;
						double charHeight = Math.floor(xyPosition.getFontSize());
						
						signature.setWidth(width);
						signature.setHeight(height);
						
						if(position == TextAnchorPosition.BOTTOMLEFT){
							signature.setX(x*ESL_SCALE);
							signature.setY(y*ESL_SCALE);
						}
						else if(position == TextAnchorPosition.BOTTOMRIGHT){
							signature.setX((x+charWidth)*ESL_SCALE);
							signature.setY(y*ESL_SCALE);
						}
						else if(position == TextAnchorPosition.TOPLEFT){
							signature.setX(x*ESL_SCALE);
							signature.setY((y-charHeight)*ESL_SCALE);
						}
						else {
							//TextAnchorPosition.TOPRIGHT
							signature.setX((x+charWidth)*ESL_SCALE);
							signature.setY((y-charHeight)*ESL_SCALE);
						}
						log.fine("position of anchor '"+anchorText+"' for character '"+xyPosition.getUnicode()+"' positionned "+position.toString()+" at (" + signature.getX() +", "+ + signature.getY()+") where character width="+charWidth+"*"+ESL_SCALE);	
					}
					//we remove the eSignLive text anchor
					signature.setTextAnchor(null);
					
					for(Field field: signature.getFields()) {
						TextAnchor fieldTextAnchor = field.getTextAnchor();
						if(textAnchor != null){
							String anchorText = fieldTextAnchor.getAnchorText();
							int character = fieldTextAnchor.getCharacter();
							int height = fieldTextAnchor.getHeight();
							int occurrence = fieldTextAnchor.getOccurrence();
							TextAnchorPosition position = fieldTextAnchor.getPosition();
							int width = fieldTextAnchor.getWidth();
							int xOffset = fieldTextAnchor.getXOffset();
							int yOffset = fieldTextAnchor.getYOffset();
							
							//We set the signature x-y coordinates by performing a client side extraction using PDFBox
							TextPosition xyPosition = textAnchorCoordinateExtract(pages, anchorText, character, occurrence);
							double x = Math.floor(xyPosition.getXDirAdj()) + xOffset;
							double charWidth = Math.floor(xyPosition.getWidthDirAdj());
							double y = Math.floor(xyPosition.getYDirAdj()) + yOffset;
							double charHeight = Math.floor(xyPosition.getFontSize());
							
							field.setWidth(width);
							field.setHeight(height);
							
							if(position == TextAnchorPosition.BOTTOMLEFT){
								field.setX(x*ESL_SCALE);
								field.setY(y*ESL_SCALE);
							}
							else if(position == TextAnchorPosition.BOTTOMRIGHT){
								field.setX((x+charWidth)*ESL_SCALE);
								field.setY(y*ESL_SCALE);
							}
							else if(position == TextAnchorPosition.TOPLEFT){
								field.setX(x*ESL_SCALE);
								field.setY((y-charHeight)*ESL_SCALE);
							}
							else {
								//TextAnchorPosition.TOPRIGHT
								field.setX((x+charWidth)*ESL_SCALE);
								field.setY((y-charHeight)*ESL_SCALE);
							}
							log.fine("position of anchor '"+anchorText+"' for character '"+xyPosition.getUnicode()+"' positionned "+position.toString()+" at (" + field.getX() +", "+ + field.getY()+") where character width="+charWidth+"*"+ESL_SCALE);	
						}
						field.setTextAnchor(null);
					}
				}
				
				//we disable extraction
				document.setExtraction(false);
				pdfDocument.close();
			}
			return pkg;
		}
		catch(IOException ex){
			throw new RuntimeException(ex);
		}
	}
	
	//Get the XY coordinates per text anchor
	private static TextPosition textAnchorCoordinateExtract(ArrayList<StrippedPDFPage> pages, String anchorText, int character, int occurrence) {
		
		ArrayList<TextPosition> coordinates = new ArrayList<TextPosition>();
		for(StrippedPDFPage page: pages){
			Pattern pattern = Pattern.compile(anchorText);
			Matcher matcher = pattern.matcher(page.getPageOfText());
			while(matcher.find()){
				log.fine("found text anchor: "+anchorText);
				int startIndex = matcher.start();
				
				ArrayList<TextPosition> textPositionDetails = page.getTextPositionDetails();
				TextPosition position = textPositionDetails.get(startIndex+character);
				log.fine("position of character '"+position.getUnicode()+"' at (" + position.getXDirAdj() +", "+ + position.getYDirAdj()+")");
				
				coordinates.add(position);
			}
		}

		TextPosition coordinate = coordinates.get(occurrence);
		return coordinate;
	}
}
