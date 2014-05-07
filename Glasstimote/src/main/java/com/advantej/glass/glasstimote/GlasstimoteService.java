package com.advantej.glass.glasstimote;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.google.android.glass.timeline.LiveCard;

import java.util.List;

public class GlasstimoteService extends Service {

    private static final String LIVE_CARD_TAG = "BEACONS_CARD";
    private LiveCard mLiveCard;
    private RemoteViews mLiveCardView;


    private BeaconManager mBeaconManager;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private List<Beacon> mBeacons = null;

    private final IBinder mBinder = new MyBinder();

    public class MyBinder extends Binder {
        public GlasstimoteService getService() {
            return GlasstimoteService.this;
        }
    }

    public GlasstimoteService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBeaconManager = new BeaconManager(this);
        mBeaconManager.setRangingListener(mRangingListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        publishOrUpdateLiveCard(0);
        checkBTStatusAndStartRanging();
        return START_STICKY;
    }

    private void checkBTStatusAndStartRanging() {

        // Check if device supports Bluetooth Low Energy.
        if (!mBeaconManager.hasBluetooth()) {
            Toast.makeText(this, getString(R.string.error_bluetooth_le_unsupported), Toast.LENGTH_LONG).show();
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!mBeaconManager.isBluetoothEnabled()) {
            Toast.makeText(this, getString(R.string.error_bluetooth_not_enabled), Toast.LENGTH_LONG).show();
            return;
        } else {
            connectToService();
        }

    }

    private void connectToService() {
        mBeaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                try {
                    mBeaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        mBeaconManager.disconnect();
        unpublishLiveCard();
        super.onDestroy();
    }

    private BeaconManager.RangingListener mRangingListener = new BeaconManager.RangingListener() {
        @Override
        public void onBeaconsDiscovered(Region region, List<Beacon> beacons) {

            mBeacons = beacons;

            int count = beacons.size();
            if (count > 0) {
                publishOrUpdateLiveCard(beacons.size());
            } else if (count == 0) {
                unpublishLiveCard();
            }
        }
    };

    private void publishOrUpdateLiveCard(int beaconCount){

        if (mLiveCard == null) { // create it if does not exist

            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            mLiveCardView = new RemoteViews(getPackageName(), R.layout.beacons_live_card);
            if (beaconCount == 0) {
                mLiveCardView.setTextViewText(R.id.num_beacons, getString(R.string.discovering));
            } else {
                mLiveCardView.setTextViewText(R.id.num_beacons, String.valueOf(beaconCount));
            }

            //IMP : set Action
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));


            //Publish card
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);

        } else {
            mLiveCardView.setTextViewText(R.id.num_beacons, String.valueOf(beaconCount));
            mLiveCard.setViews(mLiveCardView);

            if (!mLiveCard.isPublished()) {
                mLiveCard.publish(LiveCard.PublishMode.SILENT);
            }
//            mLiveCard.navigate();
        }
    }

    private void unpublishLiveCard() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public List<Beacon> getBeacons() {
        return mBeacons;
    }
}
