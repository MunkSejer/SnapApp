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

    private static Repository repository = new Repository();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
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

    //https://www.youtube.com/watch?v=7QnhepFaMLM&list=PLdHg5T0SNpN2NimxW3piNqEVBWtXcraz-&index=28
    public void downloadImageFromFirebase(String snapId, ImageView imageView){
        StorageReference imageReference = storage.getReference()
                .child("snapshots")
                .child(snapId);


        imageReference.getBytes(1024*1024)
                .addOnSuccessListener((bytes) -> {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageView.setImageBitmap(bitmap);
                })
                .addOnFailureListener( e -> {
                    Log.e(TAG, "onFailure: ", e);

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

    public void addNoteToFirebase(String new_note) {
        // insert a new note with "new note"
        DocumentReference reference = db.collection(NOTES).document();
        // creates new document to get id
        Note note = new Note(reference.getId(), new_note);
        // reference has a
        Map<String, String> stringMap = new HashMap<>();
        stringMap.put("title", new_note);
        // Will replace any prev. values.
        reference.set(stringMap);
        System.out.println("Done inserting: " + reference.getId());
    }



    public void deleteNote(String id){
        DocumentReference docRef = db.collection(NOTES).document(id);
        docRef.delete();
    }

    public void updateNoteAndImage(Note note, Bitmap bitmap) {
        updateNote(note);
        StorageReference ref = storage.getReference(note.getId());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ref.putBytes(baos.toByteArray()).addOnCompleteListener(snap -> {
            System.out.println("OK to upload " + snap);
        }).addOnFailureListener(exception -> {
            System.out.println("failure to upload " + exception);
        });
    }

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

