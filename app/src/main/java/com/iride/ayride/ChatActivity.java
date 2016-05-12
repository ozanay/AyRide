package com.iride.ayride;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;
import com.firebase.ui.FirebaseListAdapter;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private final static String loggerTag = ChatActivity.class.getSimpleName();
    private static String userName;
    private Firebase firebase;
    private FirebaseListAdapter<Chat> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        try {
            Firebase.setAndroidContext(this);
            String chatUrl = getIntent().getStringExtra("chatUrl");
            Log.d(loggerTag, "CHAT URL: " + chatUrl);
            ArrayList<String> hashTags = new ArrayList<>();
            UserLocalStorage userLocalStorage = new UserLocalStorage(getSharedPreferences(StoragePreferences.USER_PREFERENCES, Context.MODE_PRIVATE));
            userName = userLocalStorage.getUserName() + " " + userLocalStorage.getUserSurName();
            this.firebase = new Firebase(chatUrl);
            final EditText textEdit = (EditText) this.findViewById(R.id.text_edit);
            Button sendButton = (Button) this.findViewById(R.id.send_button);
            if (sendButton != null) {
                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String text;
                        if (textEdit != null) {
                            text = textEdit.getText().toString();
                            Chat message = new Chat(userName, text);
                            firebase.push().setValue(message);
                            textEdit.setText("");
                        }
                    }
                });
            }

            final ListView listView = (ListView) this.findViewById(android.R.id.list);
            mListAdapter = new FirebaseListAdapter<Chat>(this, Chat.class,
                    android.R.layout.two_line_list_item, firebase) {
                @Override
                protected void populateView(View v, Chat model, int position) {
                    TextView author = (TextView) v.findViewById(android.R.id.text2);
                    author.setTextColor(Color.RED);
                    author.setText(model.getAuthor());
                    TextView message = (TextView) v.findViewById(android.R.id.text1);
                    message.setTextColor(Color.BLUE);
                    message.setText(model.getMessage());
                }
            };
            if (listView != null) {
                listView.setAdapter(mListAdapter);
            }
        } catch (Exception exc){
            Log.d(loggerTag, exc.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mListAdapter.cleanup();
    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(ChatActivity.this,HomePageActivity.class));
        finish();
    }
}
