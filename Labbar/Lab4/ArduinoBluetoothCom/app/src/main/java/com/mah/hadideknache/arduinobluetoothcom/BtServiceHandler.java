package com.mah.hadideknache.arduinobluetoothcom;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by hello on 2017-12-28.
 */

public class BtServiceHandler {


    private BtService mBoundService;
    private boolean mIsBound = false;
    private ServiceConnection mConnection;

    public void doBindService(MainActivity main) {
        Log.d("LocalHandler", "doBindService " );
        // Establish a connection with the service.  We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        Intent intent = new Intent(main,BtService.class);
        main.startService(intent);

        mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder binder) {
                // This is called when the connection with the service has been
                // established, giving us the service object we can use to
                // interact with the service.  Because we have bound to a explicit
                // service that we know is running in our own process, we can
                // cast its IBinder to a concrete class and directly access it.
                BtService.LocalService ls = (BtService.LocalService) binder;
                mBoundService = ls.getService();
                mIsBound = true;

                // Tell the user about this for our demo.
                //  Toast.makeText(main, "Service Connected", Toast.LENGTH_SHORT).show();
                Log.d("LocalHandler", "ServiceConnected " );
            }
            public void onServiceDisconnected(ComponentName className) {
                // This is called when the connection with the service has been
                // unexpectedly disconnected -- that is, its process crashed.
                mBoundService = null;
                mIsBound = false;
                Log.d("LocalHandler", "ServiceDisconnected " );
            }
        };


        boolean result = main.getApplicationContext().bindService(intent, mConnection, Context.MODE_PRIVATE);
        if (!result){
            Log.d("LocalHandler", "No binding");

        }else{
            Log.d("LocalHandler", "BINDING");
        }




        /*
        main.bindService(new Intent(main,
                BtService.class), mConnection,Context.BIND_AUTO_CREATE );*/

    }

    public void doUnbindService(MainActivity mainActivity) {
        if (mIsBound) {
            // Detach our existing connection.
            mIsBound = false;
            mainActivity.getApplicationContext().unbindService(mConnection);

        }
    }


    public void startReceive() {
        mBoundService.startReceive();
    }

    public void connectDevice() {
        mBoundService.initConnection();
    }

    public void disconnectBT() {
        mBoundService.disconnectBt();

    }

    public void stopService(MainActivity mainActivity) {
        mBoundService.stopService(new Intent(mainActivity, BtService.class));
    }
}
