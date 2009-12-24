package org.akaza.openclinica.bean.extract;

import java.io.OutputStream;
import java.util.List;

import org.akaza.openclinica.bean.core.EntityBean;

/**
 * User: bruceperry
 * Date: May 15, 2008
 * The interface for a class that creates a file and downloads to an operating
 * system a bean in various formats, such as CSV or PDF. The class is initially defined
 * for downloading DiscrepancyNoteBeans.
 * @see DownloadDiscrepancyNote
 * @author Bruce W. Perry
 */
public interface DownLoadBean {

    void downLoad(EntityBean bean, String format, OutputStream stream);
    void downLoad(List<EntityBean> listOfBeans, String format, OutputStream stream);
    int getContentLength(EntityBean bean, String format);
}
