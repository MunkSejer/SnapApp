package com.example.snapapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SnapViewActivity extends AppCompatActivity {

    private ImageView imageView;
    private Snap currentSnap;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snap_view);

        imageView = findViewById(R.id.snapViewImageView);
        textView = findViewById(R.id.showIdTextView);
        String snapID = getIntent().getStringExtra("snapId");
        currentSnap = Repository.r().getSnapWithId(snapID);

        textView.setText(currentSnap.getId());

        Repository.r().downloadImageFromFirebase(snapID, imageView);
    }


    public void closeDeleteBtnPressed(View view){
        Toast.makeText(this, "Snap deleted", Toast.LENGTH_LONG).show();
        String snapID = getIntent().getStringExtra("snapId");
        Repository.r().deleteSnap(snapID);
        Repository.r().removeSnapFromStorage(snapID);
        this.finish();
    }
}