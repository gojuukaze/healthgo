package cn.ikaze.healthgo.model;

import android.util.Log;

import io.realm.Realm;
import io.realm.RealmAsyncTask;


/**
 * Created by gojuukaze on 16/8/19.
 * Email: i@ikaze.uu.me
 */
public class SuccessTransaction implements Realm.Transaction.OnSuccess {

    RealmAsyncTask asyncTransaction;

    public SuccessTransaction(RealmAsyncTask task) {
        this.asyncTransaction = task;
    }

    @Override
    public void onSuccess() {
        if (asyncTransaction != null && !asyncTransaction.isCancelled()) {
            asyncTransaction.cancel();
            asyncTransaction = null;
        }
        Log.d("realm","insert success");

    }


}
