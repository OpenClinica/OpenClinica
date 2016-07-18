/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.domain.technicaladmin;

import javax.persistence.*;
import java.util.Date;

/**
 * <p>
 * Does not extend any Super Domain class because it does not follow OpenClinica convention. This table is managed via LiquiBase
 * </p>
 * 
 * @author Krikor Krumlian
 */
@Entity
@Table(name = "databasechangelog")
@IdClass(DatabaseChangeLogBeanPk.class)
public class DatabaseChangeLogBean {

    private String id;
    private String author;
    private String fileName;
    private Date dataExecuted;
    private String md5Sum;
    private String description;
    private String comments;
    private String tag;
    private String liquibase;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    @Column(name = "author")
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Id
    @Column(name = "filename")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Column(name = "dateexecuted")
    public Date getDataExecuted() {
        return dataExecuted;
    }

    public void setDataExecuted(Date dataExecuted) {
        this.dataExecuted = dataExecuted;
    }

    @Column(name = "md5sum")
    public String getMd5Sum() {
        return md5Sum;
    }

    public void setMd5Sum(String md5Sum) {
        this.md5Sum = md5Sum;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "comments")
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    @Column(name = "tag")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Column(name = "liquibase")
    public String getLiquibase() {
        return liquibase;
    }

    public void setLiquibase(String liquibase) {
        this.liquibase = liquibase;
    }
}