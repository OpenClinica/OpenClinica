package core.org.akaza.openclinica.service.crfdata;

import core.org.akaza.openclinica.bean.admin.CRFBean;
import core.org.akaza.openclinica.bean.core.DataEntryStage;
import core.org.akaza.openclinica.bean.core.Status;
import core.org.akaza.openclinica.bean.login.UserAccountBean;
import core.org.akaza.openclinica.bean.managestudy.EventDefinitionCRFBean;
import core.org.akaza.openclinica.bean.managestudy.StudyEventBean;
import core.org.akaza.openclinica.bean.submit.*;
import core.org.akaza.openclinica.dao.hibernate.DynamicsItemFormMetadataDao;
import core.org.akaza.openclinica.dao.hibernate.DynamicsItemGroupMetadataDao;
import core.org.akaza.openclinica.dao.managestudy.EventDefinitionCRFDAO;
import core.org.akaza.openclinica.dao.managestudy.StudyEventDAO;
import core.org.akaza.openclinica.dao.submit.*;
import core.org.akaza.openclinica.domain.crfdata.DynamicsItemFormMetadataBean;
import core.org.akaza.openclinica.domain.crfdata.DynamicsItemGroupMetadataBean;
import core.org.akaza.openclinica.domain.rule.RuleSetBean;
import core.org.akaza.openclinica.domain.rule.action.PropertyBean;
import core.org.akaza.openclinica.domain.rule.action.StratificationFactorBean;
import core.org.akaza.openclinica.exception.OpenClinicaException;
import core.org.akaza.openclinica.service.rule.expression.ExpressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

public class DynamicsMetadataService implements MetadataServiceInterface {
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final String ESCAPED_SEPERATOR = "\\.";
    private DynamicsItemFormMetadataDao dynamicsItemFormMetadataDao;
    private DynamicsItemGroupMetadataDao dynamicsItemGroupMetadataDao;
    DataSource ds;
    private ExpressionService expressionService;

    public DynamicsMetadataService(DataSource ds) {
        // itemsAlreadyShown = new ArrayList<Integer>();
        this.ds = ds;
    }

    public boolean hide(Object metadataBean, EventCRFBean eventCrfBean) {
        // TODO -- interesting problem, where is the SpringServletAccess object going to live now? tbh 03/2010
        ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        itemFormMetadataBean.setShowItem(false);
        // DynamicsItemFormMetadataDao dynamicsMetadataDao = (DynamicsItemFormMetadataDao) metadataDao;
        // DynamicsItemFormMetadataDao metadataDao = (DynamicsItemFormMetadataDao) SpringServletAccess.getApplicationContext(context).getBean("dynamicsItemFormMetadataDao");
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setShowItem(false);
        getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        return true;
    }

    public boolean isShown(Integer itemId, EventCRFBean eventCrfBean, ItemDataBean itemDataBean) {
        // do we check against the database, or just against the object? against the db
        // ItemFormMetadataBean itemFormMetadataBean = (ItemFormMetadataBean) metadataBean;
        // return itemFormMetadataBean.isShowItem();
        ItemFormMetadataBean itemFormMetadataBean = getItemFormMetadataDAO().findByItemIdAndCRFVersionId(itemId, eventCrfBean.getCRFVersionId());
        DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean, itemDataBean);
        // DynamicsItemFormMetadataBean dynamicsMetadataBean = getDynamicsItemFormMetadataDao().findByMetadataBean(itemFormMetadataBean, eventCrfBean);
        if (dynamicsMetadataBean != null) {
            // System.out.println("DID find a row in the db for (with IDB) " + itemFormMetadataBean.getId() + " idb id " + itemDataBean.getId());
            return dynamicsMetadataBean.isShowItem();
        } else {
            // System.out.println("did not find a row in the db for (with IDB) " + itemFormMetadataBean.getId() + " idb id " + itemDataBean.getId());
            return false;
        }
        // return false;
    }

    /**
     *
     * TODO: remove the @deprecated call. The reason it is there now is to accommodate the call being made from the DataEntryServlet
     *
     * @param metadataBean
     * @param eventCrfBean
     * @param itemDataBean
     * @return DynamicsItemFormMetadataBean
     */
    private DynamicsItemFormMetadataBean getDynamicsItemFormMetadataBean(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean, ItemDataBean itemDataBean) {
        ItemFormMetadataBean itemFormMetadataBean = metadataBean;
        DynamicsItemFormMetadataBean dynamicsMetadataBean = null;

        dynamicsMetadataBean = getDynamicsItemFormMetadataDao().findByMetadataBean(itemFormMetadataBean, eventCrfBean, itemDataBean);

        return dynamicsMetadataBean;

    }

    public boolean showItem(ItemFormMetadataBean metadataBean, EventCRFBean eventCrfBean, ItemDataBean itemDataBean) {
        ItemFormMetadataBean itemFormMetadataBean = metadataBean;
        itemFormMetadataBean.setShowItem(true);
        DynamicsItemFormMetadataBean dynamicsMetadataBean = new DynamicsItemFormMetadataBean(itemFormMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setItemDataId(itemDataBean.getId());
        dynamicsMetadataBean.setShowItem(true);
        dynamicsMetadataBean.setPassedDde(0);
        getDynamicsItemFormMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        logger.debug("just touched ifmb id " + metadataBean.getId() + " ecb id " + eventCrfBean.getId() + " item id " + metadataBean.getItemId()
            + " itemdata id " + itemDataBean.getId());
        return true;
    }

    public boolean showGroup(ItemGroupMetadataBean metadataBean, EventCRFBean eventCrfBean) {

        ItemGroupMetadataBean itemGroupMetadataBean = metadataBean;
        itemGroupMetadataBean.setShowGroup(true);
        DynamicsItemGroupMetadataBean dynamicsMetadataBean = new DynamicsItemGroupMetadataBean(itemGroupMetadataBean, eventCrfBean);
        dynamicsMetadataBean.setPassedDde(0);
        getDynamicsItemGroupMetadataDao().saveOrUpdate(dynamicsMetadataBean);
        return true;
    }





    public DynamicsItemFormMetadataDao getDynamicsItemFormMetadataDao() {
        return dynamicsItemFormMetadataDao;
    }

    public DynamicsItemGroupMetadataDao getDynamicsItemGroupMetadataDao() {
        return dynamicsItemGroupMetadataDao;
    }

    //JN: The following methods were all returning global variables causing an issue in the case of concurrent users. Modified to return new DAO Object. The thread pooling will take care of any heap issues and I do not expect any.

    private ItemDataDAO getItemDataDAO() {
        return new ItemDataDAO(ds);
    }

    private ItemDAO getItemDAO() {
        return new ItemDAO(ds);
    }

    private ItemFormMetadataDAO getItemFormMetadataDAO() {
        return new ItemFormMetadataDAO(ds);
    }

    private ItemGroupMetadataDAO getItemGroupMetadataDAO() {
        return new ItemGroupMetadataDAO(ds);
    }

    public StudyEventDAO getStudyEventDAO() {
        return new StudyEventDAO(ds);
    }

    public ExpressionService getExpressionService() {
        return expressionService;
    }

    public void setExpressionService(ExpressionService expressionService) {
        this.expressionService = expressionService;
    }


}
