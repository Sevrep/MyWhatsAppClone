package com.sevrep.mywhatsappclone;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WhatsAppChatActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    private ListView chatListView;
    private ArrayList<String> chatList;
    private ArrayAdapter adapter;
    private String selectedUser;
    private EditText edtSend;
    private ImageButton btnSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_app_chat);

        selectedUser = getIntent().getStringExtra("selectedUser");
        FancyToast.makeText(this, "Chat with " + selectedUser, Toast.LENGTH_LONG, FancyToast.INFO, true).show();

        ConstraintLayout constraintLayoutChat = findViewById(R.id.constraintLayoutChat);
        constraintLayoutChat.setOnClickListener(this);

        edtSend = findViewById(R.id.edtSend);
        edtSend.setOnKeyListener(this);

        chatList = new ArrayList<>();
        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, chatList);

        chatListView = findViewById(R.id.chatListView);
        chatListView.setAdapter(adapter);

        btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        loadList();

    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();
        if (viewId == R.id.btnSend) {
            sendMessage();
            hideKeypad(v);
        } else if (viewId == R.id.constraintLayoutChat) {
            hideKeypad(v);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v == edtSend) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                onClick(btnSend);
            }
            return false;
        }
        return true;
    }

    private void hideKeypad(View v) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadList() {
        try {
            ParseQuery<ParseObject> firstUserChatQuery = ParseQuery.getQuery("Chat");
            firstUserChatQuery.whereEqualTo("waSender", ParseUser.getCurrentUser().getUsername());
            firstUserChatQuery.whereEqualTo("waRecipient", selectedUser);

            ParseQuery<ParseObject> secondUserChatQuery = ParseQuery.getQuery("Chat");
            secondUserChatQuery.whereEqualTo("waSender", selectedUser);
            secondUserChatQuery.whereEqualTo("waRecipient", ParseUser.getCurrentUser().getUsername());

            ArrayList<ParseQuery<ParseObject>> allQueries = new ArrayList<>();
            allQueries.add(firstUserChatQuery);
            allQueries.add(secondUserChatQuery);

            ParseQuery<ParseObject> objectParseQuery = ParseQuery.or(allQueries);
            objectParseQuery.orderByAscending("createdAt");
            objectParseQuery.findInBackground((objects, e) -> {
                if (objects.size() > 0 && e == null) {
                    for (ParseObject chatObject : objects) {
                        String waMessage = chatObject.get("waMessage") + "";
                        if (Objects.requireNonNull(chatObject.get("waSender")).equals(ParseUser.getCurrentUser().getUsername())) {
                            waMessage = ParseUser.getCurrentUser().getUsername() + ": " + waMessage;
                        }
                        if (Objects.requireNonNull(chatObject.get("waSender")).equals(selectedUser)) {
                            waMessage = selectedUser + ": " + waMessage;
                        }
                        chatList.add(waMessage);
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        boolean isValid = true;
        String message = edtSend.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            edtSend.setError("Write something...");
            isValid = false;
        }

        if (isValid) {
            ParseObject chat = new ParseObject("Chat");
            chat.put("waSender", ParseUser.getCurrentUser().getUsername());
            chat.put("waRecipient", selectedUser);
            chat.put("waMessage", message);
            chat.saveInBackground(e -> {
                if (e == null) {
                    FancyToast.makeText(this, "Message sent.", Toast.LENGTH_SHORT, FancyToast.SUCCESS, true).show();
                    chatList.add(ParseUser.getCurrentUser().getUsername() + ": " + message);
                    adapter.notifyDataSetChanged();
                    edtSend.setText("");
                }
            });
        } else {
            FancyToast.makeText(this, "No message to be sent...", Toast.LENGTH_SHORT, FancyToast.ERROR, true).show();
        }
    }
}