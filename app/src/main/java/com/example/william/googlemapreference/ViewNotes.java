package com.example.william.googlemapreference;


        import java.io.File;

        import android.app.Activity;
        import android.os.Bundle;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.content.Context;
        import android.content.Intent;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;


public class  ViewNotes extends Activity {
    /** Called when the activity is first created. */
    SQLiteDatabase mydb;
    public String title;
    public  String body;
    public String picName;
    public String lastId;




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_contents);
        Bundle d = new Bundle();
        d = getIntent().getExtras();
        lastId = d.getString("markerid");

        TextView titleText = (TextView) findViewById(R.id.title);
        TextView bodyText = (TextView) findViewById(R.id.body);

        mydb = openOrCreateDatabase("locationmarkersqlite", Context.MODE_PRIVATE,null);
        //file:///mnt/sdcard/picname.jpg
        Cursor cursor = mydb.rawQuery("select title,body,photo from locations where _id = '" + lastId + "'", null);
        if (cursor.moveToFirst()) {
            do {
                title = cursor.getString(0);
                body = cursor.getString(1);
                picName = cursor.getString(2);
                titleText.setText(title);
                bodyText. setText(body);
                //String path = Environment.getExternalStorageDirectory().getPath();
                //the above is incorrect and reads /mtn/sdcard which is why I use the string below.
                String  path = "/sdcard/";
                File imgFile = new  File(path + picName);
                //works for all externally stored pics...good to save for later.
                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ImageView myImage = (ImageView) findViewById(R.id.locationpic);
                    myImage.setImageBitmap(myBitmap);
                }
            }
            while (cursor.moveToNext());
        }     //close while
        cursor.close();


        Button mapButton = (Button) findViewById(R.id.mapButton);



        //intent to go to map
        mapButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intentMap =new Intent(ViewNotes.this,MapsActivity.class);
                startActivity(intentMap);
            }
        });  //close Method

    }//close onCreate
}//close class
