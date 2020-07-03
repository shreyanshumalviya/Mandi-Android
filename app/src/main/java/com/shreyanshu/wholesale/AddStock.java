package com.shreyanshu.wholesale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddStock extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    final String COLLECTION_PATH = "stock";
    EditText t_item_spec, t_start_stock;
    AutoCompleteTextView t_name;
    List<String> nameList;
    ArrayAdapter<String> nameAdapter;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_stock);
        mAuth = FirebaseAuth.getInstance();
        //check_authentication();
        t_name = findViewById(R.id.as_name);
        t_item_spec = findViewById(R.id.as_item_spec);
        t_start_stock = findViewById(R.id.as_start_stock);
        t_start_stock.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try{
                if (Integer.parseInt(s.toString())<0){
                    t_start_stock.setText(0+"");
                    Toast.makeText(AddStock.this,"Stock cannot be less than 0",Toast.LENGTH_SHORT).show();
                }}
                catch (NumberFormatException n){
                    Log.e("TAG", "afterTextChanged: "+n.getMessage() );
                }
            }
        });
        nameList = new ArrayList<>();
        fill_adapter();
        nameAdapter = new ArrayAdapter<String>(this, R.layout.list_item, nameList);
        t_name.setThreshold(1);
        t_name.setAdapter(nameAdapter);
    }

    private void check_authentication() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals("wr2wKs14W2hjmsrG6pl0OQtaJdu2")) {
            startActivity(new Intent(this,MainActivity.class));
        }
    }

    private void fill_adapter() {
        db.collection(COLLECTION_PATH).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getId());
                        nameList.add(document.getString("name"));
                    }
                    t_name.setEnabled(true);
                    t_item_spec.setEnabled(true);
                    t_start_stock.setEnabled(true);
                }
            }
        });
    }

    public void add(View view) {
        final String name = t_name.getText().toString();
        final Context context = this;
        if (nameList.contains(name)) {
            Toast.makeText(this,"Item with " +name +" name already exist",Toast.LENGTH_SHORT).show();
        } else {
            stock_json stock1 = new stock_json(t_name.getText().toString(), Integer.parseInt(t_start_stock.getText().toString()), t_item_spec.getText().toString(), Integer.parseInt(t_start_stock.getText().toString()), null);
            db.collection(COLLECTION_PATH).document().set(stock1)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context,"Saved Successfully",Toast.LENGTH_SHORT).show();
                            nameList.add(name);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,"Unable to saved, Try restarting application or contact support",Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
    //read
    /*db.collection(COLLECTION_PATH).document("" + item_id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                stock = (stock_json) documentSnapshot.toObject(stock_json.class);
                t_name.setText(stock.name);
                t_item_spec.setText(stock.item_spec);
                t_start_stock.setText(stock.start_stock + "");

            }
        });*/
}
