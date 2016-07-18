package org.akaza.openclinica.control;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.fetcher.FeedFetcher;
import com.sun.syndication.fetcher.FetcherException;
import com.sun.syndication.fetcher.impl.FeedFetcherCache;
import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
import com.sun.syndication.io.FeedException;
import org.akaza.openclinica.web.SQLInitServlet;
import org.apache.commons.lang.StringEscapeUtils;
import org.jmesa.view.html.HtmlBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

public class RssReaderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    FeedFetcherCache feedInfoCache = HashMapFeedInfoCache.getInstance();
    FeedFetcher feedFetcher = new HttpURLFeedFetcher(feedInfoCache);
    String rssUrl = SQLInitServlet.getField("rss.url");
    String rssMore = SQLInitServlet.getField("rss.more");
    String text1 = SQLInitServlet.getField("about.text1");
    String text2 = SQLInitServlet.getField("about.text2");
    ResourceBundle resword,resformat;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    	resword = ResourceBundle.getBundle("org.akaza.openclinica.i18n.words",req.getLocale());
    	resformat = ResourceBundle.getBundle("org.akaza.openclinica.i18n.format",req.getLocale());
    	PrintWriter pw = new PrintWriter(resp.getOutputStream());
        if (rssUrl == null || rssUrl.length() == 0) {
            about(pw);
        } else {
            getFeed(pw);
        }
    }

    void getFeed(PrintWriter pw) {

        SyndFeed feed = null;
        String htmlFeed = null;

        try {
            feed = feedFetcher.retrieveFeed(new URL(rssUrl));
            htmlFeed = feedHtml(feed);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            htmlFeed = errorFeedHtml(e.getMessage());
            e.printStackTrace();
        } catch (FeedException e) {
            // TODO Auto-generated catch block
            htmlFeed = errorFeedHtml(e.getMessage());
            e.printStackTrace();
        } catch (FetcherException e) {
            htmlFeed = errorFeedHtml(e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            htmlFeed = errorFeedHtml(e.getMessage());
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            pw.println(htmlFeed);
            pw.close();
        }
    }

    void about(PrintWriter pw) {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.h1().close().append(resword.getString("about")).h1End().ul().close();
        htmlBuilder.li().close().append(text1).liEnd();
        htmlBuilder.li().close().append(text2).liEnd();
        htmlBuilder.ulEnd().toString();

        pw.println(htmlBuilder.toString());
        pw.close();

    }

    String feedHtml(SyndFeed feed) {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.h1().close().append(resword.getString("news")).h1End().ul().close();
        List<SyndEntryImpl> theFeeds = feed.getEntries();

        for (int i = 0; i < (theFeeds.size() >= 4 ? 4 : theFeeds.size()); i++) {
            SyndEntryImpl syndFeed = theFeeds.get(i);
            String description = null;

            if (syndFeed.getDescription().getValue().length() > 50) {
                Integer k = 50;
                while (syndFeed.getDescription().getValue().charAt(k) != ' ') {
                    k--;
                }
                description = syndFeed.getDescription().getValue().substring(0, k) + " ...";
            } else {
                description = syndFeed.getDescription().getValue();
            }
            SimpleDateFormat sdf = new SimpleDateFormat(resformat.getString("mid_date_format"));
            String theDate = sdf.format(syndFeed.getPublishedDate());
            htmlBuilder.li().close().a().href(syndFeed.getLink()).append(" target=\"_blank\"").close().append(
                    theDate + " - " + StringEscapeUtils.escapeHtml(syndFeed.getTitle()) + " - " + description).aEnd().liEnd();

        }
        if (rssMore != null && rssMore.length() > 0) {
            return htmlBuilder.ulEnd().a().href(rssMore).append(" target=\"_blank\"").close().div().align("right").close().append(resword.getString("more")+"...").divEnd().aEnd()
                    .toString();
        } else {
            return htmlBuilder.ulEnd().toString();
        }

    }

    String errorFeedHtml(String error) {
        HtmlBuilder htmlBuilder = new HtmlBuilder();
        htmlBuilder.h1().close().append(resword.getString("news")).h1End().ul().close();
        htmlBuilder.li().close().append(resword.getString("couldnot_retrieve_news")).liEnd();
        return htmlBuilder.ulEnd().toString();
    }
}
