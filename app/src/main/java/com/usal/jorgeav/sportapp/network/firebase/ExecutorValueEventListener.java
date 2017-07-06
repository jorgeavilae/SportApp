package com.usal.jorgeav.sportapp.network.firebase;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

/**
 * Created by Jorge Avila on 27/06/2017.
 */

/* ValueEventListener callbacks are invoked in the UI main thread so for heavy operations,
 * such as store data in Content Provider, it recommended to use another thread
 * https://stackoverflow.com/a/39060380/4235666
 *
 * Source:
 * https://github.com/CodingDoug/white-label-event-app/commit/917ff279febce1977635226fe9181cc1ff099656
 * From:
 * https://stackoverflow.com/a/35777599/4235666
 */
// TODO: 27/06/2017 memory leak
public abstract class ExecutorValueEventListener implements ValueEventListener {
    private final Executor executor;
    public ExecutorValueEventListener(final Executor executor) {
        this.executor = executor;
    }

    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                onDataChangeExecutor(dataSnapshot);
            }
        });
//        new AsyncTask<Object, Void, Void>() {
//            @Override
//            protected Void doInBackground(Object... objects) {
//                onDataChangeExecutor((DataSnapshot) objects[0]);
//                return null;
//            }
//        }.execute(dataSnapshot);
    }

    @Override
    public void onCancelled(final DatabaseError databaseError) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                onCancelledExecutor(databaseError);
            }
        });
//        new AsyncTask<Object, Void, Void>() {
//            @Override
//            protected Void doInBackground(Object... objects) {
//                onCancelledExecutor((DatabaseError) objects[0]);
//                return null;
//            }
//        }.execute(databaseError);
    }

    protected abstract void onDataChangeExecutor(DataSnapshot dataSnapshot);
    protected abstract void onCancelledExecutor(DatabaseError databaseError);
}
