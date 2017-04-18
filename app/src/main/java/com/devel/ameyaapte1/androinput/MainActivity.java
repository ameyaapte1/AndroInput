package com.devel.ameyaapte1.androinput;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private EditText editText_ip_address;
    private Button button_wifi, button_bluetooth;

    // Called when the activity is first created.
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText_ip_address = (EditText) findViewById(R.id.editText_ip);
        button_wifi = (Button) findViewById(R.id.button_wifi);
        button_bluetooth = (Button) findViewById(R.id.button_bluetooth);

        button_wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip = editText_ip_address.getText().toString();
                if (ip.isEmpty())
                    return;
                Intent intent = new Intent(MainActivity.this, MousePadActivity.class);
                intent.putExtra("connection", "wifi");
                intent.putExtra("ip", ip);
                startActivity(intent);
            }
        });

        button_bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MousePadActivity.class);
                intent.putExtra("connection", "bluetooth");
                startActivity(intent);
            }
        });
    }
}

