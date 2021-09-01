package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Log.d("Loggert_Froggert","Login Activity Start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();

//        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            Log.d("Loggert_Froggert","Auto Login " + mAuth.getCurrentUser().getEmail());

            Toast.makeText(LoginActivity.this, "Sign in Successful",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }
    }


    public void createAccount(View view)
    {
        EditText emailView = findViewById(R.id.EmailField);
        EditText passView = findViewById(R.id.PasswordField);

        String email = emailView.getText().toString();
        String password = passView.getText().toString();

        if (email.isEmpty() || password.isEmpty())
        {
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Loggert_Froggert", "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(LoginActivity.this, "Sign Up Successful",
                                    Toast.LENGTH_SHORT).show();

                            //setContentView(R.layout.activity_test);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("Loggert_Froggert", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    public void enterAccount(View view)
    {

        EditText emailView = findViewById(R.id.EmailField);
        EditText passView = findViewById(R.id.PasswordField);

        String email = emailView.getText().toString();
        String password = passView.getText().toString();

        if (email.isEmpty() || password.isEmpty())
        {
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Loggert_Froggert", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(LoginActivity.this, "Sign in Successful",
                                    Toast.LENGTH_SHORT).show();

                            //setContentView(R.layout.activity_test);

                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);


                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();

                        }

                        // ...
                    }
                });
    }


}
