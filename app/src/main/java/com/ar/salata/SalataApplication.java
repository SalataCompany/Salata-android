package com.ar.salata;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class SalataApplication extends Application {
//	public final static String BASEURL = "http://www.salatamasr.com/";
	public final static String BASEURL = "http://staging.salatamasr.com/";

	@Override
	public void onCreate() {
		super.onCreate();

		Realm.init(this);
		RealmConfiguration defaultConfiguration = new RealmConfiguration.Builder()
				.deleteRealmIfMigrationNeeded()
				.name("SalataDB.realm")
				.build();
		Realm.setDefaultConfiguration(defaultConfiguration);
	}
}
