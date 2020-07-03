package com.shreyanshu.wholesale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.FocusFinder;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;

public class Signup extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText t_email, t_password, t_confirmPassword, t_shop_name, t_userName, t_shopAddress;
    FirebaseFirestore db;
    String COLLECTION_PATH = "users";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        t_confirmPassword = findViewById(R.id.su_cnfPassword);
        t_password = findViewById(R.id.su_password);
        t_email = findViewById(R.id.su_email);
        t_shop_name = findViewById(R.id.su_shopName);
        t_userName = findViewById(R.id.su_userName);
        t_shopAddress = findViewById(R.id.su_shopAddress);
    }

    public void signup(View view) {
        final String email, password, user_name, shop_name, shop_address;
        email = t_email.getText().toString();
        password = t_password.getText().toString();
        user_name = t_userName.getText().toString();
        shop_name = t_shop_name.getText().toString();
        shop_address = t_shopAddress.getText().toString();
        if (email.equals("")) {
            Toast.makeText(this, "incorrect Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.equals("")) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (!password.equals(t_confirmPassword.getText().toString())) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            t_password.setText("");
            t_confirmPassword.setText("");
            return;
        }
        if (user_name.equals("")) {
            Toast.makeText(this, "User Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (shop_name.equals("")) {
            Toast.makeText(this, "Shop Name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (shop_address.equals("")) {
            Toast.makeText(this, "Shop Address cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    db.collection(COLLECTION_PATH).document(mAuth.getCurrentUser().getUid())
                            .set(new users_json(shop_name,user_name,shop_address,email,password,null));
                    Toast.makeText(Signup.this,"Registered Successfully.",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(Signup.this,"Error - "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

@IgnoreExtraProperties
class users_json {
    public String shop_name;
    public String user_name;
    public String shop_address;
    public String email;
    public String password;
    public List<String> transactions;

    users_json(String shop_name, String user_name, String shop_address, String email, String password, List<String> transactions) {
        this.shop_name = shop_name;
        this.user_name = user_name;
        this.shop_address = shop_address;
        this.email = email;
        this.password = password;
        this.transactions = transactions;
    }

    users_json() {

    }
}