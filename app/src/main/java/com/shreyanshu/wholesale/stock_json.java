package com.shreyanshu.wholesale;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.List;

@IgnoreExtraProperties
final class stock_json {
    public String name;
    public int stock;
    public String item_spec;
    public int start_stock;
    public List<String> transactions;

    stock_json(String name, int stock, String item_spec, int start_stock, List<String> transactions) {
        this.name = name;
        this.stock = stock;
        this.item_spec = item_spec;
        this.start_stock = start_stock;
        this.transactions = transactions;
    }

    stock_json(){

    }
}


