package com.example.trollo.technologic;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.nfc.NfcAdapter;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;

public class MainActivity extends AppCompatActivity {

    NfcAdapter nfc;
    EditText tagTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tagTextField = (EditText) findViewById(R.id.tagTextField);

        nfc = NfcAdapter.getDefaultAdapter(this);

        if(nfc != null && nfc.isEnabled()){
            Toast.makeText(this, "NFC available!", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "NFC not available!", Toast.LENGTH_LONG).show();
        }

        startReadingTags();
    }

    private void startReadingTags() {
        while(true){

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(intent.hasExtra(nfc.EXTRA_TAG)){
            //Toast.makeText(this, "NfcIntent!", Toast.LENGTH_SHORT).show();
            //Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //NdefMessage ndefMessage = createNdefMessage("As pik");
            //writeNdefMessage(tag, ndefMessage);
            Parcelable[] parcelables = intent.getParcelableArrayExtra(nfc.EXTRA_NDEF_MESSAGES);
            if(parcelables != null && parcelables.length > 0) {
                readTextFromMessage((NdefMessage)parcelables[0]);

            }
            else{
                Toast.makeText(this, "No ndfef messages", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void readTextFromMessage(NdefMessage mess) {
        NdefRecord[] records = mess.getRecords();
        if(records != null){
            NdefRecord record = records[0];
            String tagContent = getTextFromNdefRecord(record);
            Toast.makeText(this, tagContent, Toast.LENGTH_SHORT).show();
        }
        else{

        }
    }

    private String getTextFromNdefRecord(NdefRecord record) {
        String tagContent = null;
        try{
            byte[] payload = record.getPayload();
            String textEncoding = "UTF-8";
            int languageSize = payload[0] & 0063;
            tagContent = new String(payload, languageSize + 1,
                    payload.length - languageSize - 1, textEncoding);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return tagContent;
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableForegroundDispatchSystem();
    }

    @Override
    protected void onPause() {
        super.onPause();
        disableForegroundDispatchSystem();
    }

    private void enableForegroundDispatchSystem(){
        Intent intent = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pending = PendingIntent.getActivities(this, 0, new Intent[]{intent}, 0);
        IntentFilter[] intentFilters = new IntentFilter[] {};
        nfc.enableForegroundDispatch(this, pending, intentFilters, null);
    }

    private void disableForegroundDispatchSystem(){
        nfc.disableForegroundDispatch(this);
    }

    private void formatTag(Tag tag, NdefMessage mess){
        try{
            NdefFormatable ndef = NdefFormatable.get(tag);
            if(ndef == null){
                Toast.makeText(this, "Tag is not ndef formattable", Toast.LENGTH_SHORT).show();

            }
            else{
                ndef.connect();
                ndef.format(mess);
                ndef.close();
            }
        }
        catch(Exception e){
            Log.e("formatTag", e.getMessage());
        }
    }

    private NdefMessage createNdefMessage(String content){
        NdefRecord record = NdefRecord.createTextRecord(null, tagTextField.getText().toString());
        NdefMessage mess = new NdefMessage(new NdefRecord[]{record});
        return mess;
    }

    private void writeNdefMessage(Tag tag, NdefMessage mess){
        try{
            if(tag == null){
                Toast.makeText(this, "Tag object cannot be null", Toast.LENGTH_SHORT).show();


            }
            else{
                Ndef ndef = Ndef.get(tag);
                if(ndef == null){
                    formatTag(tag, mess);
                }
                else{
                    ndef.connect();
                    if(!ndef.isWritable()){
                        Toast.makeText(this, "Tag is not writable", Toast.LENGTH_SHORT).show();

                        ndef.close();
                        return;
                    }
                    ndef.writeNdefMessage(mess);
                    ndef.close();
                    Toast.makeText(this, "Tag written!", Toast.LENGTH_SHORT).show();

                }

            }
        }
        catch(Exception e){
            Log.e("formatTag", e.getMessage());
        }
    }


}
