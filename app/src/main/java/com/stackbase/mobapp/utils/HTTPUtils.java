package com.stackbase.mobapp.utils;

/**
 * HttpUtil is a single class containing methods to conveniently perform HTTP
 * requests. HttpUtil only uses regular java io and net functionality and does
 * not depend on external libraries.
 * The class contains methods to perform a get, post, put, and delete request,
 * and supports posting forms. Optionally, one can provide headers.
 *
 * Example usage:
 *
 *     // get
 *     String res = HttpUtil.get("http://www.google.com");
 *
 *     // post
 *     String res = HttpUtil.post("http://sendmedata.com", "This is the data");
 *
 *     // post form
 *     Map<String, String> params = new HashMap<String, String>();
 *     params.put("firstname", "Joe");
 *     params.put("lastname", "Smith");
 *     params.put("age", "28");
 *     String res = HttpUtil.postForm("http://site.com/newuser", params);
 *
 *     // append query parameters to url
 *     String url = "http://mydatabase.com/users";
 *     Map<String, String> params = new HashMap<String, String>();
 *     params.put("orderby", "name");
 *     params.put("limit", "10");
 *     String fullUrl = HttpUtil.appendQueryParams(url, params);
 *     // fullUrl = "http://mydatabase.com/user?orderby=name&limit=10"
 *
 */


import com.stackbase.mobapp.ApplicationContextProvider;
import com.stackbase.mobapp.R;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPUtils {
    public enum Result {
        RESPONSE,
        COOKIE
    }

    /**
     * Send a get request
     * @param url
     * @return response
     * @throws IOException
     */
    static public Map<Result, String>  get(String url) throws IOException {
        return get(url, null);
    }

    /**
     * Send a get request
     * @param url         Url as string
     * @param headers     Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  get(String url,
                             Map<String, String> headers) throws IOException {
        return fetch("GET", url, null, headers);
    }

    /**
     * Send a post request
     * @param url         Url as string
     * @param body        Request body as string
     * @param headers     Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  post(String url, String body,
                              Map<String, String> headers) throws IOException {
        return fetch("POST", url, body, headers);
    }

    static public Map<Result, String>  post(String url,
                              Map<String, String> headers, File uploadFile) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        try {

            HttpPost httppost = new HttpPost(url);
            if (headers != null) {
                for (String name : headers.keySet()) {
                    httppost.addHeader(name, headers.get(name));
                }
            }
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setCharset(MIME.UTF8_CHARSET);
            builder.addBinaryBody("uploadify", uploadFile, ContentType.MULTIPART_FORM_DATA,
                    uploadFile.getName());
            httppost.setEntity(builder.build());
            HttpResponse response = httpclient.execute(httppost);
            int status = response.getStatusLine().getStatusCode();
            // checks server's status code first
            if (status == HttpURLConnection.HTTP_OK) {
                Header[] repHeaders = response.getAllHeaders();
//                InputStream is = httpConn.getInputStream();
                String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");

//                is.close();
                Map<Result, String> result = new HashMap<>();
                result.put(Result.RESPONSE, responseBody);
                if (repHeaders != null) {
                    for (Header header: repHeaders) {
                        if (header.getName().equals("Set-Cookie")) {
                            result.put(Result.COOKIE, header.getValue());
                        }
                    }
                }
                return result;
            } else {
                throw new RemoteException(status,
                        String.format(ApplicationContextProvider.getContext().getString(
                                R.string.call_api_failed),
                        status));
            }
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    /**
     * Send a post request
     * @param url         Url as string
     * @param body        Request body as string
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  post(String url, String body) throws IOException {
        return post(url, body, null);
    }

    /**
     * Post a form with parameters
     * @param url         Url as string
     * @param params      map with parameters/values
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  postForm(String url, Map<String, String> params)
            throws IOException {
        return postForm(url, params, null);
    }

    /**
     * Post a form with parameters
     * @param url         Url as string
     * @param params      Map with parameters/values
     * @param headers     Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  postForm(String url, Map<String, String> params,
                                  Map<String, String> headers) throws IOException {
        // set content type
        if (headers == null) {
            headers = new HashMap<String, String>();
        }
        headers.put("Content-Type", "application/x-www-form-urlencoded");

        // parse parameters
        String body = "";
        if (params != null) {
            boolean first = true;
            for (String param : params.keySet()) {
                if (first) {
                    first = false;
                } else {
                    body += "&";
                }
                String value = params.get(param);
                body += URLEncoder.encode(param, "UTF-8") + "=";
                body += URLEncoder.encode(value, "UTF-8");
            }
        }
        return post(url, body, headers);
    }

    /**
     * Send a put request
     * @param url         Url as string
     * @param body        Request body as string
     * @param headers     Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  put(String url, String body,
                             Map<String, String> headers) throws IOException {
        return fetch("PUT", url, body, headers);
    }

    /**
     * Send a put request
     * @param url         Url as string
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  put(String url, String body) throws IOException {
        return put(url, body, null);
    }

    /**
     * Send a delete request
     * @param url         Url as string
     * @param headers     Optional map with headers
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  delete(String url,
                                Map<String, String> headers) throws IOException {
        return fetch("DELETE", url, null, headers);
    }

    /**
     * Send a delete request
     * @param url         Url as string
     * @return response   Response as string
     * @throws IOException
     */
    static public Map<Result, String>  delete(String url) throws IOException {
        return delete(url, null);
    }

    /**
     * Append query parameters to given url
     * @param url         Url as string
     * @param params      Map with query parameters
     * @return url        Url with query parameters appended
     * @throws IOException
     */
    static public String appendQueryParams(String url,
                                           Map<String, String> params) throws IOException {
        String fullUrl = new String(url);

        if (params != null) {
            boolean first = (fullUrl.indexOf('?') == -1);
            for (String param : params.keySet()) {
                if (first) {
                    fullUrl += '?';
                    first = false;
                }
                else {
                    fullUrl += '&';
                }
                String value = params.get(param);
                fullUrl += URLEncoder.encode(param, "UTF-8") + '=';
                fullUrl += URLEncoder.encode(value, "UTF-8");
            }
        }

        return fullUrl;
    }

    /**
     * Retrieve the query parameters from given url
     * @param url         Url containing query parameters
     * @return params     Map with query parameters
     * @throws IOException
     */
    static public Map<String, String> getQueryParams(String url)
            throws IOException {
        Map<String, String> params = new HashMap<String, String>();

        int start = url.indexOf('?');
        while (start != -1) {
            // read parameter name
            int equals = url.indexOf('=', start);
            String param = "";
            if (equals != -1) {
                param = url.substring(start + 1, equals);
            }
            else {
                param = url.substring(start + 1);
            }

            // read parameter value
            String value = "";
            if (equals != -1) {
                start = url.indexOf('&', equals);
                if (start != -1) {
                    value = url.substring(equals + 1, start);
                }
                else {
                    value = url.substring(equals + 1);
                }
            }

            params.put(URLDecoder.decode(param, "UTF-8"),
                    URLDecoder.decode(value, "UTF-8"));
        }

        return params;
    }

    /**
     * Returns the url without query parameters
     * @param url         Url containing query parameters
     * @return url        Url without query parameters
     * @throws IOException
     */
    static public String removeQueryParams(String url)
            throws IOException {
        int q = url.indexOf('?');
        if (q != -1) {
            return url.substring(0, q);
        }
        else {
            return url;
        }
    }

    /**
     * Send a request
     * @param method      HTTP method, for example "GET" or "POST"
     * @param url         Url as string
     * @param body        Request body as string
     * @param headers     Optional map with headers
     * @return response   Response as Map: key: response, value response
     * @throws IOException
     */
    static public Map<Result, String> fetch(String method, String url, String body,
                               Map<String, String> headers) throws IOException {
        // connection
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection)u.openConnection();
        conn.setConnectTimeout(50000);
        conn.setReadTimeout(60000);

        // method
        if (method != null) {
            conn.setRequestMethod(method);
        }

        // headers
        if (headers != null) {
            for(String key : headers.keySet()) {
                conn.addRequestProperty(key, headers.get(key));
            }
        } else {
            headers = new HashMap<>();
        }
        headers.put("User-Agent", "esse.io mobile Agent");

        // body
        if (body != null) {
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            os.write(body.getBytes());
            os.flush();
            os.close();
        }

        try {
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_PERM) {
                // handle redirects
                String location = conn.getHeaderField("Location");
                return fetch(method, location, body, headers);
            } else if (status != HttpURLConnection.HTTP_OK) {
                throw new RemoteException(conn.getResponseCode(), String.format(ApplicationContextProvider.getContext().getString(
                                R.string.call_api_failed),
                        status));
            } else {
                // response
                InputStream is = conn.getInputStream();
                Map<String, List<String>> repHeaders = conn.getHeaderFields();
                String response = streamToString(is);
                is.close();
                Map<Result, String> result = new HashMap<>();
                result.put(Result.RESPONSE, response);
                if (repHeaders != null) {
                    List<String> cookies = repHeaders.get("Set-Cookie");
                    if (cookies != null) {
                        for (String s: cookies) {
                            if (s.indexOf("SESSIONID") >= 0) {
                                result.put(Result.COOKIE, s);
                                break;
                            }
                        }
                    }
                }
                return result;
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Read an input stream into a string
     * @param in
     * @return
     * @throws IOException
     */
    static public String streamToString(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

}
