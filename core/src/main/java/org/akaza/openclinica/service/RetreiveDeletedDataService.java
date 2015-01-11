package org.akaza.openclinica.service;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.akaza.openclinica.domain.user.UserAccount;
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

	EventDefinitionCRFDAO edcdao;
	UserAccountDAO udao;
	StudyEventDAO sedao;
	EventCRFDAO ecdao;
	CRFVersionDAO cvdao;
	CRFDAO cdao;
	ItemDataDAO iddao;
	ItemDAO idao;
	AuthoritiesDao authoritiesDao;
	AuditLogEventDao auditLogEventDao;

	public RetreiveDeletedDataService() {
	}

	public RetreiveDeletedDataService(DataSource ds, AuditLogEventDao auditLogEventDao) {
		this.ds = ds;
		this.auditLogEventDao = auditLogEventDao;
	}

	// start point

	@SuppressWarnings("null")
	public void retrieveProcess(Integer studyEventId) throws IllegalArgumentException, IllegalAccessException {
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - ");

		AuditLogEvent auditLogEvent = prepareForAGroupList(studyEventId);
		ArrayList<Object> crfObjects = (ArrayList<Object>) getAuditLogEventDao().findByParam(auditLogEvent);
		ArrayList<AuditLogEvent> auditLogEvents = parseCrfObjects(crfObjects, studyEventId);

		if (auditLogEvents.size() != 0) {
			ArrayList<Integer> listOfCrfIds = new ArrayList<Integer>();
			for (AuditLogEvent auditEvent : auditLogEvents) {

				if (listOfCrfIds.size() == 0) {
					listOfCrfIds.add(auditEvent.getCrfId());
				} else if (!listOfCrfIds.contains(auditEvent.getCrfId())) {
					listOfCrfIds.add(auditEvent.getCrfId());
				}
			}
			System.out.println("List of CRF_ID count:  " + listOfCrfIds.size());

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
						System.out.println("For crfId= " + crfId + "  cv= " + crfVersionBean.getId() + "    Event CRF exists in eCRF Table: ecId= " + eventCRFBean.getId());

						// update AL item_data records)
						loopListOfALRecords(auditLogEvents, eventCRFBean, crfId, null, null);

						break;
					} else if (eventCRFBean == null && crfVersionsCount == count) {
						System.out.println("For crfId= " + crfId + "    Event CRF does not exist");
						// EventCRFBean eventCRF =
						// createEventCRF(crfVersionBean, studyEventBean,
						// userAccountBean);
						loopListOfALRecords(auditLogEvents, null, crfId, crfVersionBean, studyEventBean);

						// update AL item_data records
					}
				}
			}
		} else {
			System.out.println("No Deleted Crfs has been detected...");
		}
	}

	// Loop List of item_data records AL table that requires to be updated its
	// fields
	private void loopListOfALRecords(ArrayList<AuditLogEvent> auditLogEvents, EventCRFBean eCrfBean, Integer crfId, CRFVersionBean crfVersionBean, StudyEventBean studyEventBean) {
		Integer createrOrOwnerUserId = 1;
		Integer updaterUserId = 1;

		for (AuditLogEvent ale : auditLogEvents) {
			if (ale.getCrfId() == crfId) {

				ale.setAuditTable("item_data");
				ArrayList<AuditLogEvent> listOfEventCrfRecordsInAuditLogToBeUpdated = getAuditLogEventDao().findByParamForEventCrf(ale);
				if (listOfEventCrfRecordsInAuditLogToBeUpdated.size() != 0) {
					int listSize = listOfEventCrfRecordsInAuditLogToBeUpdated.size();
					createrOrOwnerUserId = listOfEventCrfRecordsInAuditLogToBeUpdated.get(0).getUserAccount().getUserId();
					updaterUserId = listOfEventCrfRecordsInAuditLogToBeUpdated.get(listSize - 1).getUserAccount().getUserId();
					udao = new UserAccountDAO(ds);
					UserAccountBean uBean = (UserAccountBean) udao.findByPK(createrOrOwnerUserId);
					// create New Event_CRF record in Event_crf Table if it does not exist
					if (eCrfBean == null)
						eCrfBean = createEventCRF(crfVersionBean, studyEventBean, uBean);
					updateEventCrfIdsInAL(listOfEventCrfRecordsInAuditLogToBeUpdated, eCrfBean, ale.getEventCrfId());

					ale.setAuditTable("event_crf");
					ArrayList<AuditLogEvent> listOfEntityIdInALToBeUpdated = getAuditLogEventDao().findByParamForEventCrf(ale);
					if (listOfEntityIdInALToBeUpdated.size() != 0)
						updateEntityIdInAL(listOfEntityIdInALToBeUpdated, eCrfBean, ale.getEventCrfId());
				}
			}
		}

		// Check ItemData Table for available records
		iddao = new ItemDataDAO(ds);
		ArrayList<ItemDataBean> listOfitemDataBeans = iddao.findAllByEventCRFId(eCrfBean.getId());
		ArrayList<Object> itemObjects = (ArrayList<Object>) getAuditLogEventDao().findByParamForItemData("item_data", eCrfBean.getId());
		ArrayList<AuditLogEvent> uniqueListOfItemRecordsInAL = parseItemObjects(itemObjects);

		for (AuditLogEvent itemRecordInAL : uniqueListOfItemRecordsInAL) {
			if (listOfitemDataBeans.size() != 0) {
				idao = new ItemDAO(ds);
				ItemBean iBean = (ItemBean) idao.findByNameAndCRFId(itemRecordInAL.getEntityName(), crfId);

				for (ItemDataBean itemDataBean : listOfitemDataBeans) {

					if (itemRecordInAL.getEventCrfId() == itemDataBean.getEventCRFId() && iBean.getId() == itemDataBean.getItemId() && itemRecordInAL.getItemDataRepeat() == itemDataBean.getOrdinal()) {
						uniqueListOfItemRecordsInAL.remove(itemRecordInAL);
					}
				}
			}
		}

		insertItemDataRecordsInItemDataTable(uniqueListOfItemRecordsInAL, crfId, createrOrOwnerUserId, updaterUserId);

		// Update ItemData Ids in AL Table getting its values from ItemData item_data_id
		ArrayList<ItemDataBean> listOfitemDataBeansAfterInsert = iddao.findAllByEventCRFId(eCrfBean.getId());

		AuditLogEvent auditEvent = new AuditLogEvent();
		auditEvent.setAuditTable("item_data");
		auditEvent.setEventCrfId(eCrfBean.getId());
		ArrayList<AuditLogEvent> auditEventList = null;
		ArrayList<AuditLogEvent> listOfItemRecordsInAL = getAuditLogEventDao().findByParamForEventCrf(auditEvent);
		updaterUserId = listOfItemRecordsInAL.get(listOfItemRecordsInAL.size() - 1).getUserAccount().getUserId();

		idao = new ItemDAO(ds);

		for (ItemDataBean itemDataBean : listOfitemDataBeansAfterInsert) {
			for (AuditLogEvent itemRecordInAL : listOfItemRecordsInAL) {
				ItemBean iBean = (ItemBean) idao.findByNameAndCRFId(itemRecordInAL.getEntityName(), crfId);
				if (itemRecordInAL.getEventCrfId() == itemDataBean.getEventCRFId() && iBean.getId() == itemDataBean.getItemId() && itemRecordInAL.getItemDataRepeat() == itemDataBean.getOrdinal()) {
					// update AL
					itemRecordInAL.setEntityId(itemDataBean.getId());
					getAuditLogEventDao().saveOrUpdate(itemRecordInAL);
					System.out.println("Updating ItemData Ids in  AL ");
				}
			}
		}

		updateEventCRF(eCrfBean, updaterUserId);
	}

	private void insertItemDataRecordsInItemDataTable(ArrayList<AuditLogEvent> ales, Integer crfId, Integer createrOrOwnerUserId, Integer updaterUserId) {
		//Collections.sort
		
		ArrayList <ItemDataBean> idBeans = new ArrayList();
		for (AuditLogEvent ale : ales) {
			idao = new ItemDAO(ds);
			ItemBean iBean = (ItemBean) idao.findByNameAndCRFId(ale.getEntityName(), crfId);
			ItemDataBean itemDataBean = buildItemDataBean(ale.getItemDataRepeat(), ale.getEventCrfId(), iBean.getId(), createrOrOwnerUserId);
		    idBeans.add(itemDataBean);   
		}
			// Sort ItemDataBean List by item_id and ordinal
            sortList(idBeans);		
		
		for (ItemDataBean idBean : idBeans){
			iddao = new ItemDataDAO(ds);
			iddao.create(idBean);
			System.out.println("Inserting Item Data record in Item Data Table " + idBean.getId());
		}

		for (ItemDataBean idBean : idBeans){
			updateItemDataBean(idBean, updaterUserId);
			iddao = new ItemDataDAO(ds);
			iddao.update(idBean);
		}


	}

	// method#4 update event_crf_Ids in entity_id column in Audit Log table
	// where audit_table=event_crf
	private void updateEntityIdInAL(ArrayList<AuditLogEvent> ales, EventCRFBean eventCRFBean, Integer ecId) {
		for (AuditLogEvent ale : ales) {
			if (eventCRFBean.getId() != ecId) {
				ale.setEntityId(eventCRFBean.getId());
				ale.setEventCrfId(eventCRFBean.getId());
				getAuditLogEventDao().saveOrUpdate(ale);
				System.out.println("Updating EventCRF Ids in Entity Id field of AL " + eventCRFBean.getId());
			} else {
				System.out.println("No Updates to Entity Id field of AL for eventCrfId= " + ecId);
			}
		}

	}

	// method#1 update event_crf_Ids in Audit Log table also set repeat key to 1
	// for null values where audit_table =item_data
	private void updateEventCrfIdsInAL(ArrayList<AuditLogEvent> ales, EventCRFBean eventCRFBean, Integer ecId) {
		for (AuditLogEvent ale : ales) {
			if (eventCRFBean.getId() != ecId) {
				ale.setEventCrfId(eventCRFBean.getId());
				if (ale.getItemDataRepeat() == null)
					ale.setItemDataRepeat(1);
				getAuditLogEventDao().saveOrUpdate(ale);
				System.out.println("Updating EventCRF Ids in Event_crf field of AL Table " + ale.getEventCrfId() + "  To  " + eventCRFBean.getId());
			} else {
				System.out.println("No Updates to Event_CRF and repeat # field of AL for eventCrfId= " + ecId);
			}
		}
	}

	// method#3 insert a single record in Event_crf table
	private EventCRFBean createEventCRF(CRFVersionBean crfVersionBean, StudyEventBean studyEventBean, UserAccountBean userAccountBean) {

		EventCRFBean ecBean = new EventCRFBean();
		ecBean.setAnnotations("");
		// ecBean.setCreatedDate(new Date());
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
		// ecBean.setUpdater(userAccountBean);
		// ecBean.setUpdatedDate(new Date());
		ecBean = (EventCRFBean) ecdao.create(ecBean);
		logger.debug("*********CREATED EVENT CRF");
		System.out.println("Saving a new EventCRF record in Event CRF TAble with event_crf_id = " + ecBean.getId());

		return ecBean;
	}

	private EventCRFBean updateEventCRF(EventCRFBean ecBean, Integer updaterUserId) {

		UserAccountBean uBean = new UserAccountBean();
		uBean.setId(updaterUserId);
		ecBean.setUpdater(uBean);
		ecBean.setOldStatus(ecBean.getStatus());
		System.out.println("Updating Event CRF Updater User_id");
		ecBean = (EventCRFBean) ecdao.update(ecBean);

		logger.debug("*********UPDATED EVENT CRF");
		return ecBean;
	}

	private ItemDataBean updateItemDataBean(ItemDataBean itemDataBean, Integer updaterUserId) {

		UserAccountBean uBean = new UserAccountBean();

		uBean.setId(updaterUserId);
		itemDataBean.setUpdater(uBean);

		itemDataBean.setOldStatus(itemDataBean.getStatus());
		System.out.println("  Updating Item Data  updater User_Id");

		return itemDataBean;
	}

	private ItemDataBean buildItemDataBean(Integer itemRepeatKey, Integer eventCrfId, Integer itemId, Integer createrOrOwnerUserId) {

		ItemDataBean itemDataBean = new ItemDataBean();
		UserAccountBean uBean = new UserAccountBean();

		uBean.setId(createrOrOwnerUserId);
		itemDataBean.setOwner(uBean);

		itemDataBean.setUpdater(uBean);

		itemDataBean.setItemId(itemId);
		itemDataBean.setEventCRFId(eventCrfId);
		itemDataBean.setValue("");
		itemDataBean.setCreatedDate(new Date());
		itemDataBean.setStatus(Status.AVAILABLE);
		itemDataBean.setOrdinal(itemRepeatKey);

		return itemDataBean;
	}

	private ArrayList<AuditLogEvent> parseCrfObjects(ArrayList<Object> crfObjects, Integer studyEventId) {
		ArrayList<AuditLogEvent> auditLogEvents = new ArrayList<AuditLogEvent>();
		AuditLogEvent auditLogEvent;

		for (Object obj : crfObjects) {
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
			System.out.println("eventCrfId: " + eventCrfId + "  eventCrfVersionId: " + eventCrfVersionId + "  crfId:  " + crfId + "  studyEventId:  " + studyEventId);
		}
		return auditLogEvents;
	}

	private ArrayList<AuditLogEvent> parseItemObjects(ArrayList<Object> itemObjects) {
		ArrayList<AuditLogEvent> auditLogEvents = new ArrayList<AuditLogEvent>();
		AuditLogEvent auditLogEvent;

		for (Object obj : itemObjects) {
			Object[] row = (Object[]) obj;
			int eventCrfId = Integer.valueOf(row[0].toString());
			String entityName = row[1].toString();
			int itemDataRepeat = Integer.valueOf(row[2].toString());

			auditLogEvent = new AuditLogEvent();
			auditLogEvent.setEventCrfId(eventCrfId);
			auditLogEvent.setEntityName(entityName);
			auditLogEvent.setItemDataRepeat(itemDataRepeat);

			auditLogEvents.add(auditLogEvent);
			System.out.println("eventCrfId: " + eventCrfId + "  entityName: " + entityName + "  itemDataRepeat:  " + itemDataRepeat);
		}
		return auditLogEvents;
	}

	private AuditLogEvent prepareForAGroupList(Integer studyEventId) {
		AuditLogEvent auditLogEvent = new AuditLogEvent();
		AuditLogEventType auditLogEventType = new AuditLogEventType();
		auditLogEventType.setAuditLogEventTypeId(13);
		auditLogEvent.setAuditLogEventType(auditLogEventType);
		auditLogEvent.setStudyEventId(studyEventId);
		auditLogEvent.setAuditTable("item_data");
		return auditLogEvent;
	}

	public AuditLogEventDao getAuditLogEventDao() {
		return auditLogEventDao;
	}

	public void setAuditLogEventDao(AuditLogEventDao auditLogEventDao) {
		this.auditLogEventDao = auditLogEventDao;
	}

	@SuppressWarnings("unchecked")
	private void sortList(ArrayList<ItemDataBean> idBeans) {

	    Collections.sort(idBeans, new Comparator() {

	        public int compare(Object o1, Object o2) {

	            Integer x1 = ((ItemDataBean) o1).getItemId();
	            Integer x2 = ((ItemDataBean) o2).getItemId();
	            int sComp = x1.compareTo(x2);

	            if (sComp != 0) {
	               return sComp;
	            } else {
	                x1 = ((ItemDataBean) o1).getOrdinal();
	                x2 = ((ItemDataBean) o2).getOrdinal();
	               return x1.compareTo(x2);
	            }
	            }
	    });
	}

	}

