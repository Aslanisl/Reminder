package ru.mail.aslanisl.reminder;

import android.app.Application;


public class App extends Application {

    private static App mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static App get(){
        return mInstance;
    }
}
