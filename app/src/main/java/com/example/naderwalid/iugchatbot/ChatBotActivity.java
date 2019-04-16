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

import com.ibm.watson.developer_cloud.conversation.v1.ConversationService;
import com.ibm.watson.developer_cloud.conversation.v1.model.DialogNodeResponse;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageRequest;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;
import com.ibm.watson.developer_cloud.http.ServiceCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
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
//        image_view = findViewById(R.id.image);
//        image_view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent =new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_GET_CONTENT);
//                startActivityForResult(Intent.createChooser(intent,"Select Contact Image"),REQUEST_CODE_PICK_IMAGE);
//            }
//        });
        mainContext = getApplicationContext();
        messageAdapter = new MessageAdapter(mainContext);
        messages_view.setAdapter(messageAdapter);
        final String inputWorkspaceId = getString(R.string.conversation_workspaceId);
        if (ContextCompat.checkSelfPermission(ChatBotActivity.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {
            conversationService = initConversationService();
            MessageResponse response = null;
            conversationAPI(String.valueOf(message_field.getText()), context, inputWorkspaceId);

        } else {
            requestInternetPermission();
        }





        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conversationAPI(String.valueOf(message_field.getText()), context, inputWorkspaceId);
                message_field.getText().clear();
            }
        });
    }
    private ConversationService initConversationService() {
        ConversationService service = new ConversationService(ConversationService.VERSION_DATE_2016_07_11);
        String username = getString(R.string.conversation_username);
        String password = getString(R.string.conversation_password);
        service.setUsernameAndPassword(username, password);
        service.setEndPoint(getString(R.string.conversation_url));
        return service;
    }
    public void conversationAPI(String input, Map context, String workspaceId) {

        //conversationService
        MessageRequest newMessage = new MessageRequest.Builder()
                .inputText(input).context(context).build();
        if (message_field.getText().length()>0){
            messageAdapter.add(new Message(input,user,I_AM_USER));
            messages_view.smoothScrollToPosition(messages_view.getCount() - 1);
        }
        //cannot use the following as it will attempt to run on the UI thread and crash
//    MessageResponse response = conversationService.message(workspaceId, newMessage).execute();

        //use the following so it runs on own async thread
        //then when get a response it calls displayMsg that will update the UI
        conversationService.message(workspaceId, newMessage).enqueue(new ServiceCallback<MessageResponse>() {
            @Override
            public void onResponse(MessageResponse response) {
                //output to system log output, just for verification/checking
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
                Toast.makeText(ChatBotActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }
    private void requestInternetPermission() {
        // TODO - Check if showing permission rationale needed
        // If yes, call showExplanationDialog() to explain why you need this permission
        // If not, request the permission from the Android system
        if (ActivityCompat.shouldShowRequestPermissionRationale(ChatBotActivity.this, Manifest.permission.INTERNET)) {
            showExplanationDialog();

        } else {
            ActivityCompat.requestPermissions(ChatBotActivity.this, new String[]{Manifest.permission.INTERNET}, PERMISSIONS_REQUEST_INTERNET);

        }

    }
  /* public void getDialogNode (DialogNodeResponse dialogNode){
        handler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
   }*/
    public void displayMsg(final MessageResponse msg)
    {
        final MessageResponse mssg=msg;
        handler.post(new Runnable() {

            @Override
            public void run() {

                //from the WCS API response
                //https://www.ibm.com/watson/developercloud/conversation/api/v1/?java#send_message
                //extract the text from output to display to the user
               if (!mssg.getText().isEmpty()){

                    String text = mssg.getText().get(0);
                    //now output the text to the UI to show the chat history
                    messageAdapter.add(new Message(text,bot,I_AM_BOT));
                    messages_view.smoothScrollToPosition(messages_view.getCount() - 1);
                   //set the context, so that the next time we call WCS we pass the accumulated context
                    context = mssg.getContext();
                }else{
                    Toast.makeText(mainContext, ""+mssg, Toast.LENGTH_LONG).show();

                }

//                //now output the text to the UI to show the chat history
//                messageAdapter.add(new Message(text,bot,I_AM_BOT));
//                messages_view.smoothScrollToPosition(messages_view.getCount() - 1);
//
//                //set the context, so that the next time we call WCS we pass the accumulated context
//                context = mssg.getContext();

                //rather than converting response to a JSONObject and parsing through it
                //we can use the APIs for the MessageResponse .getXXXXX() to get the values as shown above
                //keeping the following just in case need this at a later date
                //
                //          https://developer.android.com/reference/org/json/JSONObject.html

               /* try {
                    JSONObject jObject= new JSONObject(msg.getContext());
                    JSONObject jsonOutput = jObject.getJSONObject("output");
                    JSONArray jArray1 = jsonOutput.getJSONArray("text");
                    JSONArray jArray3 = jsonOutput.getJSONArray("option");
                    JSONArray jArray2= jObject.getJSONArray("intents");



                    if (jArray3 != null){
                        for (int i=0; i < jArray3.length(); i++)
                        {
                            try {
                                String textContent = String.valueOf(jArray3.getString(i));
                                System.out.println(textContent);
                                messageAdapter.add(new Message(textContent,bot,I_AM_BOT));
                                messages_view.smoothScrollToPosition(messages_view.getCount() - 1);
                            } catch (JSONException e) {
                                // Oops
                                System.out.println(e);
                            }
                        }
                    }


                    if (jArray1 != null){
                        for (int i=0; i < jArray1.length(); i++)
                        {
                            try {
                                String textContent = String.valueOf(jArray1.getString(i));
                                System.out.println(textContent);
                                messageAdapter.add(new Message(textContent,bot,I_AM_BOT));
                                messages_view.smoothScrollToPosition(messages_view.getCount() - 1);
                            } catch (JSONException e) {
                                // Oops
                                System.out.println(e);
                            }
                        }
                    }
                    if(jArray2 != null){
                        for (int i=0; i < jArray2.length(); i++)
                        {
                            try {
                                JSONObject oneObject = jArray2.getJSONObject(i);
                                // Pulling items from the array
                                String oneObjectsItem = oneObject.getString("confidence");
                                String oneObjectsItem2 = oneObject.getString("intent");
                                String jOutput = oneObjectsItem+" : "+oneObjectsItem2;
                                messageAdapter.add(new Message(jOutput,bot,I_AM_BOT));
                                messages_view.smoothScrollToPosition(messages_view.getCount() - 1);

                            } catch (JSONException e) {
                                // Oops
                            }
                        }
                    }
                }catch (JSONException ex){

                }
                */

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
