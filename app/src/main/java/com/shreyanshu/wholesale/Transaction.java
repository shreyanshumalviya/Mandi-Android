package com.shreyanshu.wholesale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Transaction extends AppCompatActivity {
    EditText t_date, t_time, t_amount, t_memo;
    AutoCompleteTextView t_itemName, t_customerName;
    TextView l_currentStock;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> itemList = new ArrayList<>(), itemIdList = new ArrayList<>(), userList = new ArrayList<>(), userIdList = new ArrayList<>();
    ArrayAdapter<String> itemAdapter, userAdapter;
    String itemId, customerId;
    String time;
    Date date;
    ToggleButton b_inOrOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
      //  check_authentication();
        b_inOrOut = findViewById(R.id.at_inOutSwitch);
        t_itemName = findViewById(R.id.at_itemName);
        t_customerName = findViewById(R.id.at_customerName);
        t_date = findViewById(R.id.at_date);
        t_time = findViewById(R.id.at_time);
        t_amount = findViewById(R.id.at_amount);
        t_memo = findViewById(R.id.at_memo);
        l_currentStock = findViewById(R.id.at_currentStock);
        fill_adapter();
        get_date();
        itemAdapter = new ArrayAdapter<>(this, R.layout.list_item, itemList);
        t_itemName.setThreshold(0);
        t_itemName.setAdapter(itemAdapter);
        userAdapter = new ArrayAdapter<>(this, R.layout.list_item, userList);
        t_customerName.setThreshold(0);
        t_customerName.setAdapter(userAdapter);
        t_itemName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = itemList.indexOf(t_itemName.getText().toString());
                t_itemName.setText(t_itemName.getText().toString().split("\n-")[0]);
                itemId = itemIdList.get(position);
                db.collection("stock").document(itemId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        l_currentStock.setText("Current Stock - " + document.get("stock"));
                    }
                });
            }
        });
        t_customerName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position = userList.indexOf(t_customerName.getText().toString());
                t_customerName.setText(userList.get(position).split("\n-")[0]);
                customerId = userIdList.get(position);
            }
        });
    }

    private void check_authentication() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || !currentUser.getUid().equals("wr2wKs14W2hjmsrG6pl0OQtaJdu2")) {
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    private void fill_adapter() {
        final boolean[] done = {false};
        db.collection("stock").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getId());
                        itemList.add(document.getString("name") + "\n-         " + document.getString("item_spec"));
                        itemIdList.add(document.getId());
                        if (done[0]) {
                            t_itemName.setEnabled(true);
                            t_customerName.setEnabled(true);
                            t_date.setEnabled(true);
                            t_time.setEnabled(true);
                            t_amount.setEnabled(true);
                            t_memo.setEnabled(true);
                        } else done[0] = true;
                    }
                }
            }
        });
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isComplete()) {
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getId());
                        userList.add(document.getString("user_name") + "\n-         " + document.getString("shop_name"));
                        userIdList.add(document.getId());
                        if (done[0]) {
                            t_itemName.setEnabled(true);
                            t_customerName.setEnabled(true);
                            t_date.setEnabled(true);
                            t_time.setEnabled(true);
                            t_amount.setEnabled(true);
                            t_memo.setEnabled(true);
                        } else done[0] = true;
                    }
                }
            }
        });
    }

    private void get_date() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        date = c.getTime();
        t_date.setText(new SimpleDateFormat("dd/MM/yyyy").format(date));
        if (timeOfDay < 12) {
            time = "Morning";
        } else if (timeOfDay < 16) {
            time = "Noon";
        } else if (timeOfDay < 20) {
            time = "Evening";
        } else {
            time = "Night";
        }
        t_time.setText(time);
    }

    public void addTransaction(View view) {
        final String documentId = db.collection("transactions").document().getId();
        int amount = Integer.parseInt(t_amount.getText().toString());
        if (b_inOrOut.isChecked()) {
            amount = -amount;
        }
        final transaction_json transaction = new transaction_json(
                itemId, t_itemName.getText().toString(), customerId, t_customerName.getText().toString(),
                date, time, t_memo.getText().toString(), amount);
        db.collection("transactions").document(documentId).set(transaction)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(Transaction.this, "Saved Successfully", Toast.LENGTH_SHORT).show();
                        get_date();
                        if (customerId != null)
                            db.collection("users").document(customerId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    users_json user = task.getResult().toObject(users_json.class);
                                    if (user.transactions == null) {
                                        user.transactions = new ArrayList<>();
                                        user.transactions.add(documentId);
                                    } else
                                        user.transactions.add(documentId);
                                    db.collection("users").document(customerId).set(user);
                                }
                            });
                        db.collection("stock").document(itemId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                stock_json stock = task.getResult().toObject(stock_json.class);
                                if (stock.transactions == null) {
                                    stock.transactions = new ArrayList<>();
                                    stock.transactions.add(documentId);
                                } else
                                    stock.transactions.add(documentId);
                                stock.stock = stock.stock + transaction.amount;
                                db.collection("stock").document(itemId).set(stock);
                            }
                        });
                    }
                });
    }
}

@IgnoreExtraProperties
final class transaction_json {
    public String itemName, customerName, itemId, customerId;
    public Date date;
    public String time, memo;
    public int amount;

    transaction_json(String itemId, String itemName, String customerId, String customerName, Date date, String time, String memo, int amount) {
        this.amount = amount;
        this.customerId = customerId;
        this.customerName = customerName;
        this.date = date;
        this.itemId = itemId;
        this.itemName = itemName;
        this.time = time;
        this.memo = memo;
    }

    transaction_json() {

    }
}

