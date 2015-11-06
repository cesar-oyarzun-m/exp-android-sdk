package com.scala.expandroidsdk;


import android.util.Log;

import com.scala.expandroidsdk.channels.OrganizationChannel;
import com.scala.expandroidsdk.channels.SystemChannel;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import rx.Observable;
import rx.Subscriber;


/**
 * Created by Cesar Oyarzun on 11/4/15.
 */
public class SocketManager {


    private final String LOG_TAG = SocketManager.class.getSimpleName();
    private Socket socket;
    private OrganizationChannel organizationChannel = null;
    private SystemChannel systemChannel = null;


    public Observable startSocket() {

        if (socket == null) {
            try {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, trustAllCerts, new SecureRandom());
                IO.setDefaultSSLContext(sc);
                HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

                // socket options
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                opts.reconnection = false;
                opts.secure = true;
                opts.sslContext = sc;

                socket = IO.socket(AppSingleton.getInstance().getHost(), opts);

                //create channels
                organizationChannel = new OrganizationChannel(socket);
                systemChannel = new SystemChannel(socket);

                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {

                    }

                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(LOG_TAG, "Socket Disconnected");
                    }

                }).on(Utils.MESSAGE, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        final JSONObject json = (JSONObject) args[0];
                        try {
                            String type = json.getString(Utils.TYPE);
                            String channel = null;
                            if(json.has(Utils.CHANNEL)){
                                channel = json.getString(Utils.CHANNEL);
                            }
                            if(Utils.RESPONSE.equalsIgnoreCase(type)){
                                if(channel == null){
                                    systemChannel.onResponse(json);
                                }else{
                                    Utils.SOCKET_CHANNELS socket_channels = Utils.fromString(channel);
                                    switch (socket_channels){
                                        case SYSTEM:
                                            systemChannel.onResponse(json);
                                            break;
                                        case ORGANIZATION:
                                            break;
                                        case LOCATION:
                                            break;
                                        case EXPERIENCE:
                                            break;
                                    }
                                }
                            }else if(Utils.REQUEST.equalsIgnoreCase(type)){
                                if(channel == null){
                                    systemChannel.onRequest(json);
                                }else{
                                    Utils.SOCKET_CHANNELS socket_channels = Utils.fromString(channel);
                                    switch (socket_channels){
                                        case SYSTEM:
                                            systemChannel.onRequest(json);
                                            break;
                                        case ORGANIZATION:
                                            break;
                                        case LOCATION:
                                            break;
                                        case EXPERIENCE:
                                            break;
                                    }
                                }
                            }else if(Utils.BROADCAST.equalsIgnoreCase(type)){
                                if(channel == null){
                                    systemChannel.onBroadCast(json);
                                }else{
                                    Utils.SOCKET_CHANNELS socket_channels = Utils.fromString(channel);
                                    switch (socket_channels){
                                        case SYSTEM:
                                            systemChannel.onBroadCast(json);
                                            break;
                                        case ORGANIZATION:
                                            break;
                                        case LOCATION:
                                            break;
                                        case EXPERIENCE:
                                            break;
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        Log.d(LOG_TAG, "error: " + args[0].toString());
                    }

                });
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }

        // Connect if disconnected
        if (!socket.connected()) {
            Log.d(LOG_TAG, "Connecting with Socket...");
            socket.connect();
        } else {
            Log.d(LOG_TAG, "Socket Connected");
        }
        return Observable.just(true);
    }




    private TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) throws CertificateException {
        }
    } };

    private static class RelaxedHostNameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private SSLContext ceateSsl() {
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            IO.setDefaultSSLContext(sc);
            HttpsURLConnection.setDefaultHostnameVerifier(new RelaxedHostNameVerifier());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return sc;
    }


    public void getCurrentExperience(Subscriber callback) throws JSONException {
        Map<String,String> message = new HashMap<>();
        message.put(Utils.TYPE,Utils.REQUEST);
        message.put(Utils.NAME, Utils.GET_CURRENT_EXPERIENCE);
        systemChannel.request(message, callback);
    }

    public void getCurrentDevice(Subscriber callback) throws JSONException {
        Map<String,String> message = new HashMap<>();
        message.put(Utils.TYPE, Utils.REQUEST);
        message.put(Utils.NAME, Utils.GET_CURRENT_DEVICE);
        systemChannel.request(message, callback);
    }



}