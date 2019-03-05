package org.akaza.openclinica.domain.datamap;

import org.akaza.openclinica.dao.hibernate.CryptoConverter;
import org.akaza.openclinica.domain.DataMapDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

@Entity
@Table(name = "study_subject_detail")
@GenericGenerator(name = "id-generator", strategy = "native", parameters = {@Parameter(name = "sequence_name", value = "study_subject_detail_id_seq")})
public class StudySubjectDetail extends DataMapDomainObject {

    private Integer id;
    private StudySubject studySubject;
    private String firstName;
    private String firstNameForSearchUse;
    private String lastName;
    private String lastNameForSearchUse;
    private String email;
    private String phone;
    private String identifier;
    private String identifierForSearchUse;


    public StudySubjectDetail() {
    }

    @Override
    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue(generator = "id-generator")
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @OneToOne
    @JoinColumn(name = "study_subject_id")
    public StudySubject getStudySubject() {
        return studySubject;
    }

    public void setStudySubject(StudySubject studySubject) {
        this.studySubject = studySubject;
    }

    @Column(name = "first_name")
    @Convert(converter = CryptoConverter.class)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Convert(converter = CryptoConverter.class)
    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Convert(converter = CryptoConverter.class)
    @Column(name = "email")
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Convert(converter = CryptoConverter.class)
    @Column(name = "phone")
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    @Convert(converter = CryptoConverter.class)
    @Column(name = "identifier")
    public String getIdentifier() {
        return identifier;
    }


    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Convert(converter = CryptoConverter.class)
    @Column(name = "first_name_for_search_use")
    public String getFirstNameForSearchUse() {
        return firstNameForSearchUse;
    }


    public void setFirstNameForSearchUse(String firstNameForSearchUse) {
        this.firstNameForSearchUse = firstNameForSearchUse;
    }

    @Convert(converter = CryptoConverter.class)
    @Column(name = "last_name_for_search_use")
    public String getLastNameForSearchUse() {
        return lastNameForSearchUse;
    }


    public void setLastNameForSearchUse(String lastNameForSearchUse) {
        this.lastNameForSearchUse = lastNameForSearchUse;
    }

    @Convert(converter = CryptoConverter.class)
    @Column(name = "identifier_for_search_use")
    public String getIdentifierForSearchUse() {
        return identifierForSearchUse;
    }


    public void setIdentifierForSearchUse(String identifierForSearchUse) {
        this.identifierForSearchUse = identifierForSearchUse;
    }
}
