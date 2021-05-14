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

// TODO; Commented methods: listViewSetUp() line 55, downloadImageFromFirebase(String, ImageView) from the Repository class on line 105

public class MainActivity extends AppCompatActivity implements Updatable {

    private Adapter adapter;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private String alertText = "";
    private ListView listView;
    private Button takePhoto;
    private ImageView imageView;

    private Color color;
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


    // Creates a listView of snaps when the app is started
    private void listViewSetUp() {
        // finds the listView and is stored as a variable
        listView = findViewById(R.id.listView);
        // An adapter is created with a constructor that takes a list of snaps as a parameter
        // The adapter inflates a layout file (itemrow.xml) in the MainActivity (this) layout.
        // in this case, it displays each item of the snapIDs arraylist.
        adapter = new Adapter(snapIDs, this);
        // The created adapter is set into the listView
        listView.setAdapter(adapter);

        // An onItemClickListener is being called whenever an item on the list has been clicked on by the user.
        listView.setOnItemClickListener((parent, view, position, id) ->{
            // Intent to navigate from MainActivity to SnapViewActivity
            Intent intent = new Intent(MainActivity.this, SnapViewActivity.class);
            // Data is being added to the intent to be used in the SnapViewActivity class
            // In this case the id of the snap at the given position is being sent.
            // This position is determined by which index has been clicked by the user.
            intent.putExtra("snapId", snapIDs.get(position).getId());
            // starts the activity
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
            selectColorAlert();
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



    public void drawTextToBitmap(String gText, int rgb) {
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
        paint.setColor(rgb);
        paint.setTextSize((int) (20)); // text size in pixels
        paint.setShadowLayer(1f, 0f, 1f, Color.WHITE); // text shadow
        canvas.drawText(gText, 10, 50, paint);
        Repository.r().uploadBitmapToFirebase(manipulatedImageBitmap, gText);
        imageView.setImageBitmap(manipulatedImageBitmap);
        //return manipulatedImageBitmap;
        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show();
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
            drawTextToBitmap(input.getText().toString(), rgb);
        });
        builder.setNegativeButton("Cancel", (dialog, whichBtn) ->{
            dialog.cancel();
        });

        builder.show();
    }
    public String selectedcolor = "";
    public int rgb;
    public void selectColorAlert(){
        String[] colors = {"Red", "Blue", "Green", "Pink"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose text color");
        builder.setSingleChoiceItems(colors, 0, (dialog, which) -> {
            selectedcolor = colors[which];
            if(selectedcolor.equals("Red")){
                rgb = Color.rgb(255,0,0);
            }
            if(selectedcolor.equals("Blue")){
                rgb = Color.rgb(0,0,255);
            }
            if(selectedcolor.equals("Green")){
                rgb = Color.rgb(0,255,0);
            }
            if(selectedcolor.equals("Pink")){
                rgb = Color.rgb(255, 82, 223);
            }
            //Toast.makeText(MainActivity.this, "Color chosen" + which, Toast.LENGTH_SHORT).show();
        });
        builder.setPositiveButton("Proceed", (dialog, which) -> {
            openAlertDialog();
            dialog.dismiss();
        });
        builder.setNegativeButton("Exit", (dialog, which) ->
            dialog.dismiss());
            builder.show();
    }

    @Override
    public void update(Object object) {
        adapter.notifyDataSetChanged();
    }
}