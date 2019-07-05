package com.example.naderwalid.iugchatbot;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.naderwalid.iugchatbot.arabicstemmer.ArabicStemmer;
import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.DialogNodeResponse;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatBotActivity extends AppCompatActivity {
    MemberData user,bot;
    ListView messages_view;
    ImageButton send;
    EditText message_field;
    private static ConversationService conversationService;
    Map context = new HashMap();
    private Handler handler = new Handler();
    MessageAdapter messageAdapter;
    Context mainContext;
    final boolean I_AM_USER=true;
    final boolean I_AM_BOT=false;
    private static final int PERMISSIONS_REQUEST_INTERNET = 101;
    private static final int REQUEST_CODE_PICK_IMAGE = 1;

    ImageView image_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);
        bot = new MemberData("IUG BOT","Blue");
        user = new MemberData("User","Blue");
        messages_view = findViewById(R.id.messages_view);
        send = findViewById(R.id.send_button);
        message_field = findViewById(R.id.editText);
        mainContext = getApplicationContext();
        messageAdapter = new MessageAdapter(mainContext);
        messages_view.setAdapter(messageAdapter);
        final String inputWorkspaceId = getString(R.string.conversation_workspaceId);
        if (ContextCompat.checkSelfPermission(ChatBotActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            conversationService = initConversationService();
            MessageResponse response = null;
            conversationAPI(String.valueOf(message_field.getText()), context, inputWorkspaceId,String.valueOf(message_field.getText()));

        } else {
            requestInternetPermission();
        }

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message_field.getText().length() != 0){
                    String message = stemming(String.valueOf(message_field.getText()));
                    conversationAPI(message, context, inputWorkspaceId,String.valueOf(message_field.getText()));
                    message_field.getText().clear();
                }

            }
        });
    }
    private String stemming(String targetLine){
        List<String> wordsList =null;
            wordsList = convertLineToWords(targetLine);
            StringBuilder sb =new StringBuilder();
            String afterStemming ;
            for (int i = 0; i < wordsList.size(); i++) {
                afterStemming = stemmingWords(wordsList.get(i));
                sb.append(afterStemming+" ");
            }
            String newLine = sb.toString();

        return  newLine;
    }
    private String stemmingWords(String word){
        ArabicStemmer arabicStemmer = new ArabicStemmer();
        arabicStemmer.setCurrent(word);
        arabicStemmer.stem();

        return arabicStemmer.getCurrent();
    }
    private List<String> convertLineToWords(String line){
        String[] words = null ;
        List<String> wordsList = new ArrayList<>();
            String clean = line.trim().replaceAll("\\s+", " ");
            words = clean.split(" ");
        for (String word : words) {
            wordsList.add(word);
        }

        return wordsList;
    }
    private ConversationService initConversationService() {
        ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
        String username = getString(R.string.conversation_username);
        String password = getString(R.string.conversation_password);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint(getString(R.string.conversation_url));
        return service;
    }
    public void conversationAPI(String input, Map context, String workspaceId,String originalMessage) {

        MessageRequest newMessage = new MessageRequest.Builder()
                .inputText(input).context(context).build();
        if (message_field.getText().length()>0){
            messageAdapter.add(new Message(originalMessage,user,I_AM_USER));
            messages_view.smoothScrollToPosition(messages_view.getCount() - 1);
        }

        conversationService.message(workspaceId, newMessage).enqueue(new ServiceCallback<MessageResponse>() {
            @Override
            public void onResponse(MessageResponse response) {

                System.out.println(response);
                displayMsg(response);
            }
            @Override
            public void onFailure(Exception e) {
                showError(e);
            }
        });
    }
    private void showError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ChatBotActivity.this, "رجاءاً, تأكد من إتصالك بالإنترنت", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void displayMsg(final MessageResponse msg)
    {
        final MessageResponse mssg=msg;
        handler.post(new Runnable() {

            @Override
            public void run() {

               if (!mssg.getText().isEmpty()){
                    String text = mssg.getText().get(0);
                    messageAdapter.add(new Message(text,bot,I_AM_BOT));
                    messages_view.smoothScrollToPosition(messages_view.getCount() - 1);
                    context = mssg.getContext();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode==REQUEST_CODE_PICK_IMAGE){
                image_view.setImageURI(data.getData());

            }
        }
    }
    private void requestInternetPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(ChatBotActivity.this, Manifest.permission.INTERNET)) {
            showExplanationDialog();

        } else {
            ActivityCompat.requestPermissions(ChatBotActivity.this, new String[]{Manifest.permission.INTERNET}, PERMISSIONS_REQUEST_INTERNET);

        }

    }

    private void showExplanationDialog() {
        // TODO: show alert dialog that show the following message to the user
        // "Read Contacts permission needed to be able to bring contacts here"
        // Dialog should contain two buttons, "Ok" and "Cancel"
        // If the user click Ok, request permission again
        // If the user click Cancel, only dismiss the dialog
        new AlertDialog.Builder(this)
                .setMessage("Internet permission needed to be able to use app")
                .setPositiveButton(R.string.Ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(ChatBotActivity.this, new String[]{"android.permission.INTERNET"}, PERMISSIONS_REQUEST_INTERNET);
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // TODO: check the request code
        // if it's the same as code you used when you request the permission
        // Check if the permission is granted or not
        // If yes, call loadContacts, if not show a Toast message to the user
        switch (requestCode) {
            case PERMISSIONS_REQUEST_INTERNET:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Sorry, I it's not allowed for me to do this", Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }
}
