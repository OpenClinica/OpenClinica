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
    private String lastName;
    private String email;
    private String phone;


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
}
