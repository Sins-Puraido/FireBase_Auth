package angers.dudouet.firebase_auth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UsernameActivity extends AppCompatActivity {


    private EditText writeUsername;

    private Button generateUsername;
    //recupere l'URL de la base de données pour pouvoir y accéder
    FirebaseDatabase database = FirebaseDatabase.getInstance("Database URL");
    // Database reference permet d'acceder à une portion précsie de la base de données si on le souhait. ici la racine.
    DatabaseReference reference = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        writeUsername = findViewById(R.id.editGetUsername);
        generateUsername = findViewById(R.id.button_verification);
        Intent intent = getIntent();
        String phoneNumber = intent.getStringExtra("phone");
        String userUID = intent.getStringExtra("UserId");
        generateUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(writeUsername.getText().toString())) {
                    // si la saisie est vide afficher un message d'erreur
                    Toast.makeText(UsernameActivity.this, "Entrer un nom d'utilisateur", Toast.LENGTH_SHORT).show();
                } else {
                    // enregistre la saisie en tant que nouveau nom d'utilisateur
                    //reference.child("users")
                    reference.child("Users").child(userUID).child("username").setValue(writeUsername.getText().toString());
                    reference.child("Users").child(userUID).child("phone").setValue(phoneNumber);
                    // envoie l'utilisateur sur la page d'accueil
                    Intent connectUser = new Intent(UsernameActivity.this, HomeActivity.class);
                    connectUser.putExtra("UID", userUID);
                    connectUser.putExtra("ConnectMethod", "phone");
                    startActivity(connectUser);
                    finish();
                }
            }
        });
    }
}

