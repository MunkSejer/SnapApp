package com.example.snapapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class Repository {


    private final String COLLECTION_NAME = "snaps";

    private static final Repository repository = new Repository();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    public List<Snap> snapList = new ArrayList<>();

    private Updatable activity;

    //Private constructor
    private Repository(){}

    //Singleton
    public static Repository r() {
        return repository;
    }

    public void setUp(Updatable act, List<Snap> snapShots) {
        activity = act;
        this.snapList = snapShots;
        startListener();
    }

    public void startListener() {
        db.collection(COLLECTION_NAME).addSnapshotListener((values, error) -> {
            snapList.clear();
            assert values != null;
            for (DocumentSnapshot documentSnapshot : values.getDocuments()) {

                Object title = documentSnapshot.get("title");
                System.out.println("snapshot found: " + documentSnapshot.toString());

                snapList.add(new Snap(documentSnapshot.getId(), title.toString()));
            }
            //Update list
            activity.update(null);
        });
    }

    public void uploadBitmapToFirebase(Bitmap bitmap, String text) {

        DocumentReference reference = db.collection(COLLECTION_NAME).document();
        Map<String, String> map = new HashMap<>();
        map.put("title", text);
        // Will replace any prev. values.
        reference.set(map);
        System.out.println("Done inserting: " + reference.getId());

        StorageReference ref = storage.getReference("snapshots/" + reference.getId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ref.putBytes(baos.toByteArray())
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Log.d(TAG, "Image camture complete");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "onFailure: ", e);
                });
    }

    public void downloadImageFromFirebase(String snapId, ImageView imageView){
        StorageReference imageReference = storage.getReference()
                // We then call .child til get through the archives in the firebase storage.
                // snapId is passed as a method parameter to find the correct picture.
                .child("snapshots")
                .child(snapId);
        // This picture is downloaded as bytes with a max size of 1mb.
        imageReference.getBytes(1024*1024)
                // Since getBytes() is a task, we need to call a successListener.
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // To get the picture from the byte array, we have to change it to a bitmap, and we
                        // do that by using the decodeByteArray() method from the BitmapFactory class.
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        // At last the bitmap is set to the imageView, which is passed as parameter in the method header.
                        imageView.setImageBitmap(bitmap);
                    }
                })
                // An onFailureListener is added, and will execute, if there were no picture found with the given snapId.
                .addOnFailureListener( e -> {
                    Log.e(TAG, "onFailure: ", e);
                    // a new storage reference is made to a static picture
                    // and set to the imageview to be displayed to the user.
                    StorageReference errorImageReference = storage.getReference()
                            .child("errorimage")
                            .child("notfound.jpg");

                            errorImageReference.getBytes(1024*1024)
                                .addOnSuccessListener(bytes -> {
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    imageView.setImageBitmap(bitmap);
                            });
                });
    }

    public Snap getSnapWithId(String id){
        for(Snap snap : snapList){
            if(snap.getId().equals(id)){
                return snap;
            }
        }
        return null;
    }

    public void deleteSnap(String snapId) {
        db.collection("snaps")
                .document(snapId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSucces: document deleted..");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                });
    }

    public void removeSnapFromStorage(String snapId){
        storage.getReference()
                .child("snapshots")
                .child(snapId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "onSucces: Storage image deleted..");
                })
                .addOnFailureListener(e ->{
                    Log.e(TAG, "onFailure: ", e);
                });

    }

/*
    public void downloadBitmap(String id, TaskListener taskListener){ // when to call this method?
        StorageReference ref = storage.getReference(id);
        int max = 1024 * 1024; // you are free to set the limit here
        ref.getBytes(max).addOnSuccessListener(bytes -> {
            taskListener.receive(bytes); // god linie!
            System.out.println("Download OK");
        }).addOnFailureListener(ex -> {
            System.out.println("error in download " + ex);
        });
    }
*/
}

