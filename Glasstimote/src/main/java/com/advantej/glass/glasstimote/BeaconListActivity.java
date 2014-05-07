package com.advantej.glass.glasstimote;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.ViewGroup;

import com.estimote.sdk.Beacon;
import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tejas on 5/6/14.
 */
public class BeaconListActivity extends Activity {

    private GlasstimoteService mService;

    private List<Beacon> mBeaconList;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {

            mService = ((GlasstimoteService.MyBinder)binder).getService();

            if (mService != null) {
                mBeaconList = mService.getBeacons();
                setUpBeaconList();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, GlasstimoteService.class), mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mServiceConnection);
    }

    private void setUpBeaconList() {

        CardScrollView cardScrollView = new CardScrollView(this);
        BeaconsAdapter beaconsAdapter = new BeaconsAdapter(this, mBeaconList);
        cardScrollView.setAdapter(beaconsAdapter);
        cardScrollView.activate();
        setContentView(cardScrollView);
    }

    private class BeaconsAdapter extends CardScrollAdapter {

        List<Card> mCardList = new ArrayList<Card>();

        private BeaconsAdapter(Context context, List<Beacon> beacons) {
            Card tmpCard;

            for (Beacon beacon : beacons) {

                tmpCard = new Card(context);
                tmpCard.addImage(R.drawable.beacon_gray);
                tmpCard.setText(beacon.getName() + ":" + beacon.getMacAddress() + "\n Power : " + beacon.getMeasuredPower());
                tmpCard.setFootnote(String.format("Major:%s  Minor:%s", beacon.getMajor(), beacon.getMinor()));

                mCardList.add(tmpCard);

            }
        }

        @Override
        public int getCount() {
            return mCardList.size();
        }

        @Override
        public Card getItem(int i) {
            return mCardList.get(i);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return getItem(i).getView();
        }

        @Override
        public int getPosition(Object o) {
            return 0;
        }
    }
}
