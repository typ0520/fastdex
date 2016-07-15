package com.example.fertilizercrm.basic;

import com.baidu.mapapi.model.LatLng;
import com.example.fertilizercrm.FertilizerApplication;
import com.example.fertilizercrm.bean.LoginResponse;

import com.example.fertilizercrm.common.utils.Logger;

/**
 * Created by tong on 2/9/15.
 */
public class DataManager {
    private static final DataManager instance = new DataManager();


    private LoginResponse loginResponse;
    private LatLng currentLatLng;

    public synchronized static DataManager getInstance() {
        return instance;
    }

    public void setLoginResponse(LoginResponse loginResponse) {
        this.loginResponse = loginResponse;
        if (loginResponse != null) {
            Logger.e("<< loginResponse: " + loginResponse.toString());
        }
    }

    public LoginResponse getLoginResponse() {
        return loginResponse;
    }


    public void setCurrentLatLng(LatLng currentLatLng) {
        this.currentLatLng = currentLatLng;
    }

    public LatLng getCurrentLatLng() {
        return currentLatLng;
    }

    public void clear() {
        setLoginResponse(null);

        FertilizerApplication.getInstance().getEventEmitter().unregisterAll();
    }
}
