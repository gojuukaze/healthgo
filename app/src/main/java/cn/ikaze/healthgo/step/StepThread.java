package cn.ikaze.healthgo.step;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Date;

import cn.ikaze.healthgo.DateTimeHelper;
import cn.ikaze.healthgo.model.StepModel;
import cn.ikaze.healthgo.model.StepTransaction;
import cn.ikaze.healthgo.model.SuccessTransaction;
import io.realm.Realm;
import io.realm.RealmAsyncTask;


/**
 * Created by gojuukaze on 16/8/22.
 * Email: i@ikaze.uu.me
 */
public class StepThread extends Thread implements  SensorEventListener, StepListener {

    private SensorManager sensorManager;
    Sensor accel;
    private StepDetector stepDetector;
    private long numStpes = 0;
    private long lastStpes = 0;
    RealmAsyncTask realmAsyncTask;
    boolean isRegiter = false;
    boolean isActivity = false;
    private Context context;
    private Date today;


    public StepThread(Context context) {
        this.context = context;
        initStepDetector();
    }


    @Override
    public void run() {
        if (!isRegiter) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            isRegiter = true;
        }
    }

    public void mystop()
    {
        if (isRegiter) {
            sensorManager.unregisterListener(this);
            isRegiter = false;
        }


    }

    public boolean isActivity() {
        return isActivity;
    }

    public void setActivity(boolean activity) {
        isActivity = activity;
    }

    public void initStepDetector() {

        today= DateTimeHelper.getToday();
        stepDetector = new StepDetector(this);
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Realm realm = Realm.getDefaultInstance();
        StepModel result = realm.where(StepModel.class)
                .equalTo("date", today)
                .findFirst();

        lastStpes = result == null ? 0 : result.getNumSteps();
        step(lastStpes);
        realm.close();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Subscribe
    public void subscribeActivity(Boolean f) {
        if (f)
            EventBus.getDefault().post(numStpes);
        else
            save(DateTimeHelper.getToday(),numStpes);
        isActivity = f;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            stepDetector.updateModel(isActivity);

            stepDetector.updateStep(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void step(long num) {
        if (!today.equals(DateTimeHelper.getToday()))
        {
            save(today,numStpes);
            numStpes=0;
            lastStpes=0;
            today=DateTimeHelper.getToday();
        }
        numStpes += num;
        Log.d("step", "step(num) " + numStpes);
        EventBus.getDefault().post(numStpes);
        if (numStpes - lastStpes > 10) {
            lastStpes = numStpes;
            save(today,numStpes);
        }

    }

    public void save(Date date,long num)
    {
        Realm realm = Realm.getDefaultInstance();
        realmAsyncTask = realm.executeTransactionAsync(
                new StepTransaction(date, num),
                new SuccessTransaction(realmAsyncTask),
                new Realm.Transaction.OnError() {
                    @Override
                    public void onError(Throwable error) {
                        error.printStackTrace();
                        Log.d("realm", "insert error");
                    }
                }
        );
        realm.close();
    }



}


