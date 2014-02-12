package org.craftercms.engine.util.url;

import org.craftercms.core.service.Content;

import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Implementation of {@link java.net.URLConnection} that wraps a {@link org.craftercms.core.service.Content}.
 *
 * @author Alfonso Vásquez
 */
public class ContentUrlConnection extends URLConnection {

    private static final String CONTENT_LENGTH =    "content-length";
    private static final String CONTENT_TYPE =      "content-type";
    private static final String LAST_MODIFIED =     "last-modified";
    private static final String DATE_FORMAT =       "EEE, dd MMM yyyy HH:mm:ss 'GMT'";
    private static final String TIMEZONE =          "GMT";

    protected Content content;
    protected Map<String, String> headers;
    protected String contentType;
    protected long length;
    protected long lastModified;
    protected InputStream is;


    protected boolean connected;
    protected boolean initializedHeaders;

    public ContentUrlConnection(URL url, Content content) {
        super(url);

        this.content = content;
        this.headers = new LinkedHashMap<String, String>();
    }

    @Override
    public void connect() throws IOException {
        if (!connected) {
            is = content.getInputStream();
            connected = true;
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        connect();

        return is;
    }

    protected void initializeHeaders() {
        if (!initializedHeaders) {
            length = content.getLength();
            lastModified = content.getLastModified();

            FileNameMap map = getFileNameMap();
            contentType = map.getContentTypeFor(url.getFile());

            if (contentType != null) {
                headers.put(CONTENT_TYPE, contentType);
            }

            headers.put(CONTENT_LENGTH, String.valueOf(length));

            /*
             * Format the last-modified field into the preferred
             * Internet standard - ie: fixed-length subset of that
             * defined by RFC 1123
             * */
            Date date = new Date(lastModified);
            SimpleDateFormat dateFormat = new SimpleDateFormat (DATE_FORMAT, Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));

            headers.put(LAST_MODIFIED, dateFormat.format(date));

            initializedHeaders = true;
        }
    }

    @Override
    public String getHeaderField(String name) {
        initializeHeaders();

        return headers.get(name);
    }

    @Override
    public String getHeaderField(int n) {
        initializeHeaders();

        Collection<String> values = headers.values();
        String[] valuesArray = values.toArray(new String[values.size()]);

        return valuesArray[n];
    }

    @Override
    public String getHeaderFieldKey(int n) {
        initializeHeaders();

        Collection<String> keys = headers.keySet();
        String[] keysArray = keys.toArray(new String[keys.size()]);

        return keysArray[n];
    }

    @Override
    public int getContentLength() {
        initializeHeaders();

        return (int) length;
    }

    @Override
    public long getLastModified() {
        initializeHeaders();

        return lastModified;
    }

}
