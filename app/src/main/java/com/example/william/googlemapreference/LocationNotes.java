package com.example.william.googlemapreference;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class LocationNotes extends Activity {
    private EditText mTitleText;
    private EditText mBodyText;
    SQLiteDatabase mydb;
    final Context context = this;
    public String latitude;
    public String longitude;

    public String lastId;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {


        //need to get last _id

        mydb = openOrCreateDatabase("locationmarkersqlite", Context.MODE_PRIVATE,null);
        Cursor cursor = mydb.rawQuery("select _id from locations order by _id desc limit 1", null);
        if (cursor.moveToFirst()) {
            do {
                lastId = cursor.getString(0);

            }
            while (cursor.moveToNext());
        }

        cursor.close();





//set views

        setContentView(R.layout.notes);
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        Button confirmButton = (Button) findViewById(R.id.confirm);

        super.onCreate(savedInstanceState);
		/*
		 * By calling super.onCreate(savedInstanceState);,
		 * you tell the Dalvik VM to run your code in addition to the existing code in the onCreate()
		 * of the parent class. If you leave out this line, then only your code is run. The existing code is
		 *  ignored completely. However, you must include this super call in your method, because
		 *  if you don't then the onCreate() code in Activity is never run, and your app will run
		 *   into all sorts of problem like having no Context assigned to the Activity
		 *   (though you'll hit a SuperNotCalledException before you have a chance to figure out that
		 *    you have no context). In short, Android's own classes can be incredibly complex.
		 *    The code in the framework classes handles stuff like UI drawing, house cleaning and maintaining the
		 *    Activity and application lifecycles. super calls allow developers to run this complex code behind the scenes, while still providing a good level of abstraction for our own apps.
		 *  What activities are we running here? Will it work without it?
		 */

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //updating record in sqlite based on id
                mydb = openOrCreateDatabase("locationmarkersqlite", Context.MODE_PRIVATE,null);
                mydb.execSQL("UPDATE locations set title= '" + mTitleText.getText().toString() + "', body = '" + mBodyText.getText().toString() + "' where _id =" + lastId);
                //creating response dialog
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.notes_dialogue);
                dialog.setTitle("Your note has been saved");
                TextView text = (TextView) dialog.findViewById(R.id.text);
                text.setText("Would you like to save a photo?");
                ImageView image = (ImageView) dialog.findViewById(R.id.image);
                image.setImageResource(R.drawable.ic_launcher);
                Button dialogButtonMap = (Button) dialog.findViewById(R.id.dialogButtonMap);
                Button dialogButtonPhoto = (Button) dialog.findViewById(R.id.dialogButtonPhoto);
                //intent to goback to maps
                dialogButtonMap.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentMap =new Intent(LocationNotes.this,MapsActivity.class);
                        startActivity(intentMap);
                    }

                });
                //intent to go to photo
                dialogButtonPhoto.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentPhoto =new Intent(LocationNotes.this,LocationPhoto.class);
                        startActivity(intentPhoto);
                    }

                });
                dialog.show();


            }

        });

    }

}