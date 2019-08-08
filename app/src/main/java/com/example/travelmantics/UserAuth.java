package com.example.travelmantics;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class UserAuth {
    private static final int RC_SIGN_IN = 9001;

    static FirebaseAuth firebaseAuth;
    private static FirebaseAuth.AuthStateListener firebaseAuthListener;
    final ArrayList<Activity> mActivities = new ArrayList<>();

    static FirebaseStorage firebaseStorage;
    static StorageReference firebaseStorageRef;

    static boolean isAdmin;

    private UserAuth(){}

    static void init(final Activity initContext){
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    signIn(initContext);
                }
                else{
                    FirebaseFirestore fireDB = FirebaseFirestore.getInstance();

                    DocumentReference docRef = fireDB.collection("admins")
                            .document(firebaseAuth.getCurrentUser().getUid());

                    docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot.exists()){
                                isAdmin = true;
                                initContext.invalidateOptionsMenu();

                                Toast.makeText(initContext, R.string.admin_prompt,
                                        Toast.LENGTH_SHORT).show();
                            }
                            else{
                                isAdmin = false;
                            }
                        }
                    });
                    Toast.makeText(initContext, R.string.sign_in_message, Toast.LENGTH_SHORT).show();
                }
            }
        };

        connectStorage();
    }

    private static void signIn(Activity initContext) {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());


        // Create and launch sign-in intent
        initContext.startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    static void attachAuthListener(){
        firebaseAuth.addAuthStateListener(firebaseAuthListener);
    }

    static void detachAuthListener(){
        firebaseAuth.removeAuthStateListener(firebaseAuthListener);
    }

    private static void connectStorage(){
        firebaseStorage = FirebaseStorage.getInstance();
        firebaseStorageRef = firebaseStorage.getReference().child("deals_pictures");
    }
}
