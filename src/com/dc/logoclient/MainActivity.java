package com.dc.logoclient;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private TextView connectedDevice;
	private EditText commandField;
	private Button sendButton;
	private Button queueButton;
	private List<String> commands;
	private BluetoothDevice device;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		connectedDevice = (TextView) findViewById(R.id.connectedDevice);
		commandField = (EditText) findViewById(R.id.editCommand);
		sendButton = (Button) findViewById(R.id.buttonSend);
		queueButton = (Button) findViewById(R.id.buttonQueue);
		commands = new ArrayList<String>();

		// Button press event listener
		sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!commandField.getText().toString().isEmpty()) {
					commands.add(commandField.getText().toString());

					commandField.setText("");
				}

				if (!commands.isEmpty()) {
					SendMessage sendMessageTask = new SendMessage();
					sendMessageTask.execute();
				}
			}
		});

		queueButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!commandField.getText().toString().isEmpty()) {
					commands.add(commandField.getText().toString());
					commandField.setText("");
				}
			}
		});

		// Button settings = (Button) findViewById(R.id.buttonSettings);
		// settings.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Intent i = new Intent(MainActivity.this, SettingsActivity.class);
		// startActivity(i);
		// }
		// });

		BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
		btAdapter.cancelDiscovery();

		BroadcastReceiver receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (action.equals(BluetoothDevice.ACTION_FOUND)) {
					device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					// TODO: Will use the first one it finds. Filter to only use
					// the raspberry pi
					// TODO: Achieve this through ping/response? Display a list
					// to choose from?
					connectedDevice.setText("Connected to: " + device.getName());
				}
			}
		};

		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(receiver, filter);
		btAdapter.startDiscovery();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		// int id = item.getItemId();
		// if (id == R.id.action_settings) {
		// return true;
		// }
		return super.onOptionsItemSelected(item);
	}

	private class SendMessage extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			BluetoothSocket btSocket;
			try {
				if (device != null) {
					// Connect to the remote server through bluetooth
					UUID uuid = UUID.fromString("04c6032b-0000-4000-8000-00805f9b34fc");
					btSocket = device.createRfcommSocketToServiceRecord(uuid);
					btSocket.connect();

					// Collect all the commands together into a String
					StringBuffer commandsSB = new StringBuffer();

					for (String command : commands) {
						commandsSB.append(command);
						commandsSB.append(";");
					}

					commandsSB.append("\n");

					// Send the commands to the remote server
					OutputStream outStream = btSocket.getOutputStream();
					byte[] msgBuffer = commandsSB.toString().getBytes();
					outStream.write(msgBuffer);

					// Clear the list of commands
					commands.clear();

					// Close the connection
					btSocket.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}
}