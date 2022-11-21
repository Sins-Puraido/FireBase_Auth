package angers.dudouet.firebase_auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeActivity extends AppCompatActivity {

    private TextView GreetingsCom;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        String userID = intent.getStringExtra("UID");
        String Connection_Method = intent.getStringExtra("ConnectMethod");
        if (Connection_Method.equals("phone")){
            //recupere l'URL de la base de données pour pouvoir y accéder
            FirebaseDatabase database = FirebaseDatabase.getInstance("https://auth-tel-default-rtdb.europe-west1.firebasedatabase.app/");
            // Database reference permet d'acceder à une portion précsie de la base de données si on le souhait. ici la racine.
            DatabaseReference reference = database.getReference().child("Users").child(userID);
            reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    DataSnapshot snapshot = task.getResult();
                    GreetingsCom = findViewById(R.id.GreetingsComment);
                    GreetingsCom.setText("Felicitations " + snapshot.child("username").getValue(String.class) +" vous vous etes connecté ! \n Welcome to Home Screen");
                }
            });
        }
        else if (Connection_Method.equals("yahoo")){
            GreetingsCom = findViewById(R.id.GreetingsComment);
            GreetingsCom.setText("Felicitations, vous vous etes connecté ! \n Welcome to Home Screen");

        }




    }
}