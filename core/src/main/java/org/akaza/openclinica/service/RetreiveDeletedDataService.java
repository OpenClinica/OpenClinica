package org.akaza.openclinica.service;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.lang.reflect.Field;

import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.akaza.openclinica.bean.admin.CRFBean;
import org.akaza.openclinica.bean.core.ResponseType;
import org.akaza.openclinica.bean.core.Role;
import org.akaza.openclinica.bean.core.Status;
import org.akaza.openclinica.bean.core.SubjectEventStatus;
import org.akaza.openclinica.bean.core.UserType;
import org.akaza.openclinica.bean.login.StudyUserRoleBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import org.akaza.openclinica.bean.managestudy.StudyBean;
import org.akaza.openclinica.bean.managestudy.StudyEventBean;
import org.akaza.openclinica.bean.managestudy.StudyEventDefinitionBean;
import org.akaza.openclinica.bean.managestudy.StudySubjectBean;
import org.akaza.openclinica.bean.odmbeans.AuditLogsBean;
import org.akaza.openclinica.bean.odmbeans.StudyEventDefBean;
import org.akaza.openclinica.bean.submit.CRFVersionBean;
import org.akaza.openclinica.bean.submit.EventCRFBean;
import org.akaza.openclinica.bean.submit.ItemBean;
import org.akaza.openclinica.bean.submit.ItemDataBean;
import org.akaza.openclinica.bean.submit.ItemFormMetadataBean;
import org.akaza.openclinica.bean.submit.ItemGroupBean;
import org.akaza.openclinica.bean.submit.ItemGroupMetadataBean;
import org.akaza.openclinica.bean.submit.ResponseSetBean;
import org.akaza.openclinica.dao.admin.CRFDAO;
import org.akaza.openclinica.dao.hibernate.AuditLogEventDao;
import org.akaza.openclinica.dao.hibernate.AuthoritiesDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import org.akaza.openclinica.dao.login.UserAccountDAO;
import org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import org.akaza.openclinica.dao.managestudy.StudyDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import org.akaza.openclinica.dao.managestudy.StudyEventDefinitionDAO;
import org.akaza.openclinica.dao.managestudy.StudySubjectDAO;
import org.akaza.openclinica.dao.submit.CRFVersionDAO;
import org.akaza.openclinica.dao.submit.EventCRFDAO;
import org.akaza.openclinica.dao.submit.ItemDAO;
import org.akaza.openclinica.dao.submit.ItemDataDAO;
import org.akaza.openclinica.dao.submit.ItemFormMetadataDAO;
import org.akaza.openclinica.dao.submit.ItemGroupDAO;
import org.akaza.openclinica.dao.submit.ItemGroupMetadataDAO;
import org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import org.akaza.openclinica.domain.crfdata.DynamicsItemGroupMetadataBean;
import org.akaza.openclinica.domain.datamap.AuditLogEvent;
import org.akaza.openclinica.domain.datamap.AuditLogEventType;
import org.akaza.openclinica.domain.rule.RuleBean;
import org.akaza.openclinica.domain.rule.action.PropertyBean;
import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.user.AuthoritiesBean;
import org.akaza.openclinica.service.crfdata.BeanPropertyService;
import org.apache.log4j.spi.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.validation.DataBinder;
import org.springframework.validation.Errors;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Servlet for creating a user account.
 *
 * @author ssachs
 */
/**
 * @author joekeremian
 *
 */
/**
 * @author joekeremian
 *
 */
public class RetreiveDeletedDataService {

	protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

	DataSource ds;
	private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
	private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;

	EventDefinitionCRFDAO edcdao;
	UserAccountDAO udao;
	StudyDAO sdao;
	StudyEventDefinitionDAO seddao;
	StudySubjectDAO ssdao;
	StudyEventDAO sedao;
	EventCRFDAO ecdao;
	CRFVersionDAO cvdao;
	CRFDAO cdao;
	ItemDataDAO iddao;
	ItemDAO idao;
	ItemFormMetadataDAO ifmdao;
	AuthoritiesDao authoritiesDao;
	ItemGroupDAO igdao;
	ItemGroupMetadataDAO igmdao;
	AuditLogEventDao auditLogEventDao;

	public RetreiveDeletedDataService() {
	}

	public RetreiveDeletedDataService(DataSource ds, AuditLogEventDao auditLogEventDao) {
		this.ds = ds;
		this.auditLogEventDao = auditLogEventDao;
	}

	@SuppressWarnings("null")
	public void retrieveProcess(Integer studyEventId) throws IllegalArgumentException, IllegalAccessException {

		AuditLogEvent auditLog = new AuditLogEvent();
		AuditLogEventType auditLogEventType = new AuditLogEventType();
		auditLogEventType.setAuditLogEventTypeId(13);
		auditLog.setAuditLogEventType(auditLogEventType);
		auditLog.setStudyEventId(studyEventId);
		auditLog.setAuditTable("item_data");
		ArrayList<Object> objects = (ArrayList<Object>) getAuditLogEventDao().findByParam(auditLog);

		ArrayList<AuditLogEvent> auditLogEvents = new ArrayList<AuditLogEvent>();
		AuditLogEvent auditLogEvent;

		for (Object obj : objects) {
			Object[] row = (Object[]) obj;
			int eventCrfId = Integer.valueOf(row[0].toString());
			int eventCrfVersionId = Integer.valueOf(row[1].toString());
			cdao = new CRFDAO(ds);
			int crfId = cdao.findByVersionId(eventCrfVersionId).getId();

			auditLogEvent = new AuditLogEvent();
			auditLogEvent.setEventCrfId(eventCrfId);
			auditLogEvent.setEventCrfVersionId(eventCrfVersionId);
			auditLogEvent.setCrfId(crfId);
			auditLogEvent.setStudyEventId(studyEventId);

			auditLogEvents.add(auditLogEvent);
			System.out.println("eventCrfId: " + eventCrfId + "  eventCrfVersionId: " + eventCrfVersionId + "  crfId:  " + crfId
					+ "  studyEventId:  " + studyEventId);
		}

		if (auditLogEvents != null) {
			ArrayList<Integer> listOfCrfIds = new ArrayList<Integer>();
			for (AuditLogEvent auditEvent : auditLogEvents) {

				if (listOfCrfIds == null) {
					listOfCrfIds.add(auditEvent.getCrfId());
				} else if (!listOfCrfIds.contains(auditEvent.getCrfId())) {
					listOfCrfIds.add(auditEvent.getCrfId());
				}
			}
			System.out.println("List of CRF ID count:  " + listOfCrfIds.size());
			for (Integer individualCrfId : listOfCrfIds) {
				System.out.println("CRF ID in the List of CRFs  " + individualCrfId);
			}

			sedao = new StudyEventDAO(ds);
			StudyEventBean studyEventBean = (StudyEventBean) sedao.findByPK(studyEventId);
			UserAccountBean userAccountBean = new UserAccountBean();
			userAccountBean.setId(1);

			for (Integer crfId : listOfCrfIds) {
				Integer crfVersionsCount = 0;
				cvdao = new CRFVersionDAO(ds);
				ArrayList<CRFVersionBean> crfVersionBeans = cvdao.findAllByCRFId(crfId);
				crfVersionsCount = crfVersionBeans.size();
				int count = 0;
				for (CRFVersionBean crfVersionBean : crfVersionBeans) {
					count++;
					ecdao = new EventCRFDAO(ds);
					EventCRFBean eventCRFBean = ecdao.findByEventCrfVersion(studyEventBean, crfVersionBean);
					if (eventCRFBean != null) {
						System.out.println("For crfId= " + crfId + "  crfVersionId = " + crfVersionBean.getId()
								+ "    Event CRF exists in EventCRF Table " + eventCRFBean.getId() + "  for crfVersionId= "
								+ eventCRFBean.getCRFVersionId());

						loopListOfALRecords(auditLogEvents, eventCRFBean, crfId);
						break;
					} else if (eventCRFBean == null && crfVersionsCount == count) {
						System.out.println("For crfId= " + crfId + "    Event CRF does not exist");
						EventCRFBean eventCRF = createEventCRF(crfVersionBean, studyEventBean, userAccountBean);
						loopListOfALRecords(auditLogEvents, eventCRF, crfId);

					}
				}

			}
		}
	}

	// Loop List of item_data records AL table that requires to be updated its
	// fields
	private void loopListOfALRecords(ArrayList<AuditLogEvent> auditLogEvents, EventCRFBean eCrfBean, Integer crfId) {

		for (AuditLogEvent ale : auditLogEvents) {
			if (ale.getCrfId() == crfId) {
				AuditLogEvent ale1 = new AuditLogEvent();
				Integer ecId = ale.getEventCrfId();
				ale1.setEventCrfId(ecId);

				ale1.setAuditTable("item_data");
				ArrayList<AuditLogEvent> listOfEventCrfIdandRepeatKeyRecordsInAuditLogToBeUpdated = getAuditLogEventDao()
						.findByParamForEventCrf(ale1);
				updateEventCrfIdsInALEventCrfFieldandSetRepeatKeyTo1(listOfEventCrfIdandRepeatKeyRecordsInAuditLogToBeUpdated, eCrfBean,ecId);

				ale1.setAuditTable("event_crf");
				ArrayList<AuditLogEvent> listOfEntityIdRecordsInAuditLogTableToBeUpdated = getAuditLogEventDao().findByParamForEventCrf(
						ale1);
				updateEventCrfIdsInALEntityIdField(listOfEntityIdRecordsInAuditLogTableToBeUpdated, eCrfBean, ecId);

			}
		}

	}

	// method#4 update event_crf_Ids in entity_id column in Audit Log table
	// where audit_table=event_crf
	private void updateEventCrfIdsInALEntityIdField(ArrayList<AuditLogEvent> ales, EventCRFBean eventCRFBean, Integer ecId) {
		for (AuditLogEvent ale : ales) {
			if (eventCRFBean.getId() != ecId) {
				ale.setEntityId(eventCRFBean.getId());
				getAuditLogEventDao().saveOrUpdate(ale);
				System.out.println("Updating EventCRF Ids in Entity Id field of AL");
			}
		}

	}

	// method#1 update event_crf_Ids in Audit Log table also set repeat key to 1
	// for null values where audit_table =item_data
	private void updateEventCrfIdsInALEventCrfFieldandSetRepeatKeyTo1(ArrayList<AuditLogEvent> ales, EventCRFBean eventCRFBean, Integer ecId) {
		for (AuditLogEvent ale : ales) {
			if (eventCRFBean.getId() != ecId) {
				ale.setEventCrfId(eventCRFBean.getId());
				if (ale.getItemDataRepeat() == null)
					ale.setItemDataRepeat(1);
				getAuditLogEventDao().saveOrUpdate(ale);
				System.out.println("Updating EventCRF Ids in Event_crf field,also setting repeat key number to 1 for null values");
			}
		}
	}

	// method#3 insert a single record in Event_crf table
	private EventCRFBean createEventCRF(CRFVersionBean crfVersionBean, StudyEventBean studyEventBean, UserAccountBean userAccountBean) {

		EventCRFBean ecBean = new EventCRFBean();
		ecBean.setAnnotations("");
		ecBean.setCreatedDate(new Date());
		ecBean.setCRFVersionId(crfVersionBean.getId());
		ecBean.setInterviewerName("");
		ecBean.setDateInterviewed(null);
		ecBean.setOwner(userAccountBean);
		ecBean.setStatus(Status.AVAILABLE);
		ecBean.setCompletionStatusId(1);
		ecBean.setStudySubjectId(studyEventBean.getStudySubjectId());
		ecBean.setStudyEventId(studyEventBean.getId());
		ecBean.setValidateString("");
		ecBean.setValidatorAnnotations("");
		ecBean.setUpdater(userAccountBean);
		ecBean.setUpdatedDate(new Date());
		ecBean = (EventCRFBean) ecdao.create(ecBean);
		logger.debug("*********CREATED EVENT CRF");
		System.out.println("Saving a new EventCRF record in Event CRF TAble ");

		return ecBean;
	}

	private UserAccountBean getUserAccount(String userName) {
		udao = new UserAccountDAO(ds);
		UserAccountBean userAccountBean = (UserAccountBean) udao.findByUserName(userName);
		return userAccountBean;
	}

	private StudyEventBean updateStudyEvent(StudyEventBean seBean, SubjectEventStatus status, StudyBean studyBean,
			StudySubjectBean studySubjectBean) {
		// seBean.setUpdater(getUserAccount(getInputUsername(studyBean,
		// studySubjectBean)));
		seBean.setUpdatedDate(new Date());
		seBean.setSubjectEventStatus(status);
		seBean = (StudyEventBean) sedao.update(seBean);
		logger.debug("*********UPDATED STUDY EVENT ");
		return seBean;
	}

	private EventCRFBean updateEventCRF(EventCRFBean ecBean, StudyBean studyBean, StudySubjectBean studySubjectBean) {
		String inputUsername = ""; // getInputUsername(studyBean,
									// studySubjectBean);
		ecBean.setUpdater(getUserAccount(inputUsername));
		ecBean.setUpdatedDate(new Date());
		ecBean.setStatus(Status.UNAVAILABLE);
		ecBean = (EventCRFBean) ecdao.update(ecBean);
		logger.debug("*********UPDATED EVENT CRF");
		return ecBean;
	}

	private ItemDataBean createItemData(ItemBean itemBean, String itemValue, Integer itemOrdinal, EventCRFBean eventCrfBean,
			StudyBean studyBean, StudySubjectBean studySubjectBean) {
		logger.info("item Oid:  " + itemBean.getOid() + "   itemValue:  " + itemValue + "  itemOrdinal:  " + itemOrdinal);
		ItemDataBean itemDataBean = new ItemDataBean();
		itemDataBean.setItemId(itemBean.getId());
		itemDataBean.setEventCRFId(eventCrfBean.getId());
		itemDataBean.setValue(itemValue);
		itemDataBean.setCreatedDate(new Date());
		itemDataBean.setStatus(Status.UNAVAILABLE);
		itemDataBean.setOrdinal(itemOrdinal);
		// itemDataBean.setOwner(getUserAccount(getInputUsername(studyBean,
		// studySubjectBean)));
		return itemDataBean;
	}

	public AuditLogEventDao getAuditLogEventDao() {
		return auditLogEventDao;
	}

	public void setAuditLogEventDao(AuditLogEventDao auditLogEventDao) {
		this.auditLogEventDao = auditLogEventDao;
	}

}
