package edu.temple.androidbeam;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.EditText;

public class MainActivity extends Activity implements CreateNdefMessageCallback {

	EditText message;
	
	IntentFilter[] intentFiltersArray;
	PendingIntent pendingIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		message = (EditText) findViewById(R.id.message);
		
		/**NFC**/
		NfcAdapter mNfcAdapter;
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		if (mNfcAdapter != null)
			mNfcAdapter.setNdefPushMessageCallback(this, this); // Use setBeamPushUris for large files

		pendingIntent = PendingIntent.getActivity(
				this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		try {
			ndefFilter.addDataType("*/*");    /* Handles all MIME based dispatches.
	                                       You should specify only the ones that you need. */
		}
		catch (MalformedMimeTypeException e) {
			throw new RuntimeException("fail", e);
		}
		
		intentFiltersArray = new IntentFilter[] {ndefFilter};
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		NdefMessage msg = new NdefMessage(
				new NdefRecord[] { NdefRecord.createMime(
						"application/edu.temple.androidbeam", message.getText().toString().getBytes())

				});
		return msg;
	}
	
	
	@Override
	public void onResume(){
		super.onResume();
		if (NfcAdapter.getDefaultAdapter(this) != null) {
			NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
			// Check to see that the Activity started due to an Android Beam
			if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
				processBeam(getIntent());
			}
		}
	}
	
	public void onNewIntent(Intent intent) {
		processBeam(intent);
	}
	
	/**
	 * Parses the NDEF Message from the intent and displayes it on the EditText
	 */
	void processBeam(Intent intent) {
		Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
				NfcAdapter.EXTRA_NDEF_MESSAGES);
		// only one message sent during the beam
		if (rawMsgs != null){
			NdefMessage msg = (NdefMessage) rawMsgs[0];
			// record 0 contains the MIME type, record 1 is the AAR, if present
			String messageString = new String(msg.getRecords()[0].getPayload());
			
			message.setText(messageString);
		}
	}
}
