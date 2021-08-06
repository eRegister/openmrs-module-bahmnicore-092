package org.bahmni.module.bahmnicore.web.v1_0.contract;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class PatientContract {

	private String uuid;
	private Date birthDate;
	private String extraIdentifiers;
	private int personId;
	private String identifier;

	private String constituency;
	private String village;
	private String district;

	private String givenName;
	private String middleName;
	private String familyName;
	private String gender;
	private String age;
	private String activeVisitUuid;
	private String customAttribute;
	private Object patientProgramAttributeValue;
	private Date deathDate;

	public String getAge() {
		if (birthDate == null)
			return null;

		// Use default end date as today.
		Calendar today = Calendar.getInstance();

		// If date given is after date of death then use date of death as end date
		if (getDeathDate() != null && today.getTime().after(getDeathDate())) {
			today.setTime(getDeathDate());
		}

		Calendar bday = Calendar.getInstance();
		bday.setTime(birthDate);

		int age = today.get(Calendar.YEAR) - bday.get(Calendar.YEAR);

		// Adjust age when today's date is before the person's birthday
		int todaysMonth = today.get(Calendar.MONTH);
		int bdayMonth = bday.get(Calendar.MONTH);
		int todaysDay = today.get(Calendar.DAY_OF_MONTH);
		int bdayDay = bday.get(Calendar.DAY_OF_MONTH);

		if (todaysMonth < bdayMonth) {
			age--;
		} else if (todaysMonth == bdayMonth && todaysDay < bdayDay) {
			// we're only comparing on month and day, not minutes, etc
			age--;
		}

		return Integer.toString(age);
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	@JsonSerialize(using=JsonDateSerializer.class)
	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public void setAge(String age){
		this.age = age;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getMiddleName() {
		return middleName;
	}

	public void setMiddleName(String middleName) {
		this.middleName = middleName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public Date getDeathDate() {
		return deathDate;
	}

	public void setDeathDate(Date deathDate) {
		this.deathDate = deathDate;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getActiveVisitUuid() {
		return activeVisitUuid;
	}

	public void setActiveVisitUuid(String activeVisitUuid) {
		this.activeVisitUuid = activeVisitUuid;
	}

	public String getCustomAttribute() {
		return customAttribute;
	}

	public void setCustomAttribute(String customAttribute) {
		this.customAttribute = customAttribute;
	}

	public Object getPatientProgramAttributeValue() {
		return patientProgramAttributeValue;
	}

	public void setPatientProgramAttributeValue(Object patientProgramAttributeValue) {
		this.patientProgramAttributeValue = patientProgramAttributeValue;
	}

	public String getExtraIdentifiers() {
		return extraIdentifiers;
	}

	public void setExtraIdentifiers(String extraIdentifiers) {
		this.extraIdentifiers = extraIdentifiers;
	}

	public int getPersonId() {
		return personId;
	}

	public void setPersonId(int personId) {
		this.personId = personId;
	}

	public String getConstituency() { return this.constituency; }
	public void setConstituency(String constituency) { this.constituency = constituency; }

	public String getVillage(){ return this.village; }
	public void setVillage(String village){ this.village = village; }

	public String getDistrict() { return this.district; }
	public void setDistrict(String district){ this.district = district; }

	/**
	 * Used to serialize Java.util.Date, which is not a common JSON
	 * type, so we have to create a custom serialize method;
	 */
	@Component
	public static  class JsonDateSerializer extends JsonSerializer<Date> {
		private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		@Override
		public void serialize(Date date, JsonGenerator gen, SerializerProvider provider)
				throws IOException {
			String formattedDate = dateFormat.format(date);
			gen.writeString(formattedDate);
		}
	}
}