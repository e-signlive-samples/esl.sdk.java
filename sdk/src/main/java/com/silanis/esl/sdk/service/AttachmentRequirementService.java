package com.silanis.esl.sdk.service;

import com.silanis.esl.api.model.RequirementStatus;
import com.silanis.esl.api.model.Role;
import com.silanis.esl.sdk.*;
import com.silanis.esl.sdk.internal.*;
import com.silanis.esl.sdk.internal.converter.SignerConverter;

import java.util.UUID;

/**
 * The AttachmentRequirementService class provides methods to help create attachments for signers.
 */
public class AttachmentRequirementService {

    private UrlTemplate template;
    private RestClient client;

    public AttachmentRequirementService(RestClient restClient, String baseUrl) {
        this.client = restClient;
        template = new UrlTemplate(baseUrl);
    }

    /**
     * Sender accepts signer's attachment requirement.
     *
     * @param packageId      the package ID
     * @param signer         the signer who uploaded the attachment
     * @param attachmentName
     */
    public void acceptAttachment(PackageId packageId, Signer signer, String attachmentName) {
        String path = template.urlFor(UrlTemplate.SIGNER_PATH)
                .replace("{packageId}", packageId.getId())
                .replace("{roleId}", signer.getId())
                .build();

        signer.getAttachmentRequirement().get(attachmentName).setSenderComment("");
        signer.getAttachmentRequirement().get(attachmentName).setStatus(com.silanis.esl.sdk.RequirementStatus.COMPLETE);

        Role apiPayload = new SignerConverter(signer).toAPIRole(UUID.randomUUID().toString().replace("-", ""));

        try {
            String json = Serialization.toJson(apiPayload);
            client.put(path, json);
        } catch (RequestException e){
            throw new EslServerException( "Could not accept attachment for signer.", e);
        } catch (Exception e) {
            throw new EslException("Could not accept attachment for signer." + " Exception: " + e.getMessage());
        }
    }

    /**
     * Sender rejects signer's attachment requirement with a comment.
     *
     * @param packageId      the package ID
     * @param signer         the signer who uploaded the attachment
     * @param attachmentName
     * @param senderComment  the sender's rejection comment
     */
    public void rejectAttachment(PackageId packageId, Signer signer, String attachmentName, String senderComment) {
        String path = template.urlFor(UrlTemplate.SIGNER_PATH)
                .replace("{packageId}", packageId.getId())
                .replace("{roleId}", signer.getId())
                .build();

        signer.getAttachmentRequirement().get(attachmentName).setSenderComment(senderComment);
        signer.getAttachmentRequirement().get(attachmentName).setStatus(com.silanis.esl.sdk.RequirementStatus.REJECTED);

        Role apiPayload = new SignerConverter(signer).toAPIRole(UUID.randomUUID().toString().replace("-", ""));

        try {
            String json = Serialization.toJson(apiPayload);
            client.put(path, json);
        } catch (RequestException e){
            throw new EslServerException( "Could not reject attachment for signer.", e);
        } catch (Exception e) {
            throw new EslException("Could not reject attachment for signer." + " Exception: " + e.getMessage());
        }
    }

    /**
     * Sender downloads the attachment.
     *
     * @param packageId    the package ID
     * @param attachmentId the attachment's ID
     * @return
     */
    public byte[] downloadAttachment(PackageId packageId, String attachmentId) {
        String path = template.urlFor(UrlTemplate.ATTACHMENT_REQUIREMENT_PATH)
                .replace("{packageId}", packageId.getId())
                .replace("{attachmentId}", attachmentId)
                .build();

        try {
            return client.getBytes(path);
        } catch (RequestException e){
            throw new EslServerException( "Could not download the pdf attachment.", e);
        } catch (Exception e) {
            throw new EslException("Could not download the pdf attachment." + " Exception: " + e.getMessage());
        }
    }

}
