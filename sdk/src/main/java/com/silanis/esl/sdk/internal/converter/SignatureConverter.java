package com.silanis.esl.sdk.internal.converter;

import com.silanis.esl.api.model.Approval;
import com.silanis.esl.api.model.Field;
import com.silanis.esl.api.model.Role;
import com.silanis.esl.sdk.*;
import com.silanis.esl.sdk.builder.SignatureBuilder;

/**
 * User: jessica
 * Date: 20/11/13
 * Time: 3:42 PM
 *
 * Converter between SDK signature and API signature.
 */
public class SignatureConverter {

    private com.silanis.esl.sdk.Signature sdkSignature = null;
    private com.silanis.esl.api.model.Approval apiApproval = null;
    private com.silanis.esl.api.model.Package apiPackage = null;

    /**
     * Construct with API objects.
     */
    public SignatureConverter(com.silanis.esl.api.model.Approval apiApproval, com.silanis.esl.api.model.Package apiPackage) {
        this.apiApproval = apiApproval;
        this.apiPackage = apiPackage;
    }

    /**
     * Construct with SDK object.
     */
    public SignatureConverter(com.silanis.esl.sdk.Signature sdkSignature) {
        this.sdkSignature = sdkSignature;
    }

    /**
     * Convert from API approval to SDK signature.
     *
     * @return an SDK Signature object.
     *
     */
    public com.silanis.esl.sdk.Signature toSDKSignature() {

        if (apiPackage == null || apiApproval == null) {
            return sdkSignature;
        }

        SignatureBuilder signatureBuilder = null;

        for ( Role role : apiPackage.getRoles() ) {
            if ( role.getId().equals( apiApproval.getRole() ) ) {
                if ( isPlaceholder(role)) {
                    signatureBuilder = SignatureBuilder.signatureFor(new Placeholder(role.getId()));
                }
                else if ( isGroupRole(role) ){
                    signatureBuilder = SignatureBuilder.signatureFor(new GroupId(role.getSigners().get(0).getGroup().getId()));
                }
                else{
                    signatureBuilder = SignatureBuilder.signatureFor(role.getSigners().get(0).getEmail());
                }
            }
        }

        if(apiApproval.getId() != null){
            signatureBuilder.withId(new SignatureId(apiApproval.getId()));
        }

        signatureBuilder.withName( apiApproval.getName() );

        com.silanis.esl.api.model.Field apiSignatureField = null;
        for ( com.silanis.esl.api.model.Field apiField : apiApproval.getFields() ) {
            if (apiField.getType().equals(FieldType.SIGNATURE.getApiValue()) ) {
                apiSignatureField = apiField;
            } else {
                signatureBuilder.withField( new FieldConverter(apiField).toSDKField() );
            }

        }
        if ( apiSignatureField == null ) {
            signatureBuilder.withStyle( com.silanis.esl.sdk.SignatureStyle.ACCEPTANCE );
            signatureBuilder.withSize( 0, 0 );

        } else {
            signatureBuilder.withStyle( com.silanis.esl.sdk.SignatureStyle.fromAPIFieldSubType(apiSignatureField.getSubtype()) );
            if ( apiSignatureField.getPage() != null )
                signatureBuilder.onPage( apiSignatureField.getPage() );

            if ( apiSignatureField.getLeft() != null && apiSignatureField.getTop() != null )
                signatureBuilder.atPosition( apiSignatureField.getLeft(), apiSignatureField.getTop() );

            if ( apiSignatureField.getHeight() != null && apiSignatureField.getWidth() != null )
                signatureBuilder.withSize( apiSignatureField.getWidth(), apiSignatureField.getHeight() );

            if ( apiSignatureField.evalExtract() ) {
                signatureBuilder.withPositionExtracted();
            }
        }

        Signature signature = signatureBuilder.build();
        if(null != apiApproval.getAccepted())
            signature.setAccepted(apiApproval.getAccepted());

        return signature;
    }


    /**
     * Convert from SDK signature to API approval.
     *
     * @return an API approval object.
     *
     */
    public Approval toAPIApproval() {

        if (sdkSignature == null) {
            return apiApproval;
        }

        Approval result = new Approval();

        if(sdkSignature.getId() != null){
            result.setId(sdkSignature.getId().getId());
        }

        result.addField(getAPIFieldFromSignature());

        for ( com.silanis.esl.sdk.Field field : sdkSignature.getFields() ) {
            result.addField( ConversionService.convert( field ) );
        }

        return result;
    }

    /**
     * To see if the role is a place holder.
     *
     * @param role
     * @return a boolean value indicating if the role is a place holder.
     */
    private static boolean isPlaceholder(Role role) {
        return role.getSigners().isEmpty();
    }

    private static boolean isGroupRole(Role role) {
        return role.getSigners().get(0).getGroup() != null;
    }

    /**
     * Convert the signature's non-field (ie. except the fields linked to this signature) properties to a field.
     *
     * @return a field.
     */
    private Field getAPIFieldFromSignature() {
        Field result = new Field();

        result.setPage(sdkSignature.getPage());
        result.setExtract(sdkSignature.isExtraction());
        if ( sdkSignature.getName() != null ) {
            result.setName(sdkSignature.getName());
        }

        if (!sdkSignature.isExtraction()) {
            result.setTop( sdkSignature.getY() );
            result.setLeft( sdkSignature.getX() );
            result.setWidth( sdkSignature.getWidth() );
            result.setHeight( sdkSignature.getHeight() );
        }

        if (sdkSignature.getTextAnchor() != null ) {
            result.setExtractAnchor( new TextAnchorConverter(sdkSignature.getTextAnchor()).toAPIExtractAnchor() );
        }

        result.setType(FieldType.SIGNATURE.getApiValue());
        result.setSubtype( getSignatureSubtype() );

        return result;
    }

    /**
     * Convert a signature style to be a field sub type.
     *
     * @return a field sub type.
     */
    private String getSignatureSubtype() {
        if (sdkSignature == null) {
            return null;
        }
        return sdkSignature.getStyle().getApiValue();
    }
    
}
