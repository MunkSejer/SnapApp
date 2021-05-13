package com.example.snapapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements Updatable {

    private Adapter adapter;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String alertText = "";
    private ListView listView;
    private Button takePhoto;
    private ImageView imageView;

    public List<Snap> snapIDs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        takePhoto = findViewById(R.id.takePhoto);

        listViewSetUp();

        Repository.r().setUp(this, snapIDs);
    }

    private void listViewSetUp() {
        listView = findViewById(R.id.listView);
        adapter = new Adapter(snapIDs, this);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) ->{
            // Intent to navigate to SnapViewActivity
            Intent intent = new Intent(MainActivity.this, SnapViewActivity.class);
            intent.putExtra("snapId", snapIDs.get(position).getId());
            startActivity(intent);
        });
    }

    Bitmap imageBitmap;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            //Bitmap manipulatedImage = drawTextToBitmap(alertText);
            //imageView.setImageBitmap(manipulatedImage);
            openAlertDialog();
            //imageView.setImageBitmap(imageBitmap);
        }
    }



    public void selectImageFromGallery(View view){
    }

    public void takePhoto(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }



    public void drawTextToBitmap(String gText) {
        Bitmap.Config bitmapConfig = imageBitmap.getConfig();
        // set default bitmap config if none
        if(bitmapConfig == null) {
            bitmapConfig = Bitmap.Config.ARGB_8888;
        }
        // resource bitmaps are imutable,
        // so we need to convert it to mutable one
        Bitmap manipulatedImageBitmap = imageBitmap.copy(bitmapConfig, true);
        Canvas canvas = new Canvas(manipulatedImageBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);// new antialised Paint
        paint.setColor(Color.rgb(252, 3,3));
        paint.setTextSize((int) (20)); // text size in pixels
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow
        canvas.drawText(gText, 10, 100, paint);
        Repository.r().uploadBitmapToFirebase(manipulatedImageBitmap, gText);
        imageView.setImageBitmap(manipulatedImageBitmap);
        //return manipulatedImageBitmap;
        Toast.makeText(this, "Upload succesfull", Toast.LENGTH_SHORT).show();
    }

    public void openAlertDialog(){
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Add text to image");

         final EditText input = new EditText(this);
         input.setInputType(InputType.TYPE_CLASS_TEXT);
         builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", (dialog, whichBtn) ->{
            //alertText = input.getText().toString();
            drawTextToBitmap(input.getText().toString());
        });
        builder.setNegativeButton("Cancel", (dialog, whichBtn) ->{
            dialog.cancel();
        });

        builder.show();
    }
    public String selectedcolor = "";

    public void selectColorAlert(){
        String[] colors = {"Red", "Blue", "Greem"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ChooseText Color");
        builder.setSingleChoiceItems(colors, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedcolor = colors[which];
                Toast.makeText(MainActivity.this, "Color chosen" + which, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public void update(Object object) {
        adapter.notifyDataSetChanged();
    }
}