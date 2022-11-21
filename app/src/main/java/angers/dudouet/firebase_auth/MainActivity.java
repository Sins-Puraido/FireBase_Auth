package angers.dudouet.firebase_auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    // variable pour la classe FirebaseAuth qui defini la session de l'utilisateur
    private FirebaseAuth mAuth;
    // variable for our text input
    // field for phone and OTP.
    private EditText edtPhone, edtOTP;
    // buttons for generating OTP, verifying OTP and connect by Yahoo
    private Button verifyOTPBtn, generateOTPBtn;
    // string for storing our verification ID
    private String verificationId;

    private Button connectYahoo;



    private String phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //recuperation de l'instance de l'authentification firebase
        mAuth = FirebaseAuth.getInstance();

        // initializing variables for button and Edittext.

        //for yahoo
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("yahoo.com");
        connectYahoo = findViewById(R.id.idBnlogWthYahoo);

        //for phone
        edtPhone = findViewById(R.id.idEdtPhoneNumber);
        edtOTP = findViewById(R.id.idEdtOtp);
        verifyOTPBtn = findViewById(R.id.idBtnVerify);
        generateOTPBtn = findViewById(R.id.idBtnGetOtp);




        // setting onclick listener for generate OTP button.
        generateOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    // when mobile number text field is empty
                    // displaying a toast message.
                    Toast.makeText(MainActivity.this, "Veuillez rentrer un numero de telephone valide.", Toast.LENGTH_SHORT).show();
                } else {
                    // if the text field is not empty we are calling our
                    // send OTP method for getting OTP from Firebase.
                    phone = "+33" + edtPhone.getText().toString();
                    sendVerificationCode(phone);
                }
            }
        });

        // initializing on click listener
        // for verify otp button
        verifyOTPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validating if the OTP text field is empty or not.
                if (TextUtils.isEmpty(edtOTP.getText().toString())) {
                    // si l'OTP est vide afficher un message d'erreur
                    Toast.makeText(MainActivity.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else {
                    // si l'OTP n'est pas vide, lancer la methode pour verifier l'OTP
                    verifyCode(edtOTP.getText().toString());
                }
            }
        });


        connectYahoo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth
                        .startActivityForSignInWithProvider(/* activity= */ MainActivity.this, provider.build())
                        .addOnSuccessListener(

                                //cas ou l'authentification par Yahoo est un succée
                                new OnSuccessListener<AuthResult>() {
                                    @Override
                                    public void onSuccess(AuthResult authResult) {
                                        Intent i = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                })
                        .addOnFailureListener(
                                //cas ou l'authentification par Yahoo est un échec
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Gère les erreurs
                                    }
                                });
            }
        });
    }


    private void signInWithCredential(PhoneAuthCredential credential) {
        //verifie si le code entrer est bon
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) { //case were the code is right

                            //recupere l'URL de la base de données pour pouvoir y accéder
                            FirebaseDatabase database = FirebaseDatabase.getInstance("https://auth-tel-default-rtdb.europe-west1.firebasedatabase.app/");
                            // Database reference permet d'acceder à une portion précsie de la base de données si on le souhait. ici la racine.
                            DatabaseReference reference = database.getReference();
                            DatabaseReference UserUID = reference.child("Users").child(mAuth.getUid());
                            ValueEventListener eventListener = new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(!dataSnapshot.exists()){
                                        //create a new user
                                        Intent i = new Intent(MainActivity.this, UsernameActivity.class);
                                        i.putExtra("phone", phone);
                                        i.putExtra("UserId", mAuth.getUid());
                                        startActivity(i);
                                        finish();
                                    }
                                    else{
                                        //User already exist
                                        Intent connectUser = new Intent(MainActivity.this, HomeActivity.class);
                                        connectUser.putExtra("UID", mAuth.getUid());
                                        startActivity(connectUser);
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            };
                            UserUID.addListenerForSingleValueEvent(eventListener);





                        } else { //case were the code is wrong
                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void sendVerificationCode(String number) {
        //methode pour envoyer le code sur le telephone de l'utilisateur
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(number)            // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // Activity (for callback binding)
                        .setCallbacks(mCallBack)           // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    //objet representant le mecanisme d'authetification par telephone
    private final PhoneAuthProvider.OnVerificationStateChangedCallbacks

            // initializing our callbacks for on
            // verification callback method.
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        // below method is used when
        // OTP is sent from Firebase
        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            // when we receive the OTP it
            // contains a unique id which
            // we are storing in our string
            // which we have already created.
            verificationId = s;
        }

        // this method is called when user
        // receive OTP from Firebase.
        //cet methode est appelée quand l'utilisateur recois le code depuis firebase
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            // below line is used for getting OTP code
            // which is sent in phone auth credentials.
            final String code = phoneAuthCredential.getSmsCode();

            // checking if the code
            // is null or not.
            if (code != null) {
                // if the code is not null then
                // we are setting that code to
                // our OTP edittext field.
                edtOTP.setText(code);

                // after setting this code
                // to OTP edittext field we
                // are calling our verifycode method.
                verifyCode(code);
            }
        }

        // this method is called when firebase doesn't
        // sends our OTP code due to any error or issue.
        @Override
        public void onVerificationFailed(FirebaseException e) {
            // displaying error message with firebase exception.
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    // below method is use to verify code from Firebase.
    private void verifyCode(String code) {
        // below line is used for getting
        // credentials from our verification id and code.
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        // after getting credential we are
        // calling sign in method.
        signInWithCredential(credential);
    }
}

