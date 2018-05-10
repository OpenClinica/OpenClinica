package org.akaza.openclinica.control;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.view.component.Column;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.GroupCellEditor;
import org.jmesa.view.html.AbstractHtmlView;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.HtmlSnippets;

public class StudyStatisticsView extends AbstractHtmlView {
	   private final ResourceBundle resword;

       public StudyStatisticsView(Locale locale) {
           resword = ResourceBundleProvider.getWordsBundle(locale);
       }
	
	public Object render() {
	        HtmlSnippets snippets = getHtmlSnippets();
	        HtmlBuilder html = new HtmlBuilder();
	        setCustomCellEditors();
	        html.append(snippets.themeStart());
	     
	        html.append(snippets.tableStart());
	       
	        html.append(snippets.theadStart());
	        html.append(customHeader());
	        html.append(snippets.filter());
	        html.append(snippets.header());
	        html.append(snippets.theadEnd());
	        html.append(snippets.tbodyStart());
	        html.append(snippets.body());
	        html.append(snippets.tbodyEnd());
	        html.append(snippets.footer());
	        html.append(snippets.tableEnd());
	        html.append(snippets.themeEnd());
	        html.append(snippets.initJavascriptLimit());
	       
	        return html.toString();
	    }

	    /**
	     * Setting the group cell editor.
	     */
	    private void setCustomCellEditors(){
	    	List<Column> columns = getTable().getRow().getColumns();
	        for (Column column : columns) {
	            CellEditor decoratedCellEditor = column.getCellRenderer().getCellEditor();

	            column.getCellRenderer().setCellEditor(new GroupCellEditor(decoratedCellEditor));
	        }

	    }
    private String customHeader() {
        HtmlBuilder html = new HtmlBuilder();
        
        html.thead(0).tr(0).styleClass("header").close();
        html.td(0).colspan("4").style("border-bottom: 1px solid white;background-color:white;color:grey;").align("center").close().append(resword.getString("subject_enrollment_for_study")).tdEnd();
        
        html.theadEnd(0);
        return html.toString();
    }
}


