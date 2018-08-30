package com.example.lawrene.selfwithdrawal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConfigureActivity extends AppCompatActivity {

    EditText pinEdt;
    Button saveBtn;
    String amount, newPin;
    DatabaseAccess databaseAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure);

        databaseAccess = DatabaseAccess.getInstance(this);
        databaseAccess.open();

        pinEdt = findViewById(R.id.pinEdt);
        saveBtn = findViewById(R.id.saveBtn);

        pinEdt.setText(databaseAccess.getPin());

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveNew(pinEdt.getText().toString());
            }
        });
    }

    public void saveNew(String pin){
        if(pinEdt.getText().toString().length() < 4){
            Toast.makeText(this, "Pin cannot be less than 4 digits", Toast.LENGTH_SHORT).show();
        }

        else{
            databaseAccess.storePin(pin);
            Toast.makeText(this, "Pin saved", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onBackPressed() {
//        finish();
//        super.onBackPressed();
    }
}
