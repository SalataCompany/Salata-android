package com.ar.salata;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class SalataApplication extends Application {
	public final static String BASEURL = "https://www.salatamasr.shop/";
//	public final static String BASEURL = "https://staging.salatamasr.shop/";

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
