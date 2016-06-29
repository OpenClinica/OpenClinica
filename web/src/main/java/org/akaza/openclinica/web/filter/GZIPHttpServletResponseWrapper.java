package org.akaza.openclinica.web.filter;

import org.apache.http.HttpHeaders;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/**
 * Created by yogi on 6/28/16.
 */
public class GZIPHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private ServletResponseGZIPOutputStream gzipStream;
    private ServletOutputStream outputStream;
    private PrintWriter printWriter;

    public GZIPHttpServletResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        response.addHeader(HttpHeaders.CONTENT_ENCODING, "gzip");
    }

    public void finish() throws IOException {
        if (printWriter != null) {
            printWriter.close();
        }
        if (outputStream != null) {
            outputStream.close();
        }
        if (gzipStream != null) {
            gzipStream.close();
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (printWriter != null) {
            printWriter.flush();
        }
        if (outputStream != null) {
            outputStream.flush();
        }
        super.flushBuffer();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (printWriter != null) {
            throw new IllegalStateException("printWriter already defined");
        }
        if (outputStream == null) {
            initGzip();
            outputStream = gzipStream;
        }
        return outputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (outputStream != null) {
            throw new IllegalStateException("printWriter already defined");
        }
        if (printWriter == null) {
            initGzip();
            printWriter = new PrintWriter(new OutputStreamWriter(gzipStream, getResponse().getCharacterEncoding()));
        }
        return printWriter;
    }

    @Override
    public void setContentLength(int len) {
    }

    private void initGzip() throws IOException {
        gzipStream = new ServletResponseGZIPOutputStream(getResponse().getOutputStream());
    }

}

