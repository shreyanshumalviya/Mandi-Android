package com.shreyanshu.wholesale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import static android.widget.Toast.LENGTH_SHORT;
import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity {
    boolean locked = true;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        check_authentication();

    }

    public void logout(View view) {
        mAuth.signOut();
        startActivity(new Intent(this, MainActivity.class));
    }

    private void check_authentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            login();
        }
    }

    private void login() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.login_dialog, null);
        final EditText t_password = mView.findViewById(R.id.ld_password), t_email = mView.findViewById(R.id.ld_email);
        final Context context = this;
        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (locked) login();
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        mView.findViewById(R.id.ld_newUser).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Signup.class));
            }
        });
        mView.findViewById(R.id.ld_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signInWithEmailAndPassword(t_email.getText().toString(), t_password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d("TAG", "signInWithEmail:success");
                                    //FirebaseUser user = mAuth.getCurrentUser();
                                    locked = false;
                                    alertDialog.dismiss();
                                } else {
                                    Log.w("TAG", "signInWithEmail:failure", task.getException());
                                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            }
        });
        alertDialog.show();

    }

    public void add_stock(View view) {
        startActivity(new Intent(this, AddStock.class));
    }

    public void add_transaction(View view) {
        startActivity(new Intent(this, Transaction.class));
    }

    public void view_transaction(View view) {
        startActivity(new Intent(this, ViewTransaction.class));
    }
}
