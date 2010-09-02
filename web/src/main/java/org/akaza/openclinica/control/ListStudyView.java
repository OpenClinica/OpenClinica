package org.akaza.openclinica.control;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.view.component.Column;
import org.jmesa.view.editor.CellEditor;
import org.jmesa.view.editor.GroupCellEditor;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.HtmlSnippets;

public class ListStudyView  extends DefaultView{
	
	
	   private final ResourceBundle resword;
    

	public ListStudyView(Locale locale)
	{
		 resword = ResourceBundleProvider.getWordsBundle(locale);
	}
	
	public Object render() {
	        HtmlSnippets snippets = getHtmlSnippets();
	        HtmlBuilder html = new HtmlBuilder();
	    
	        html.append(snippets.themeStart());
	        html.append(snippets.tableStart());
	      
	        html.append(snippets.theadStart());
	        
	        html.append(customHeader());
	        html.append(snippets.toolbar());
	        html.append(snippets.header());
	        html.append(snippets.filter());
	        html.append(snippets.theadEnd());
	        html.append(snippets.tbodyStart());
	        setCustomCellEditors();
	        html.append(snippets.body());
	        html.append(snippets.tbodyEnd());
	        html.append(snippets.footer());
	        html.append(snippets.statusBar());
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
	    	getTable().setCaption("Subject Enrollment");
	     
	    }
	        private String customHeader(){
	        	HtmlBuilder html = new HtmlBuilder();
	        	
	        		        html.tr(1).styleClass("header").width("100%").close();
	        		      
	        		        html.td(0).style("border-bottom: 1px solid white;background-color:white;color:black;").align("center").close().append(resword.getString("subject_matrix")).tdEnd();
	        		        
	        		        html.trEnd(1);
	        	
	        		       
	        		        return html.toString();
	        		    }

}
