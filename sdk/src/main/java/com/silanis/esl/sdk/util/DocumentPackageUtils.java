package com.silanis.esl.sdk.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.text.TextPositionStripper;

import com.silanis.esl.sdk.Document;
import com.silanis.esl.sdk.DocumentPackage;
import com.silanis.esl.sdk.Signature;
import com.silanis.esl.sdk.TextAnchor;
import com.silanis.esl.sdk.TextAnchorPosition;

public class DocumentPackageUtils {
	
	private class StrippedPDFPage {
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

	public DocumentPackage convertTextAnchorToXY(DocumentPackage pkg) throws IOException{
		for(Document document : pkg.getDocuments()){
			byte[] content = document.getContent();
			PDDocument pdfDocument = PDDocument.load(content);
			
			TextPositionStripper stripper = new TextPositionStripper();
			ArrayList<StrippedPDFPage> pages = new ArrayList<StrippedPDFPage>(pdfDocument.getNumberOfPages());
			for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
				stripper.setStartPage(i);
				stripper.setEndPage(i);
				String text = stripper.getText(pdfDocument);
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
					TextPosition xyPosition = textAnchorCoordinateExtract(pages, anchorText, position, character, occurrence);
					float x = xyPosition.getXDirAdj() + xOffset;
					float charWidth = xyPosition.getWidthDirAdj();
					float y = xyPosition.getYDirAdj() + yOffset;
					float charHeight = xyPosition.getHeightDir();
					
					signature.setWidth(width);
					signature.setHeight(height);
					
					if(position == TextAnchorPosition.BOTTOMLEFT){
						signature.setX(x);
						signature.setY(y+charHeight);
					}
					else if(position == TextAnchorPosition.BOTTOMRIGHT){
						signature.setX(x+charWidth);
						signature.setY(y+charHeight);
					}
					else if(position == TextAnchorPosition.TOPLEFT){
						signature.setX(x);
						signature.setY(y);
					}
					else {
						//TextAnchorPosition.TOPRIGHT
						signature.setX(x+charWidth);
						signature.setY(y);
					}
				}
				//we remove the eSignLive text anchor
				textAnchor = null;
			}
			//we disable extraction
			document.setExtraction(false);
		}
		return pkg;
	}
	
	//Get the XY coordinates per text anchor
	private TextPosition textAnchorCoordinateExtract(ArrayList<StrippedPDFPage> pages, String anchorText, TextAnchorPosition anchorPosition, int character, int occurrence) throws IOException{
		
		ArrayList<TextPosition> coordinates = new ArrayList<TextPosition>();
		for(StrippedPDFPage page: pages){
			Pattern pattern = Pattern.compile(anchorText);
			Matcher matcher = pattern.matcher(page.getPageOfText());
			while(matcher.find()){
				int startIndex = matcher.start();
				if(anchorPosition == TextAnchorPosition.BOTTOMLEFT || anchorPosition == TextAnchorPosition.TOPLEFT){
					startIndex = matcher.start();
				}
				else { 
					//this assumes BOTTOMRIGHT OR TOPRIGHT
					startIndex = matcher.end();
				}
				
				ArrayList<TextPosition> textPositionDetails = page.getTextPositionDetails();
				TextPosition position = textPositionDetails.get(startIndex+character);
				
				coordinates.add(position);
			}
		}

		TextPosition coordinate = coordinates.get(occurrence);
		return coordinate;
	}
}
