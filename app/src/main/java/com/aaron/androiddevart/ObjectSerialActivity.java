package com.aaron.androiddevart;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectSerialActivity extends AppCompatActivity {
    private static final String CACHE_FILE = "cache.txt";

    private TextView mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_serial);

        mContent = (TextView) findViewById(R.id.content);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveObject();
            }
        });

        findViewById(R.id.read_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readObject();
            }
        });
    }

    private void readObject() {
        ObjectInputStream is = null;
        try {
            is = new ObjectInputStream(openFileInput(CACHE_FILE));
            User user = (User) is.readObject();

            String content = "My name is " + user.getName() + ", id is " + user.getId() + ", is " + (user.isMale() ? "male" : "female.");
            mContent.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if ( is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void saveObject() {
        ObjectOutputStream os = null;
        try {
            os = new ObjectOutputStream(openFileOutput(CACHE_FILE, MODE_PRIVATE));
            User user = new User(0, "Jack", true);
            os.writeObject(user);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
