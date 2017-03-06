package com.digzdigital.androidthings_sample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String PIR_GPIO_PIN_NAME = "BCM4";
    private static final String LED_GPIO_PIN_NAME = "BCM19";

    private Gpio pirGpio, ledGpio;
    private PeripheralManagerService managerService;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        managerService = new PeripheralManagerService();
        List<String> portList = managerService.getGpioList();
        if (!portList.isEmpty()) Log.i(TAG, "List of available ports:" + portList);
        else Log.i(TAG, "No available ports");

        databaseReference = FirebaseDatabase.getInstance().getReference();
        try {
            configurePirGpio();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            configureLedGpio();
        } catch (IOException e) {
            e.printStackTrace();
        }
monitorLedState();

    }

    private void monitorLedState(){
        databaseReference.child("rpi").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean ledState = (Boolean) dataSnapshot.child("ledOn").getValue();
                try {
                    switchLedOn(ledState);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void configurePirGpio() throws IOException {
        pirGpio = managerService.openGpio(PIR_GPIO_PIN_NAME);
        pirGpio.setActiveType(Gpio.ACTIVE_HIGH);
        pirGpio.setDirection(Gpio.DIRECTION_IN);
        pirGpio.setEdgeTriggerType(Gpio.EDGE_RISING);
        pirGpio.registerGpioCallback(callback);
    }

    private void configureLedGpio()throws IOException{
        ledGpio = managerService.openGpio(LED_GPIO_PIN_NAME);
        ledGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
        ledGpio.setActiveType(Gpio.ACTIVE_HIGH);
    }

    private void switchLedOn(Boolean ledOn) throws IOException {
        ledGpio.setValue(ledOn);
    }

    private GpioCallback callback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            //Do required action when pir is triggered
            return true;
        }
    };

    @Override
    protected void onDestroy(){
        super.onDestroy();
        closeGpios();
    }

    private void closeGpios() {
        try {
            closePirGpio();
        }
        catch (IOException e) {
            Log.w(TAG, "Unable to close GPIO", e);
        }
        try {
            closeLedGpio();
        } catch (IOException e) {
            e.printStackTrace();
            Log.w(TAG, "Unable to close GPIO", e);
        }
    }

    private void closeLedGpio() throws IOException {
        if (ledGpio !=null)ledGpio.close();

    }

    private void closePirGpio() throws IOException {
        if (pirGpio !=null)pirGpio.close();

    }
}
