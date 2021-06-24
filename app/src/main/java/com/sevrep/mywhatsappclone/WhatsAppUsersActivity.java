package com.sevrep.mywhatsappclone;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;

public class WhatsAppUsersActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> waUsers;
    private ArrayAdapter<String> adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_app_users);

        FancyToast.makeText(this, "Welcome " + ParseUser.getCurrentUser().getUsername(), Toast.LENGTH_LONG, FancyToast.INFO, true).show();

        waUsers = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, waUsers);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshList);

        listView = findViewById(R.id.listView);

        if (adapter.isEmpty()) {
            loadList();
        } else {
            FancyToast.makeText(this, "No registered users...", Toast.LENGTH_SHORT, FancyToast.INFO, true).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.logout_item) {
            logout();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        logout();
    }

    private void loadList() {
        try {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.orderByDescending("createdAt");
            query.findInBackground((users, e) -> {
                if (users.size() > 0 && e == null) {
                    for (ParseUser user : users) {
                        waUsers.add(user.getUsername());
                    }
                    listView.setAdapter(adapter);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshList() {
        try {
            ParseQuery<ParseUser> query = ParseUser.getQuery();
            query.whereNotEqualTo("username", ParseUser.getCurrentUser().getUsername());
            query.whereNotContainedIn("username", waUsers);
            query.orderByDescending("createdAt");
            query.findInBackground((users, e) -> {
                if (users.size() > 0 && e == null) {
                    for (ParseUser user : users) {
                        waUsers.add(user.getUsername());
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logging out?")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (arg0, arg1) -> {

                    ParseUser.getCurrentUser();
                    ParseUser.logOutInBackground(e -> {
                        Intent intent = new Intent(this, SignUpActivity.class);
                        startActivity(intent);
                        finish();
                    });

                })
                .create()
                .show();
    }

}
