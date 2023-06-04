package com.example.securetaville;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.securetaville.databinding.ActivityAddressListBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AddressListActivity extends AppCompatActivity {

    private List<String> adresseList;
    private ListView addressListView;
    private ArrayAdapter addressAdapter;

    ActivityAddressListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddressListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // Configurer l'adaptateur pour afficher les adresses dans le ListView
        addressListView = findViewById(R.id.addressListView);



        SharedPreferences sharedPreferences = getSharedPreferences("location", MODE_PRIVATE);
        Set<String> addressesSet = sharedPreferences.getStringSet("addresses", new HashSet<>());
        ArrayList<String> adresseList = new ArrayList<>(addressesSet);

        addressAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, adresseList);
        addressListView.setAdapter(addressAdapter);

        binding.bottomNavigationView.setSelectedItemId(R.id.menu_addresses);
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_map) {
                // Ouvrir AddressListActivity
                Intent addressIntent = new Intent(AddressListActivity.this, MainActivity.class );
                startActivity(addressIntent);
                return true;
            }
            // Gérer d'autres éléments du menu si nécessaire
            return false;
        });
    }
}