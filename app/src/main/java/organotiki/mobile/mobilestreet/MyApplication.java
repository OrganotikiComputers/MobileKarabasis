package organotiki.mobile.mobilestreet;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import organotiki.mobile.mobilestreet.objects.GlobalVar;

/**
 * Created by Thanasis on 1/7/2016.
 */
public class MyApplication extends Application {

    static Realm realm;
    GlobalVar gVar;

    @Override
    public void onCreate() {
        super.onCreate();
        // Create a RealmConfiguration that saves the Realm file in the app's "files" directory.
        Realm.init(this);
        //RealmConfiguration realmConfig = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().build();
        RealmConfiguration realmConfig = new RealmConfiguration.Builder().deleteRealmIfMigrationNeeded().allowWritesOnUiThread(true).build();
        Realm.setDefaultConfiguration(realmConfig);
        realm =Realm.getDefaultInstance();
    }

    public static Realm getRealm() {
        return realm;
    }

}
