/**
 * ==============================================================
 */
package com.THLight.USBeacon.Sample;
/** ============================================================== */

import android.app.Application;

/** ============================================================== */
public class thLightApplication extends Application {
    public static thLightApplication App = null;
    public static THLConfig Config = null;

    /** ================================================ */
    public static thLightApplication getApp() {
        return App;
    }

    /** ================================================ */
    @Override
    public void onCreate() {
        super.onCreate();

        App = this;
        Config = new THLConfig(this);

        Config.loadSettings();
    }

    /** ================================================ */
    @Override
    public void onTerminate() {
        Config.saveSettings();

        super.onTerminate();
    }
}
