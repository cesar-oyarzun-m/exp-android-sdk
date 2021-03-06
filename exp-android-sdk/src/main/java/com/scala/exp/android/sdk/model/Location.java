package com.scala.exp.android.sdk.model;

import com.scala.exp.android.sdk.AppSingleton;
import com.scala.exp.android.sdk.Exp;
import com.scala.exp.android.sdk.Utils;
import com.scala.exp.android.sdk.observer.ExpObservable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Cesar Oyarzun on 10/28/15.
 */

public class Location extends AbstractModel {

    private static final String API_LOCATION = "/api/locations/";
    private static final String LAYOUT = "/layout";
    private static final String RT = "_rt=";
    public static final String LOCATION_UUID = "location.uuid";
    private List<Zone> zones;

    public String getLayoutUrl(){
        String host = AppSingleton.getInstance().getHost();
        String rt = AppSingleton.getInstance().getAuth().getRestrictedToken();

        StringBuilder builder = new StringBuilder(host)
                .append(API_LOCATION)
                .append(getString(Utils.UUID))
                .append(LAYOUT)
                .append("?").append(RT).append(rt);

        return  builder.toString();
    }


    public void setZones(List<Zone> zones) {
        this.zones = zones;
    }

    public List<Zone> getZones() {
        return zones;
    }

    public ExpObservable<SearchResults<Device>> getDevices(){
        Map options = new HashMap();
        options.put(Utils.LOCATION_UUID,getString(Utils.UUID));
        return Exp.findDevices(options);
    }

    public ExpObservable<SearchResults<Thing>> getThings(){
        Map options = new HashMap();
        options.put(Utils.LOCATION_UUID, getString(Utils.UUID));
        return Exp.findThings(options);
    }

    public ExpObservable<Location> getCurrentLocation(){
        final ExpObservable<Device> observable = Device.getCurrentDevice();
            return new ExpObservable<Location>(observable.<Location>flatMapExp(new Func1<Device, ExpObservable<Location>>() {
                @Override
                public ExpObservable<Location> call(Device device) {
                    if (device != null && device.get(LOCATION_UUID) != null) {
                        return device.getLocation();
                    } else {
                        return new ExpObservable<Location>(Observable.<Location>empty());
                    }
                }
            }));
    }

    @Override
    public ExpObservable<Location> save() {
        Observable<Location> locationObservable = AppSingleton.getInstance().getEndPoint().saveLocation(getUuid(),getDocument())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
        return new ExpObservable<Location>(locationObservable);
    }

    @Override
    public ExpObservable<Location> refresh() {
        return Exp.getLocation(getUuid());
    }
}