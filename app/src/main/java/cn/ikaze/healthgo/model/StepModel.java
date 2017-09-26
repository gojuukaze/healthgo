package cn.ikaze.healthgo.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by gojuukaze on 16/8/19.
 * Email: i@ikaze.uu.me
 */
public class StepModel extends RealmObject {
    @Index
    private Date date;
    private long numSteps;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(long numSteps) {
        this.numSteps = numSteps;
    }
}
