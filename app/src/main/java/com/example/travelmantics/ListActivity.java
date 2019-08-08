package com.example.travelmantics;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class ListActivity extends AppCompatActivity {




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        UserAuth.init(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.list_activity_menu, menu);
        MenuItem newDealOption = menu.findItem(R.id.manage_deals);

        newDealOption.setVisible(UserAuth.isAdmin);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem newDealOption = menu.findItem(R.id.manage_deals);

        newDealOption.setVisible(UserAuth.isAdmin);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.manage_deals:
                Intent manageDealsIntent = new Intent(ListActivity.this,
                        DealManagerActivity.class);
                startActivity(manageDealsIntent);
                break;
            case R.id.log_out:
                UserAuth.firebaseAuth.signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        UserAuth.detachAuthListener();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        UserAuth.attachAuthListener();

        RecyclerView rvDeals = findViewById(R.id.rvDeals);

        final DealAdapter dealAdapter = new DealAdapter(this);

        rvDeals.setAdapter(dealAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL, false);
        rvDeals.setLayoutManager(layoutManager);
    }
}
