package com.example.travelmantics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class DealManagerActivity extends AppCompatActivity {

    final String TAG = "FireStore";
    public static final int RC_GET_IMAGE = 9042;
    FirebaseFirestore fireDB;

    EditText edTxtTitle;
    EditText edTxtDescription;
    EditText edTxtPrice;
    TravelDeal travelDeal;
    ImageView imvScenery;

    public void saveDeal(){
        travelDeal.setTitle(edTxtTitle.getText().toString());
        travelDeal.setDescription(edTxtDescription.getText().toString());
        travelDeal.setPrice(edTxtPrice.getText().toString());

        DocumentReference docRef;

        if(UserAuth.firebaseAuth.getUid() == null){
            Toast.makeText(DealManagerActivity.this, R.string.sign_in_prompt,
                    Toast.LENGTH_SHORT).show();
            Log.w("UserAuth", "Returned!");
            return;
        }

        if(travelDeal.getId() == null){
            docRef = fireDB.collection("trips").document();
        }
        else{
            docRef = fireDB.collection("trips").document(travelDeal.getId());
        }

        travelDeal.setId(docRef.getId());

        docRef.set(travelDeal, SetOptions.merge())
                 .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "DocumentSnapshot successfully written!");
                Toast.makeText(DealManagerActivity.this, R.string.new_trip_message,
                        Toast.LENGTH_SHORT).show();
                travelDeal = new TravelDeal();
                clean();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DealManagerActivity.this, R.string.save_fail_message,
                                Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public void deleteDeal(){
        if(travelDeal == null || UserAuth.firebaseAuth.getUid() == null){
            Toast.makeText(DealManagerActivity.this,
                    R.string.delete_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if(UserAuth.firebaseAuth.getUid() == null){
            Toast.makeText(DealManagerActivity.this,
                    R.string.delete_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        fireDB.collection("trips").document(travelDeal.getId()).delete()
        .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                StorageReference imageRef = UserAuth.firebaseStorage
                        .getReferenceFromUrl(travelDeal.getImgRef());

                imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(DealManagerActivity.this,
                                R.string.delete_success, Toast.LENGTH_SHORT).show();

                        clean();
                    }
                });
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(DealManagerActivity.this,
                        R.string.delete_failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshImage(){
        imvScenery.setVisibility(View.VISIBLE);

        try {
            StorageReference imageRef = UserAuth.firebaseStorage
                    .getReferenceFromUrl(this.travelDeal.getImgRef());

            GlideApp.with(imvScenery.getContext())
                    .load(imageRef)
                    .into(imvScenery);
        }
        catch(Exception e){
            Toast.makeText(this, R.string.no_image, Toast.LENGTH_SHORT).show();
        }
    }

    public void clean(){
        edTxtTitle.setText("");
        edTxtPrice.setText("");
        edTxtDescription.setText("");

        edTxtTitle.requestFocus();

        imvScenery.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal_manager);

        edTxtTitle = findViewById(R.id.txtTitle);
        edTxtPrice = findViewById(R.id.txtPrice);
        edTxtDescription = findViewById(R.id.txtDescription);
        imvScenery = findViewById(R.id.imvScenery);
        Button btnUpImage = findViewById(R.id.btnUpImage);

        btnUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(UserAuth.isAdmin){
                    Intent upImageIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    upImageIntent.setType("image/jpeg");
                    upImageIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(upImageIntent,
                            "Choose New Scene"), RC_GET_IMAGE);
                }
                else{
                    Toast.makeText(DealManagerActivity.this,
                            "You haven't been verified as an admin", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fireDB = FirebaseFirestore.getInstance();

        Intent incomingIntent = getIntent();

        TravelDeal travelDeal = (TravelDeal) incomingIntent
                .getSerializableExtra("travelDeal");

        if(travelDeal != null){
            this.travelDeal = travelDeal;

            edTxtTitle.setText(this.travelDeal.getTitle());
            edTxtDescription.setText(this.travelDeal.getDescription());
            edTxtPrice.setText(this.travelDeal.getPrice());

            refreshImage();
        }
        else{
            if(this.travelDeal == null){
                this.travelDeal = new TravelDeal();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == RC_GET_IMAGE && resultCode == RESULT_OK && data != null){
            final Uri imgUri = data.getData();
            if(imgUri != null && imgUri.getLastPathSegment() != null){
                final StorageReference ref = UserAuth.firebaseStorageRef
                        .child(imgUri.getLastPathSegment());
                ref.putFile(imgUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                        travelDeal.setImgRef(ref.toString()
                                .replaceAll("%3A", ":"));

                        Toast.makeText(DealManagerActivity.this,
                                R.string.img_up_success, Toast.LENGTH_SHORT).show();

                        GlideApp.with(DealManagerActivity.this)
                                .load(imgUri).into(imvScenery);
                    }
                });
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.manage_deals_menu, menu);

        menu.findItem(R.id.delete).setVisible(UserAuth.isAdmin);
        menu.findItem(R.id.save).setVisible(UserAuth.isAdmin);

        enableEditTexts(UserAuth.isAdmin);

        findViewById(R.id.btnUpImage).setEnabled(UserAuth.isAdmin);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save:
                saveDeal();
                return true;
            case R.id.delete:
                deleteDeal();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void enableEditTexts(boolean isEnabled){
        edTxtTitle.setEnabled(isEnabled);
        edTxtPrice.setEnabled(isEnabled);
        edTxtDescription.setEnabled(isEnabled);
    }
}
