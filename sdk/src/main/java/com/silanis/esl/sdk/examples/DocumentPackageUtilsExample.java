package com.silanis.esl.sdk.examples;

import static com.silanis.esl.sdk.builder.DocumentBuilder.newDocumentWithName;
import static com.silanis.esl.sdk.builder.PackageBuilder.newPackageNamed;
import static com.silanis.esl.sdk.builder.SignatureBuilder.signatureFor;
import static com.silanis.esl.sdk.builder.SignerBuilder.newSignerWithEmail;

import java.io.InputStream;

import com.silanis.esl.sdk.DocumentPackage;
import com.silanis.esl.sdk.DocumentType;
import com.silanis.esl.sdk.SignatureId;
import com.silanis.esl.sdk.TextAnchorPosition;
import com.silanis.esl.sdk.builder.TextAnchorBuilder;
import com.silanis.esl.sdk.util.DocumentPackageUtils;

public class DocumentPackageUtilsExample extends SDKSample {
	
	private InputStream documentInputStream1;

    public static final String DOCUMENT_NAME = "Document With Anchors";
    public static final int FIELD_WIDTH = 150;
    public static final int FIELD_HEIGHT = 40;

    public static void main( String... args ) {
        new DocumentPackageUtilsExample().run();
    }

    public DocumentPackageUtilsExample() {
        documentInputStream1 = this.getClass().getClassLoader().getResourceAsStream( "document-for-pdfbox-anchor-extraction.pdf" );
    }

	@Override
	void execute() {
		DocumentPackage superDuperPackage = newPackageNamed(getPackageName())
                .withSigner(newSignerWithEmail(email1)
                        .withRoleId( "Signer1" )
                        .withFirstName( "John" )
                        .withLastName( "Smith" ) )
                .withDocument( newDocumentWithName( DOCUMENT_NAME )
                        .fromStream( documentInputStream1, DocumentType.PDF )
                        .enableExtraction()
                        .withSignature(signatureFor( email1 )
                        		.withId(new SignatureId("BOTTOMRIGHT"))
                                .withPositionAnchor( TextAnchorBuilder.newTextAnchor( "BOTTOMRIGHT" )
                                        .atPosition( TextAnchorPosition.BOTTOMRIGHT )
                                        .withSize( FIELD_WIDTH, FIELD_HEIGHT )
                                        .withOffset( 0, 0 )
                                        .withCharacter( 0 )
                                        .withOccurence( 0 ) ) )
                        .withSignature(signatureFor( email1 )
                        		.withId(new SignatureId("BOTTOMLEFT"))
                                .withPositionAnchor( TextAnchorBuilder.newTextAnchor( "BOTTOMLEFT" )
                                        .atPosition( TextAnchorPosition.BOTTOMLEFT )
                                        .withSize( FIELD_WIDTH, FIELD_HEIGHT )
                                        .withOffset( 0, 0 )
                                        .withCharacter( 0 )
                                        .withOccurence( 0 ) ) )
                        .withSignature(signatureFor( email1 )
                        		.withId(new SignatureId("TOPRIGHT"))
                                .withPositionAnchor( TextAnchorBuilder.newTextAnchor( "TOPRIGHT" )
                                        .atPosition( TextAnchorPosition.TOPRIGHT )
                                        .withSize( FIELD_WIDTH, FIELD_HEIGHT )
                                        .withOffset( 0, 0 )
                                        .withCharacter( 0 )
                                        .withOccurence( 0 ) ) )
                        .withSignature(signatureFor( email1 )
                        		.withId(new SignatureId("TOPLEFT"))
                                .withPositionAnchor( TextAnchorBuilder.newTextAnchor( "TOPLEFT" )
                                        .atPosition( TextAnchorPosition.TOPLEFT )
                                        .withSize( FIELD_WIDTH, FIELD_HEIGHT )
                                        .withOffset( 0, 0 )
                                        .withCharacter( 0 )
                                        .withOccurence( 0 ) ) )
                )
                .build();
		
		superDuperPackage = DocumentPackageUtils.convertTextAnchorToXY(superDuperPackage);

        packageId = eslClient.createPackage( superDuperPackage );
        eslClient.sendPackage( packageId );

        retrievedPackage = eslClient.getPackage( packageId );
	}
}
