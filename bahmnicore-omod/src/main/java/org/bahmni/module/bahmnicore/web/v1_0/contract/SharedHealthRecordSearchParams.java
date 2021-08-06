package org.bahmni.module.bahmnicore.web.v1_0.contract;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.emrapi.utils.CustomJsonDateDeserializer;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

/**
 * Created by Teboho on 2019-03-19.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SharedHealthRecordSearchParams {
	private String patientUuid;
	private String patientIdentifier;
	private Date fromDate;
	private Date toDate;
	private Integer startIndex;
	private String loginLocationUuid;

	private static final String DATE_PATTERN = "yyyy-MM-dd";

	public SharedHealthRecordSearchParams(){

	}

	public SharedHealthRecordSearchParams(String patientUuid, String patientIdentifier, Date fromDate, Date toDate,
			String startIndex, String loginLocationUuid){
		this.patientUuid = patientUuid;
		this.patientIdentifier = patientIdentifier;
		this.fromDate = fromDate;
		this.toDate = toDate;
		this.startIndex = Integer.parseInt(startIndex);
		this.loginLocationUuid = loginLocationUuid;
	}

	public SharedHealthRecordSearchParams(RequestContext context){
		this.patientUuid = context.getParameter("patientUuid");
		this.patientIdentifier = context.getParameter("patientIdentifier");
		this.fromDate = parseDate(context.getParameter("fromDate"));
		this.toDate = parseDate(context.getParameter("toDate"));
		this.startIndex = Integer.parseInt(context.getParameter("startIndex"));
		this.loginLocationUuid = context.getParameter("loginLocationUuid");
	}

	private Date parseDate(String date) {
		DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
		try {
			Date newDate = dateFormat.parse(date);
			return newDate;
		} catch (ParseException ex){
			return null;
		}
	}

	public String getPatientUuid(){ return this.patientUuid; }
	public void setPatientUuid(String patientUuid){ this.patientUuid = patientUuid; }

	public String getPatientIdentifier(){ return this.patientIdentifier; }
	public void setPatientIdentifier(String patientIdentifier){ this.patientIdentifier = patientIdentifier; }

	@JsonSerialize(using = CustomJsonDateSerializer.class)
	public Date getFromDate(){ return this.fromDate; }

	@JsonDeserialize(using = CustomJsonDateDeserializer.class)
	public void setFromDate(Date fromDate){ this.fromDate = fromDate; }

	@JsonSerialize(using = CustomJsonDateSerializer.class)
	public Date getToDate(){ return this.toDate; }

	@JsonDeserialize(using = CustomJsonDateDeserializer.class)
	public void setToDate(Date toDate){ this.toDate = toDate; }

	public Integer getStartIndex(){ return this.startIndex; }
	public void setStartIndex(Integer startIndex){ this.startIndex = startIndex; }

	public String getLoginLocationUuid(){ return this.loginLocationUuid; }
	public void setLoginLocationUuid(String patientUuid){ this.loginLocationUuid = loginLocationUuid; }
}
