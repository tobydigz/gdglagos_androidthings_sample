package com.digzdigital.companionapp;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.digzdigital.companionapp.databinding.ActivityMainBinding;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityMainBinding binding;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.toggleLedButton.setOnClickListener(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.toggleLedButton:
                switchLedOnorOff();
                break;
        }
    }

    private void switchLedOnorOff() {
        Boolean ledOn = false;
        String ledState = "Led Off";
        if (binding.toggleLedButton.isChecked()) {
            ledOn = true;

            ledState = "Led On";
        }
        setLedStateText(ledState);
        writeDataToFirebase(ledOn);

    }

    private void setLedStateText(String ledState) {
        binding.ledStateText.setText(ledState);
    }

    private void writeDataToFirebase(Boolean ledOn) {
        databaseReference.child("rpi").child("ledOn").setValue(ledOn);
    }
}
