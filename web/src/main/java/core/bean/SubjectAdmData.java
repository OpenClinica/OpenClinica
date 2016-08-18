package core.bean;

import java.util.Date;

/**
 * Información administrativa del sujeto.
 *
 * @author SJM.
 */
public class SubjectAdmData {

	private String name;
	private String familyName;
	private String cipCode;
	private Date birth;
	private String address;

	public SubjectAdmData(
		String name,
		String familyName,
		String cipCode,
		Date birth,
		String address) {

		this.name =
			name;
		this.familyName =
			familyName;
		this.cipCode =
			cipCode;
		this.birth =
			birth;
		this.address =
			address;
	}

	public String getName() {
		return name;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getCipCode() {
		return cipCode;
	}

	public Date getBirth() {
		return birth;
	}

	public String getAddress() {
		return address;
	}
}
