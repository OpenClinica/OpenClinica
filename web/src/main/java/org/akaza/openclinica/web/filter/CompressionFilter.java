package org.akaza.openclinica.web.filter;

import org.apache.http.HttpHeaders;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by yogi on 6/28/16.
 */
public class CompressionFilter implements Filter {

    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String acceptEncoding = httpRequest.getHeader(HttpHeaders.ACCEPT_ENCODING);
        if (acceptEncoding != null) {
            if (acceptEncoding.indexOf("gzip") >= 0) {
                GZIPHttpServletResponseWrapper gzipResponse = new GZIPHttpServletResponseWrapper(httpResponse);
                chain.doFilter(request, gzipResponse);
                gzipResponse.finish();
                return;
            }
        }
        chain.doFilter(request, response);
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

}
