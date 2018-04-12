package com.conviva.platforms.android;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import com.conviva.api.system.ICallbackInterface;


/**
 * A task that makes an http request and waits for response asynchronously.
 */
public class HTTPTask implements Runnable {
    private ICallbackInterface _callback = null;
    private String _httpMethod;
    private String _url;
    private String _data;
    private int _timeoutMs;
    private String _contentT;

    public void setState(String httpMethod, String url, String data,
            String contentT, int timeoutMs, ICallbackInterface callback) {
        _httpMethod = httpMethod == null ? "POST" : httpMethod;
        _url = url;
        _data = data;
        _contentT = contentT == null ? "application/json" : contentT;
        _timeoutMs = timeoutMs;
        _callback = callback;
    }

    @Override
    public void run() {
        ConnectionResult cr = handleConnection();
        callbackIfPresent(cr.success, cr.message);
    }

    private class ConnectionResult  {
        public boolean success;
        public String message;

        public ConnectionResult(boolean scs, String msg) {
            success = scs;
            message = msg;
        }
    }

    private void callbackIfPresent(boolean success, String message) {
        if (_callback != null) {
            _callback.done(success, message);
        }
        _callback = null;
    }

    private ConnectionResult handleConnection () {
        int code = -1;
        String response = "";

        URL url = null;
        try {
            url = new URL(_url);

            HttpURLConnection urlConnection = null;

            try {
                urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setReadTimeout(_timeoutMs);
                urlConnection.setConnectTimeout(_timeoutMs);
//                urlConnection.setInstanceFollowRedirects(false);

                try {
                    urlConnection.setRequestMethod(_httpMethod);
                } catch (ProtocolException ex) {
                    // Never happens? Fallback to POST
                    _httpMethod = "POST";
                    urlConnection.setRequestMethod(_httpMethod);
                }

                urlConnection.setRequestProperty("Content-Type", _contentT);

                // Can overwrite header User-Agent by following line.
                urlConnection.setRequestProperty("User-Agent", AndroidSystemUtils.getDefaultUserAgent());
                //android.util.Log.i("Default User Agent is ", AndroidSystemUtils.getDefaultUserAgent());

                if (_httpMethod.equals("POST")) {
                    urlConnection.setDoOutput(true);
//                    urlConnection.setDoInput(true);
                    urlConnection.setUseCaches(false);
                    byte[] utf8Bytes = _data.getBytes("UTF-8");
                    urlConnection.setFixedLengthStreamingMode(utf8Bytes.length);
                    try {
                        OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                        out.write(utf8Bytes);
                        out.close();

                        try {
                            urlConnection.connect();

                            try {
                                code = urlConnection.getResponseCode();
                            } catch (IOException ex) {
                                return new ConnectionResult(false, ex.toString());
                            }

                            try {
                                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                                byte[] contents = new byte[1024];
                                ByteArrayOutputStream inBytes = new ByteArrayOutputStream();
                                int bytesRead = 0;
                                while ((bytesRead = in.read(contents)) != -1) {
                                    inBytes.write(contents, 0, bytesRead);
                                }
                                response = new String(inBytes.toByteArray());
                            } catch (IOException ex) {
                                return new ConnectionResult(false, ex.toString());
                            } finally {}
                        } catch (IOException ex) {
                            return new ConnectionResult(false, ex.toString());
                        } finally {
                            urlConnection.disconnect(); // #TODO: is this safe if connect() failed?
                        }
                    } catch (IOException ex) {
                        return new ConnectionResult(false, ex.toString());
                    } catch (IllegalStateException e) {
                        return new ConnectionResult(false, e.toString());
                    } finally {
                    }
                }
            } catch (IOException ex) {
                return new ConnectionResult(false, ex.toString());
            } finally {}
        } catch (MalformedURLException ex) {
            return new ConnectionResult(false, ex.toString());
        } catch(ArrayIndexOutOfBoundsException e) {
            // TODO: Check what can cause this issue & how can it be resolved instead of catching an exception.
            return new ConnectionResult(false, e.toString());
        }  finally {}

        if (code == HttpURLConnection.HTTP_OK) {
            return new ConnectionResult(true, response);
        } else {
            return new ConnectionResult(false, "Status code in HTTP response is not OK: " + code);
        }
    }

}

