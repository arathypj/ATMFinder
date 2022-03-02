package com.example.megamonsterbankapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class LoginActivity extends AppCompatActivity {

    SQLiteDatabase myDB;
    Cursor cursor;
    boolean loginStatus;
    boolean userExistStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Switch registerSwitch = (Switch) findViewById(R.id.switch1);
        Button signin = (Button)findViewById(R.id.signin);
        Button signup = (Button)findViewById(R.id.signup);
        signup.setEnabled(false);

        TextView t1 = (TextView) findViewById(R.id.textView);
        EditText e1 = (EditText)findViewById(R.id.email);
        EditText e2 = (EditText)findViewById(R.id.password);

        registerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    signin.setEnabled(false);
                    signup.setEnabled(true);
                }else{
                    signup.setEnabled(false);
                    signin.setEnabled(true);
                }
            }
        });




        File dbFile = new File(getFilesDir() + "/myDB.db");
        if (!dbFile.exists())
        {
            Log.i("SqlLiteExample", "File doesn't exist");

            myDB = SQLiteDatabase.openOrCreateDatabase(getFilesDir() + "/myDB.db", null);

            myDB.execSQL("create table atmlocations(id INTEGER PRIMARY KEY AUTOINCREMENT, title text, latitude float, longitude float)");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM1','52.0431','-0.7571')");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM2','52.7381','-0.9071')");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM3','52.9498','-0.8365')");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM4','52.3409','-1.2541')");
            myDB.execSQL("insert into atmlocations (title,latitude, longitude) values('ATM5','52.0431','-0.9120')");

            myDB.execSQL("create table loginDetails (id INTEGER PRIMARY KEY AUTOINCREMENT, email text, password text)");
            myDB.execSQL("insert into loginDetails (email, password) values ('arathy123@gmail.com', '123')");
            myDB.execSQL("insert into loginDetails (email, password) values ('arathy333@gmail.com', '345')");
            myDB.execSQL("insert into loginDetails (email, password) values ('reghu6988@gmail.com', '567')");
            myDB.close();
        }else{
            Log.i("SqlLiteExample", "File exist");

        }


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myDB = SQLiteDatabase.openOrCreateDatabase(getFilesDir() + "/myDB.db", null);
                cursor = myDB.rawQuery("select * from loginDetails", null);
                cursor.moveToFirst();

                loginStatus = false;

                String email = e1.getText().toString();
                String pswd = e2.getText().toString();

               while(!cursor.isAfterLast()) {
                    int e = cursor.getColumnIndex("email");
                    int p = cursor.getColumnIndex("password");

                   if (email.equals(cursor.getString(e)) && pswd.equals(cursor.getString(p))){
                       loginStatus = true;
                       Intent i1 = new Intent(LoginActivity.this, ChatActivity.class);
                       startActivity(i1);

                       //t1.setText(cursor.getString(e));
                       break;

                   }else{
                       cursor.moveToNext();
                   }

                }

               if (loginStatus==false){
                   t1.setText("Incorrect email or password");
               }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = e1.getText().toString();
                String pswd = e2.getText().toString();

                userExistStatus = false;

                myDB = SQLiteDatabase.openOrCreateDatabase(getFilesDir() + "/myDB.db", null);
                cursor = myDB.rawQuery("select * from loginDetails", null);
                cursor.moveToFirst();

                while(!cursor.isAfterLast()) {
                    int e = cursor.getColumnIndex("email");

                    if (email.equals(cursor.getString(e))){
                        userExistStatus = true;
                        break;
                    }else{
                        cursor.moveToNext();
                    }

                }

                if (userExistStatus==true){
                    t1.setText("Email already registered");
                }else{
                    cursor.moveToLast();
                    myDB.execSQL("insert into loginDetails (email, password) values ('" + email + "', '" + pswd + "')");

                    Intent i1 = new Intent(LoginActivity.this, ChatActivity.class);
                    startActivity(i1);
                }


            }
        });

    }
}