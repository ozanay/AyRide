package com.iride.ayride;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class ChatHashTagsActivity extends AppCompatActivity implements HashTagDialogFragment.HashTagDialogListener{

    private final static String loggerTag = ChatHashTagsActivity.class.getSimpleName();
    private final static String hashTagDialogFragmentTag = HashTagDialogFragment.class.getSimpleName();
    private String chatUrl;
    private ArrayList<String> hashTags;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_hash_tags);
        this.hashTags = (ArrayList<String>) getIntent().getSerializableExtra("hashTagList");
        Button addHashTagButton = (Button) findViewById(R.id.add_hashtag_button);
        if (addHashTagButton != null) {
            addHashTagButton.setOnClickListener(addHashTagListener);
        }

        ListView hashTagsList = (ListView) findViewById(R.id.list_of_chat_hashtags);
        String[] tags = formatHashTags(hashTags);
        if (tags == null){
            showHashTagDialog();
            tags = new String[]{""};
        }

        ArrayAdapter hashTagsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tags);
        if (hashTagsList != null) {
            hashTagsList.setAdapter(hashTagsAdapter);
            hashTagsList.setOnItemClickListener(hashTagsClickListener);
        }
    }

    @Override
    public void onDialogPositiveClick(HashTagDialogFragment hashTagDialogFragment) {
        String tag = hashTagDialogFragment.getHashTag();
        hashTags.add(tag);
        Intent intent = new Intent(ChatHashTagsActivity.this, ChatActivity.class);
        intent.putExtra("chatUrl",ChatHashTagsActivity.this.chatUrl+tag+"/");
        startActivity(intent);
        finish();
    }

    @Override
    public void onDialogNegativeClick(HashTagDialogFragment hashTagDialogFragment) {
        hashTagDialogFragment.dismiss();
    }

    @Override
    protected void onStop(){
        super.onStop();
        hashTags.clear();
    }

    @Override
    public void onBackPressed(){
        startActivity(new Intent(ChatHashTagsActivity.this, HomePageActivity.class));
        finish();
    }

    private String[] formatHashTags(ArrayList<String> tags){
        this.chatUrl = tags.get(tags.size()-1);
        Log.d(loggerTag, "URL: "+chatUrl);
        tags.remove(tags.size()-1);
        if (tags.size() == 0){
            return null;
        }

        String[] formattedTags = new String[tags.size()];
        for (int i =0; i<tags.size(); i++) {
            formattedTags[i] = tags.get(i);
        }

        return formattedTags;
    }

    private void showHashTagDialog() {
        new HashTagDialogFragment().show(getFragmentManager(), hashTagDialogFragmentTag);
    }

    private AdapterView.OnItemClickListener hashTagsClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
            String tag = (String)adapter.getItemAtPosition(position);
            tag = tag.substring(1);
            Intent intent = new Intent(ChatHashTagsActivity.this, ChatActivity.class);
            intent.putExtra("chatUrl",ChatHashTagsActivity.this.chatUrl+tag);
            startActivity(intent);
            finish();
        }
    };

    private View.OnClickListener addHashTagListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showHashTagDialog();
        }
    };
}
