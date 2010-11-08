/**
 * 
 */
package org.akaza.openclinica.domain.history;

import java.util.Date;

import javax.persistence.*;

import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.domain.AbstractAuditableMutableDomainObject;
import org.akaza.openclinica.domain.Status;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

/**
 * @author jnyayapathi
 *
 */
@Entity
@Table(name = "history_log",  uniqueConstraints = {@UniqueConstraint(columnNames={"id"})}
)
@GenericGenerator(name = "id-generator", strategy = "native", parameters = { @Parameter(name = "sequence", value = "history_log_id_seq") } )
public class HistoryLogBean extends AbstractAuditableMutableDomainObject{

	private Integer id;
	private String visited_link;
	
	@Column(name="id",insertable= false, updatable = false)
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	 @Transient
	    public Date getUpdatedDate() {
	        return updatedDate;
	    }

	 public void setUpdatedDate(Date updatedDate)
	 {
	 }
	    
	 @Transient
	 public void getUpdater(UserAccountBean updater) {
	    
	    }
	 @Transient
	 public Status getStatus() {
	        if (status != null) {
	            return status;
	        } else
	            return Status.AVAILABLE;
	    }

	 public void setVisited_link(String visited_link){
		 this.visited_link = visited_link;
	 }
	 @Column(name="visited_link")
	 public String getVisited_link(){
		 return visited_link;
	 }
	 
	 
}
