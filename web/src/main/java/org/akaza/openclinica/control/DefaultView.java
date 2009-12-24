package org.akaza.openclinica.control;

import org.akaza.openclinica.i18n.util.ResourceBundleProvider;
import org.jmesa.view.html.AbstractHtmlView;
import org.jmesa.view.html.HtmlBuilder;
import org.jmesa.view.html.HtmlSnippets;

import java.util.Locale;
import java.util.ResourceBundle;

public class DefaultView extends AbstractHtmlView {

    private ResourceBundle resword;

    public DefaultView() {
        // TODO Auto-generated constructor stub
    }

    public DefaultView(Locale locale) {
        resword = ResourceBundleProvider.getWordsBundle(locale);
    }

    public Object render() {
        HtmlSnippets snippets = getHtmlSnippets();
        HtmlBuilder html = new HtmlBuilder();
        html.append(snippets.themeStart());
        html.append(snippets.tableStart());
        html.append(snippets.theadStart());
        html.append(snippets.toolbar());
        html.append(snippets.header());
        html.append(snippets.filter());
        html.append(snippets.theadEnd());
        html.append(snippets.tbodyStart());
        html.append(snippets.body());
        html.append(snippets.tbodyEnd());
        html.append(snippets.footer());
        html.append(snippets.statusBar());
        html.append(snippets.tableEnd());
        html.append(snippets.themeEnd());
        html.append(snippets.initJavascriptLimit());
        return html.toString();
    }
}
