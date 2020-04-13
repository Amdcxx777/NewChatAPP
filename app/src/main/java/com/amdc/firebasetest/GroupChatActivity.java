package com.amdc.firebasetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;

public class GroupChatActivity extends AppCompatActivity {
    private ImageButton SendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages, displayNameMessages, displayTimeMessages;
    private DatabaseReference UsersRef;
    private DatabaseReference GroupNameRef;
    private String currentGroupName;
    private String currentUserID;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = Objects.requireNonNull(Objects.requireNonNull(getIntent().getExtras()).get("groupName")).toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null)  currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        GroupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();
        GetUserInfo();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        SendMessageButton.setOnClickListener(view -> {
            SaveMessageInfoToDatabase();
            userMessageInput.setText("");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        });
    }

    private void InitializeFields() {
        Objects.requireNonNull(getSupportActionBar()).setTitle(currentGroupName);
        SendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        mScrollView = findViewById(R.id.my_scroll_view);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        displayNameMessages = findViewById(R.id.group_chat_name_display);
        displayTimeMessages = findViewById(R.id.group_chat_time_display);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GroupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) DisplayMessages(dataSnapshot);
            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if(dataSnapshot.exists()) DisplayMessages(dataSnapshot);
            }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @SuppressLint("SetTextI18n")
    private void DisplayMessages(DataSnapshot dataSnapshot) {
        Iterator iterator = dataSnapshot.getChildren().iterator();
        while (iterator.hasNext()) {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
            displayNameMessages.setText(chatName);
            displayTextMessages.setText(chatMessage);
            displayTimeMessages.setText(chatTime + " - " + chatDate);
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

    private void GetUserInfo() {
        UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) currentUserName = Objects.requireNonNull(dataSnapshot.child("name").getValue()).toString();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void SaveMessageInfoToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKEY = GroupNameRef.push().getKey();
        if (TextUtils.isEmpty(message)) Toast.makeText(this, "Please write message first...", Toast.LENGTH_SHORT).show();
        else {
            HashMap<String, Object> groupMessageKey = new HashMap<>();
            GroupNameRef.updateChildren(groupMessageKey);
            DatabaseReference groupMessageKeyRef = GroupNameRef.child(Objects.requireNonNull(messageKEY));
            HashMap<String, Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name", currentUserName);
            messageInfoMap.put("message", message);
            messageInfoMap.put("date", new SimpleDateFormat("dd.MM.yyyy").format(Calendar.getInstance().getTime()));
            messageInfoMap.put("time", new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime()));
            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }
}
