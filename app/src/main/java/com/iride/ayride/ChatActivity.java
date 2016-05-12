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

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    private final static String loggerTag = ChatActivity.class.getSimpleName();
    private static String userName;
    private String chatUrl;
    private UserLocalStorage userLocalStorage;
    private Firebase firebase;
    private ValueEventListener mConnectedListener;
    private ArrayList<String> hashTags;
    private FirebaseListAdapter<Chat> mListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Firebase.setAndroidContext(this);
        chatUrl = getIntent().getStringExtra("chatUrl");
        Log.d(loggerTag, "URL: "+chatUrl);
        hashTags = new ArrayList<>();
        this.userLocalStorage = new UserLocalStorage(getSharedPreferences(StoragePreferences.USER_PREFERENCES, Context.MODE_PRIVATE));
        this.userName = userLocalStorage.getUserName()+" "+userLocalStorage.getUserSurName();
        this.firebase = new Firebase(chatUrl);
        final EditText textEdit = (EditText) this.findViewById(R.id.text_edit);
        Button sendButton = (Button) this.findViewById(R.id.send_button);
        if (sendButton != null) {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = textEdit.getText().toString();
                    Chat message = new Chat(userName, text);
                    firebase.push().setValue(message);
                    textEdit.setText("");
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
                TextView message =(TextView) v.findViewById(android.R.id.text1);
                message.setTextColor(Color.BLUE);
                message.setText(model.getMessage());
            }
        };
        listView.setAdapter(mListAdapter);
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

    private ValueEventListener chatValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.hasChildren()) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    hashTags.add("#" + child.getKey());
                }
            }

            if (userLocalStorage.isDriverMode()) {
                hashTags.add(getString(R.string.firebaseDriversChat));
            } else {
                hashTags.add(getString(R.string.firebaseUsersChat));
            }

            Intent intent = new Intent(ChatActivity.this, ChatHashTagsActivity.class);
            intent.putExtra("hashTagList", hashTags);
            startActivity(intent);
        }

        @Override
        public void onCancelled(FirebaseError firebaseError) {
            Log.e(loggerTag, firebaseError.getMessage());
        }
    };
}
