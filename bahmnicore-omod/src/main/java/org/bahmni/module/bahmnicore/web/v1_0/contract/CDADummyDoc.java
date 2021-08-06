package org.bahmni.module.bahmnicore.web.v1_0.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by Teboho on 2019-03-22.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CDADummyDoc {

	private String message;
	private String documentId;
	private boolean isSuccessful = false;

	public CDADummyDoc(){

	}

	public CDADummyDoc(String message, String documentId, boolean isSuccessful){
		this.message = message;
		this.documentId = documentId;
		this.isSuccessful = isSuccessful;
	}

	public String getMessage(){ return message; }
	public void setMessage (String message ){ this.message = message; }

	public String getDocumentId(){ return this.documentId; }
	public void setDocumentId (String documentId ){ this.documentId = documentId; }

	public boolean isSuccessful() { return this.isSuccessful; }
	public void setIsSuccesful(boolean isSuccessful){ this.isSuccessful = isSuccessful; }
}
