package com.example.william.googlemapreference;

        import java.io.File;
        import android.app.Activity;
        import android.app.Dialog;
        import android.content.Intent;
        import android.net.Uri;
        import android.os.Bundle;
        import android.os.Environment;
        import android.provider.MediaStore;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.TextView;
        import android.widget.Toast;
        import android.content.Context;
        import android.database.Cursor;
        import android.database.sqlite.SQLiteDatabase;

public class LocationPhoto extends Activity {
    static int TAKE_PICTURE = 1;
    Uri outputFileUri;
    public double latitude;
    public double longitude;
    final Context context = this;
    SQLiteDatabase mydb;
    public String lastId;
    public String picName;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        mydb = openOrCreateDatabase("locationmarkersqlite", Context.MODE_PRIVATE,null);
        Cursor cursor = mydb.rawQuery("select _id from locations order by _id desc limit 1", null);
        if (cursor.moveToFirst()) {
            do {
                lastId = cursor.getString(0);
            }
            while (cursor.moveToNext());
        }

        cursor.close();
    }



    public void btnTakePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        long seconds = System.currentTimeMillis() / 1000;
        String pic = Long.toString(seconds);
        picName = pic + ".jpg";

        File file = new File(Environment.getExternalStorageDirectory(), picName );
        outputFileUri = Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, TAKE_PICTURE);


    }

    //file:///mnt/sdcard/picname.jpg


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == TAKE_PICTURE && resultCode==RESULT_OK){
            Toast.makeText(this, outputFileUri.toString(), Toast.LENGTH_LONG).show();
            mydb = openOrCreateDatabase("locationmarkersqlite", Context.MODE_PRIVATE,null);
            mydb.execSQL("UPDATE locations set photo= '" + picName + "' where _id =" + lastId);
            //Log.v("update","UPDATE locations set photo= '" + picName + "' where _id =" + lastId);
            Dialog dialog = new Dialog(context);
            dialog.setContentView(R.layout.photo_dialogue);
            dialog.setTitle("Your picture has been saved");
            TextView text = (TextView) dialog.findViewById(R.id.text);
            text.setText("Where would you like to go?");
            ImageView image = (ImageView) dialog.findViewById(R.id.image);
            image.setImageResource(R.drawable.ic_launcher);
            Button dialogButtonMap = (Button) dialog.findViewById(R.id.dialogButtonMap);
            Button dialogButtonNote = (Button) dialog.findViewById(R.id.dialogButtonNote);

            dialogButtonMap.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentMap =new Intent(LocationPhoto.this,MapsActivity.class);
                    startActivity(intentMap);
                }

            });

            dialogButtonNote.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentNote = new Intent(LocationPhoto.this,ViewNotes.class);
                    startActivity(intentNote);
                }

            });
            dialog.show();
        }

    }

}