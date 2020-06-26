package core.org.akaza.openclinica.dao.extract;

import core.org.akaza.openclinica.bean.core.EntityBean;
import core.org.akaza.openclinica.bean.extract.OcQrtzTriggersBean;
import core.org.akaza.openclinica.dao.core.AuditableEntityDAO;
import core.org.akaza.openclinica.dao.core.DAODigester;
import core.org.akaza.openclinica.dao.core.SQLFactory;
import core.org.akaza.openclinica.dao.core.TypeNames;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

public class OcQrtzTriggersDAO extends AuditableEntityDAO {

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_OC_QRTZ_TRIGGERS;
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
    }

    /**
     * Creates a DatasetDAO object, for use in the application only.
     *
     * @param ds
     */
    public OcQrtzTriggersDAO(DataSource ds) {
        super(ds);
        this.setQueryNames();
    }

    /**
     * Creates a DatasetDAO object suitable for testing purposes only.
     *
     * @param ds
     * @param digester
     */
    public OcQrtzTriggersDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
        this.setQueryNames();
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.STRING); //trigger_name
        this.setTypeExpected(2, TypeNames.STRING); //trigger_group
        this.setTypeExpected(3, TypeNames.STRING); //job_name
        this.setTypeExpected(4, TypeNames.STRING); //job_group
        this.setTypeExpected(5, TypeNames.STRING); //description
        this.setTypeExpected(6, TypeNames.LONG); //next_fire_time
        this.setTypeExpected(7, TypeNames.LONG); //prev_fire_time
        this.setTypeExpected(8, TypeNames.INT); //priority
        this.setTypeExpected(9, TypeNames.STRING); //trigger_state
        this.setTypeExpected(10, TypeNames.STRING); //trigger_type
        this.setTypeExpected(11, TypeNames.LONG); //start_time
        this.setTypeExpected(12, TypeNames.LONG); //end_time
        this.setTypeExpected(13, TypeNames.STRING); //calendar_name
        this.setTypeExpected(14, TypeNames.SHORT); //misfire_instr
        this.setTypeExpected(15, TypeNames.BYTE); //job_data
        this.setTypeExpected(16, TypeNames.STRING); //sched_name
    }


    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            OcQrtzTriggersBean eb = (OcQrtzTriggersBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(eb);
        }
        return al;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        OcQrtzTriggersBean fb = new OcQrtzTriggersBean();
        fb.setTriggerGroup((String) hm.get("trigger_name"));
        fb.setTriggerGroup((String) hm.get("trigger_group"));
        fb.setJobName((String) hm.get("job_name"));
        fb.setJobGroup((String)hm.get("job_group"));
        fb.setDescription((String) hm.get("description"));
        fb.setNextFireTime((Long) hm.get("next_fire_time"));
        fb.setPrevFireTime((Long) hm.get("prev_fire_time"));
        fb.setPriority(((Integer) hm.get("priority")).intValue());
        fb.setTriggerState((String) hm.get("trigger_state"));
        fb.setTriggerType((String) hm.get("trigger_type"));
        fb.setStartTime((Long) hm.get("start_time"));
        fb.setEndTime((Long) hm.get("end_time"));
        if (hm.get("calander_name") != null) {
            fb.setCalendarName((String) hm.get("calander_name"));
        }
        fb.setMisfireInstr((Short) hm.get("misfire_instr"));
        fb.setJobData((Byte) hm.get("job_data"));
        fb.setSchedName((String) hm.get("sched_name"));
        return fb;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();
        return al;
    }

    public EntityBean findByPK(int ID) {
        OcQrtzTriggersBean fb = new OcQrtzTriggersBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            fb = (OcQrtzTriggersBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return fb;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();
        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();
        return al;
    }

    public EntityBean create(EntityBean eb) {
        try {
            // Do not use this to create an entity
        } catch (Exception e) {
            logger.error("Do not use this to create an entity, use the jobScheduler methods instead.", e.getMessage());
        }
        return null;
    }

    public EntityBean update(EntityBean eb) {
        try {
            // Do not use this to update an entity
        } catch (Exception e) {
            logger.error("Do not use this to update an entity, use the jobScheduler methods instead.", e.getMessage());
        }
        return null;
    }

}
