package com.shreyanshu.wholesale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;

public class ViewTransaction extends AppCompatActivity {
    AutoCompleteTextView t_itemName;
    ArrayList<String> itemList = new ArrayList<>(), itemIdList = new ArrayList<>();
    String itemId;
    FirebaseFirestore db;
    TextView l_currentStock;
    ArrayAdapter<String> itemAdapter;
    LinearLayout ll_columnNames;
    int left, startStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);
        ll_columnNames = findViewById(R.id.vt_columnName);
        db = FirebaseFirestore.getInstance();
        t_itemName = findViewById(R.id.vt_itemName);
        l_currentStock = findViewById(R.id.vt_currentStock);
        fill_adapter();
        itemAdapter = new ArrayAdapter<>(this, R.layout.list_item, itemList);
        t_itemName.setThreshold(0);
        t_itemName.setAdapter(itemAdapter);
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
                        left = Integer.parseInt(document.get("stock").toString());
                        startStock = Integer.parseInt(document.get("start_stock").toString());
                        l_currentStock.setText("Current Stock - " + left);
                    }
                });
            }
        });
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
                        } else done[0] = true;
                    }
                }
            }
        });
    }

    public void view_history(View view) {
        final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        if (itemId == null) {
            Toast.makeText(this, "Select Item", Toast.LENGTH_SHORT).show();
            return;
        }
        final LinearLayout ll_container = findViewById(R.id.vt_container);
        ll_container.removeAllViews();
        ll_container.addView(ll_columnNames);
        db.collection("transactions").whereEqualTo("itemId", itemId).orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                try {
                    Log.d("TAG", "onEvent: " + queryDocumentSnapshots.size() + "\n\njghf " + left);
                    for (QueryDocumentSnapshot snapshot : queryDocumentSnapshots) {
                        int amount = Integer.parseInt(snapshot.get("amount").toString());
                        ll_container.addView(new TransactionView(ViewTransaction.this, format.format(((Timestamp) snapshot.get("date")).toDate()), left, amount, snapshot.get("memo").toString()));
                        left = left - amount;
                    }
                    ll_container.addView(new TransactionView(ViewTransaction.this, "Start", startStock,startStock,""));
                } catch (NullPointerException n) {
                    Log.e("ERROR", "onEvent: null pointer exception");
                }
            }
        });
    }

}

class TransactionView extends LinearLayout {
    TextView t_date, t_type, t_left, t_amount, t_memo;

    public TransactionView(Context context, String date, int left, int amount, String memo) {
        super(context);
        String type;
        if (amount < 0) type = "out";
        else type = "in";
        LinearLayout.LayoutParams labelLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        LinearLayout.LayoutParams labelLayoutParams2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2);

        t_date = new TextView(context);
        t_date.setText(date);
        t_date.setPadding(15, 25, 15, 25);
        t_date.setLayoutParams(labelLayoutParams2);
        addView(t_date);

        t_type = new TextView(context);
        t_type.setText(type);
        t_type.setPadding(15, 25, 15, 25);
        t_type.setLayoutParams(labelLayoutParams);
        addView(t_type);

        t_amount = new TextView(context);
        t_amount.setText(amount + "");
        t_amount.setPadding(15, 25, 15, 25);
        t_amount.setLayoutParams(labelLayoutParams);
        addView(t_amount);

        t_left = new TextView(context);
        t_left.setText(left + "");
        t_left.setPadding(15, 25, 15, 25);
        t_left.setLayoutParams(labelLayoutParams);
        addView(t_left);

        t_memo = new TextView(context);
        t_memo.setText(memo);
        t_memo.setPadding(15, 25, 15, 25);
        t_memo.setLayoutParams(labelLayoutParams2);
        addView(t_memo);
    }
}