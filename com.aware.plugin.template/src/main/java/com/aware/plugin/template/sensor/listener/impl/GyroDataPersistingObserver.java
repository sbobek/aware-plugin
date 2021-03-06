package com.aware.plugin.template.sensor.listener.impl;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import com.aware.plugin.template.Provider;
import com.aware.plugin.template.sensor.listener.MetaWearAsyncSensorDataPersistingObserver;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.MetaWearBoard;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.Subscriber;
import com.mbientlab.metawear.data.AngularVelocity;
import com.mbientlab.metawear.module.GyroBmi160;

import java.sql.Timestamp;
import java.util.Calendar;

import bolts.Continuation;

/**
 * Created by lmarek on 02.01.2018.
 */

public class GyroDataPersistingObserver extends MetaWearAsyncSensorDataPersistingObserver {

    public GyroDataPersistingObserver(Context context) {
        super(context);
    }

    @Override
    protected ContentValues convertToDatabaseRecord(Data data) {
        final AngularVelocity velocity = data.value(AngularVelocity.class);
        final Calendar timestamp = data.timestamp();

        final ContentValues contentValues = new ContentValues();

        final Timestamp sqlTimestamp = new Timestamp(timestamp.getTime().getTime());
        contentValues.put(Provider.Velocity_Data.TIMESTAMP, sqlTimestamp.toString());
        contentValues.put(Provider.Velocity_Data.X, velocity.x());
        contentValues.put(Provider.Velocity_Data.Y, velocity.y());
        contentValues.put(Provider.Velocity_Data.Z, velocity.y());

        return contentValues;
    }

    public void register(MetaWearBoard metaWearBoard) {
        final GyroBmi160 gyroBmi160 = metaWearBoard.getModule(GyroBmi160.class);

        if (gyroBmi160 != null) {

            gyroBmi160.configure().odr(GyroBmi160.OutputDataRate.ODR_25_HZ).commit();


            gyroBmi160.angularVelocity().addRouteAsync(source -> {
                source.limit(DATA_LIMIT_IN_MILLISECONDS);
                source.stream((Subscriber) (data, env) -> processData(data));
            }).continueWith((Continuation<Route, Void>) task -> {

                addTerminationTask(() -> {
                    gyroBmi160.angularVelocity().stop();
                    gyroBmi160.stop();
                });

                gyroBmi160.angularVelocity().start();
                gyroBmi160.start();
                return null;
            });

        }

    }

    @Override
    protected Uri getDatabaseContentUri() {
        return Provider.Velocity_Data.CONTENT_URI;
    }
}
