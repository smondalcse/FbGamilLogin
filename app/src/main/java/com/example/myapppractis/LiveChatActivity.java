package com.example.myapppractis;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class LiveChatActivity extends AppCompatActivity {
    private static final String TAG = "LiveChatActivity";
    private static final int REQUEST_CODE_SPEECH_INPUT = 1000;

    private RecyclerView chatsRV;
    private ImageButton sendMsgIB, idIVRecord;
    private EditText userMsgEdt;
    private final String USER_KEY = "user";
    private final String BOT_KEY = "bot";
    private RequestQueue mRequestQueue;
    private ArrayList<MessageModal> messageModalArrayList;
    private MessageRVAdapter messageRVAdapter;
    private String msgType = "text";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_chat);

        chatsRV = findViewById(R.id.idRVChats);
        sendMsgIB = findViewById(R.id.idIBSend);
        userMsgEdt = findViewById(R.id.idEdtMessage);

        userMsgEdt.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) { }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (count > 0){
                    sendMsgIB.setVisibility(View.VISIBLE);
                    idIVRecord.setVisibility(View.GONE);
                } else {
                    sendMsgIB.setVisibility(View.GONE);
                    idIVRecord.setVisibility(View.VISIBLE);
                }

            }
        });

        mRequestQueue = Volley.newRequestQueue(LiveChatActivity.this);
        mRequestQueue.getCache().clear();
        messageModalArrayList = new ArrayList<>();
        sendMsgIB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userMsgEdt.getText().toString().isEmpty()) {
                    Toast.makeText(LiveChatActivity.this, "Please enter your message..", Toast.LENGTH_SHORT).show();
                    return;
                }
                msgType = "text";
                sendMessage(userMsgEdt.getText().toString());
                userMsgEdt.setText("");
            }
        });

        idIVRecord = findViewById(R.id.idIVRecord);
        idIVRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text");
                msgType = "voice";
                try {
                    startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
                }
                catch (Exception e) {
                    Toast.makeText(LiveChatActivity.this, " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
        messageRVAdapter = new MessageRVAdapter(messageModalArrayList, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(LiveChatActivity.this, RecyclerView.VERTICAL, false);
        chatsRV.setLayoutManager(linearLayoutManager);
        chatsRV.setAdapter(messageRVAdapter);
    }

    private void sendMessage(String userMsg) {
        messageModalArrayList.add(new MessageModal(userMsg, USER_KEY));
        messageRVAdapter.notifyDataSetChanged();

        String url = "http://115.69.213.102:5555/chat_service?text=" + userMsg;
        Log.i(TAG, "sendMessage: " + url);

        // creating a variable for our request queue.
        RequestQueue queue = Volley.newRequestQueue(LiveChatActivity.this);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {
                    JSONObject objResponse = new JSONObject(response);
                    String res = objResponse.getString("response");
                    if (msgType.equalsIgnoreCase("voice"))
                        textToSpeech(res);
                    if (res.equalsIgnoreCase("")) {
                        res = "Dont get any response yet.";
                    }
                    messageModalArrayList.add(new MessageModal(res, BOT_KEY));
                    messageRVAdapter.notifyDataSetChanged();
                    chatsRV.scrollToPosition(messageModalArrayList.size() - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error",error.toString());
            }
        });

        queue.add(request);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(
                        RecognizerIntent.EXTRA_RESULTS);

                if (!Objects.requireNonNull(result).get(0).equalsIgnoreCase("")){
                    sendMessage(Objects.requireNonNull(result).get(0));
                }
            }
        }
    }

    private TextToSpeech mTTS;
    private void textToSpeech(String msg){
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = mTTS.setLanguage(Locale.ENGLISH);
                    mTTS.setPitch(1.1f);
                    mTTS.setSpeechRate(1.1f);
                    mTTS.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });
    }
}
