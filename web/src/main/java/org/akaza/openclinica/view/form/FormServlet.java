package org.akaza.openclinica.view.form;

import org.akaza.openclinica.bean.submit.DisplayItemGroupBean;
import org.akaza.openclinica.control.admin.SpreadsheetPreviewNw;
import org.akaza.openclinica.control.managestudy.BeanFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA. User: bruceperry Date: May 19, 2007
 */
public class FormServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PrintWriter pw = httpServletResponse.getWriter();
        HorizontalFormBuilder builder = new HorizontalFormBuilder();
        SpreadsheetPreviewNw spnw = new SpreadsheetPreviewNw();
        BeanFactory beanFactory = new BeanFactory();
        ServletContext context = this.getServletContext();
        String path = context.getRealPath("/");
        POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(new File(path + "group_demo_nw.xls")));
        HSSFWorkbook wb = new HSSFWorkbook(fs);
        Map<String, Map> allMap = spnw.createCrfMetaObject(wb);
        /*
         * Map gmap = spnw.createGroupsMap(wb); Map map2 =
         * spnw.createItemsOrSectionMap(wb,"items"); FormGroupBean fgBean =
         * beanFactory.createFormGroupBeanFromMap(gmap); DisplayFormGroupBean
         * displayGroup = new DisplayFormGroupBean(); List itemsDisplayList =
         * beanFactory. createDisplayItemBeansFromMap(map2,"group_demo");
         * displayGroup.setFormGroupBean(fgBean);
         * displayGroup.setItems(itemsDisplayList);
         */
        List<DisplayItemGroupBean> formGroupsL = new ArrayList<DisplayItemGroupBean>();
        String crfName = (String) allMap.get("crf_info").get("crf_name");
        Map sections = allMap.get("sections");
        Map sectionMap = (Map) sections.get(1);
        String sectionLabel = (String) sectionMap.get("section_label");
        formGroupsL = beanFactory.createGroupBeans(allMap.get("items"), allMap.get("groups"), sectionLabel, crfName);
        // formGroupsL.add(displayGroup);
        builder.setDisplayItemGroups(formGroupsL);
        pw.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n"
            + "        \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n" + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" + "<head>\n"
            + "\t<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\" />\n"
            + "\t<title>test form</title><link rel=\"stylesheet\" href=\"includes/styles.css\" type=\"text/css\">\n"
            + "  <link rel=\"stylesheet\" href=\"includes/styles2.css\" type=\"text/css\">\n"
            + " <script type=\"text/javascript\"  language=\"JavaScript\" src=\n" + "    \"includes/repetition-model/repetition-model.js\"></script>"
            + "</head>\n" + "<body>");
        pw.write(builder.createMarkup());
        pw.write("</body>\n" + "</html>");
    }
}
