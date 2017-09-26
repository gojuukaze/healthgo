package cn.ikaze.healthgo.model;

import android.util.Log;

import java.util.Date;

import io.realm.Realm;


/**
 * Created by gojuukaze on 16/8/19.
 * Email: i@ikaze.uu.me
 */
public class StepTransaction implements Realm.Transaction {

    private Date date;
    private long num;

    public StepTransaction(Date date, long num) {
        this.date = date;
        this.num = num;
    }

    @Override
    public void execute(Realm realm) {
        Log.d("realm", "now insert [" + date + " ," + num + "]");

        StepModel stepModel =realm.where(StepModel.class).equalTo("date",date).findFirst();

        if (stepModel == null)
            stepModel = realm.createObject(StepModel.class);
        stepModel.setDate(date);
        stepModel.setNumSteps(num);
    }
}
