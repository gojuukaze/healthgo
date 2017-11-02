/*
 * Copyright 2015 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.ikaze.healthgo;

import android.app.Application;
import android.util.Log;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {

    private boolean serviceRun;

    private boolean isShowToast=false;

    public boolean isShowToast() {
        return isShowToast;
    }

    public void setShowToast(boolean showToast) {
        isShowToast = showToast;
    }

    public boolean getServiceRun() {
        return serviceRun;
    }

    public void setServiceRun(boolean serviceRun) {
        this.serviceRun = serviceRun;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        serviceRun=false;
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("step_db")
                .build();
        Log.d("app","app create()");
        Realm.setDefaultConfiguration(realmConfig); // Make this Realm the default
    }
}
