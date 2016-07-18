/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.dao.extract;

import org.akaza.openclinica.bean.core.EntityBean;
import org.akaza.openclinica.bean.extract.ArchivedDatasetFileBean;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.dao.core.AuditableEntityDAO;
import org.akaza.openclinica.dao.core.DAODigester;
import org.akaza.openclinica.dao.core.SQLFactory;
import org.akaza.openclinica.dao.core.TypeNames;
import org.akaza.openclinica.dao.login.UserAccountDAO;

import javax.sql.DataSource;
import java.util.*;

/**
 * @author thickerson
 *
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ArchivedDatasetFileDAO extends AuditableEntityDAO {
    private DAODigester digester;

    public ArchivedDatasetFileDAO(DataSource ds) {
        super(ds);
        digester = SQLFactory.getInstance().getDigester(digesterName);
        this.setQueryNames();
    }

    public ArchivedDatasetFileDAO(DataSource ds, DAODigester digester) {
        super(ds);
        this.digester = digester;
    }

    protected void setQueryNames() {
        getCurrentPKName = "getCurrentPK";
    }

    @Override
    protected void setDigesterName() {
        digesterName = SQLFactory.getInstance().DAO_ARCHIVED_DATASET_FILE;
    }

    @Override
    public void setTypesExpected() {
        this.unsetTypeExpected();
        this.setTypeExpected(1, TypeNames.INT);// file id
        this.setTypeExpected(2, TypeNames.STRING);// name
        this.setTypeExpected(3, TypeNames.INT);// dataset id
        this.setTypeExpected(4, TypeNames.INT);// export format id
        this.setTypeExpected(5, TypeNames.STRING);// file_reference
        this.setTypeExpected(6, TypeNames.INT);// run_time
        this.setTypeExpected(7, TypeNames.INT);// file_size
        this.setTypeExpected(8, TypeNames.DATE);// date_created
        this.setTypeExpected(9, TypeNames.INT);// owner id
    }

    public EntityBean create(EntityBean eb) {
        ArchivedDatasetFileBean fb = (ArchivedDatasetFileBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        variables.put(Integer.valueOf(1), fb.getName());
        variables.put(Integer.valueOf(2), Integer.valueOf(fb.getDatasetId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(fb.getExportFormatId()));
        variables.put(Integer.valueOf(4), fb.getFileReference());
        variables.put(Integer.valueOf(5), Integer.valueOf(fb.getFileSize()));
        variables.put(Integer.valueOf(6), new Double(fb.getRunTime()));
        variables.put(Integer.valueOf(7), Integer.valueOf(fb.getOwnerId()));
        this.executeWithPK(digester.getQuery("create"), variables, nullVars);
        if (isQuerySuccessful()) {
            fb.setId(getLatestPK());
        }
        return fb;
    }

    public EntityBean update(EntityBean eb) {
        ArchivedDatasetFileBean fb = (ArchivedDatasetFileBean) eb;
        HashMap variables = new HashMap();
        HashMap nullVars = new HashMap();
        variables.put(Integer.valueOf(1), fb.getName());
        variables.put(Integer.valueOf(2), Integer.valueOf(fb.getDatasetId()));
        variables.put(Integer.valueOf(3), Integer.valueOf(fb.getExportFormatId()));
        variables.put(Integer.valueOf(4), fb.getFileReference());
        variables.put(Integer.valueOf(5), Integer.valueOf(fb.getFileSize()));
        variables.put(Integer.valueOf(6), new Double(fb.getRunTime()));
        variables.put(Integer.valueOf(7), Integer.valueOf(fb.getOwnerId()));
        variables.put(Integer.valueOf(8), Integer.valueOf(fb.getId()));
        this.execute(digester.getQuery("update"), variables, nullVars);
        return fb;
    }

    public Object getEntityFromHashMap(HashMap hm) {
        ArchivedDatasetFileBean fb = new ArchivedDatasetFileBean();
        fb.setId(((Integer) hm.get("archived_dataset_file_id")).intValue());
        fb.setDateCreated((Date) hm.get("date_created"));

        fb.setName((String) hm.get("name"));
        fb.setId(((Integer) hm.get("archived_dataset_file_id")).intValue());
        fb.setDatasetId(((Integer) hm.get("dataset_id")).intValue());
        fb.setExportFormatId(((Integer) hm.get("export_format_id")).intValue());
        fb.setFileReference((String) hm.get("file_reference"));
        fb.setRunTime(((Integer) hm.get("run_time")).doubleValue());
        fb.setFileSize(((Integer) hm.get("file_size")).intValue());
        fb.setOwnerId(((Integer) hm.get("owner_id")).intValue());
        UserAccountDAO uaDAO = new UserAccountDAO(this.ds);
        UserAccountBean owner = (UserAccountBean) uaDAO.findByPK(fb.getOwnerId());
        fb.setOwner(owner);
        return fb;
    }

    public void deleteArchiveDataset(ArchivedDatasetFileBean adfBean){
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), adfBean.getId());
        this.execute(digester.getQuery("deleteArchiveDataset"), variables);
    }



    public Collection findAll() {
        this.setTypesExpected();
        ArrayList alist = this.select(digester.getQuery("findAll"));
        ArrayList al = new ArrayList();
        Iterator it = alist.iterator();
        while (it.hasNext()) {
            ArchivedDatasetFileBean fb = (ArchivedDatasetFileBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(fb);
        }
        return al;
    }

    public Collection findAll(String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public EntityBean findByPK(int ID) {
        ArchivedDatasetFileBean fb = new ArchivedDatasetFileBean();
        this.setTypesExpected();

        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(ID));

        String sql = digester.getQuery("findByPK");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        if (it.hasNext()) {
            fb = (ArchivedDatasetFileBean) this.getEntityFromHashMap((HashMap) it.next());
        }

        return fb;
    }

    public ArrayList findByDatasetId(int did) {
        // ArchivedDatasetFileBean fb = new ArchivedDatasetFileBean();
        this.setTypesExpected();
        ArrayList al = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(did));

        String sql = digester.getQuery("findByDatasetId");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ArchivedDatasetFileBean fb = (ArchivedDatasetFileBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(fb);
        }

        return al;
    }

    public ArrayList findByDatasetIdByDate(int did) {
        // ArchivedDatasetFileBean fb = new ArchivedDatasetFileBean();
        this.setTypesExpected();
        ArrayList al = new ArrayList();
        HashMap variables = new HashMap();
        variables.put(Integer.valueOf(1), Integer.valueOf(did));

        String sql = digester.getQuery("findByDatasetIdByDate");
        ArrayList alist = this.select(sql, variables);
        Iterator it = alist.iterator();

        while (it.hasNext()) {
            ArchivedDatasetFileBean fb = (ArchivedDatasetFileBean) this.getEntityFromHashMap((HashMap) it.next());
            al.add(fb);
        }

        return al;
    }
    public Collection findAllByPermission(Object objCurrentUser, int intActionType, String strOrderByColumn, boolean blnAscendingSort, String strSearchPhrase) {
        ArrayList al = new ArrayList();

        return al;
    }

    public Collection findAllByPermission(Object objCurrentUser, int intActionType) {
        ArrayList al = new ArrayList();

        return al;
    }
}
