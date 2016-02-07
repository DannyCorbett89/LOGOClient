package com.dc.logoclient;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {
	private EditText serverAddressField;
	private EditText serverPortField;
	private EditText commandField;
	private Button sendButton;
	private Button queueButton;
	private List<String> commands;
	private String serverAddress;
	private int serverPort;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		serverAddressField = (EditText) findViewById(R.id.editServerAddress);
		serverAddressField.setText("192.168.1.30");
		serverPortField = (EditText) findViewById(R.id.editServerPort);
		serverPortField.setText("83");
		commandField = (EditText) findViewById(R.id.editCommand);
		sendButton = (Button) findViewById(R.id.buttonSend);
		queueButton = (Button) findViewById(R.id.buttonQueue);
		commands = new ArrayList<String>();

		// Button press event listener
		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!commandField.getText().toString().isEmpty()) {
					serverAddress = serverAddressField.getText().toString();
					serverPort = Integer.parseInt(serverPortField.getText().toString());
					commands.add(commandField.getText().toString());

					commandField.setText("");
				}

				if (!commands.isEmpty()) {
					SendMessage sendMessageTask = new SendMessage();
					sendMessageTask.execute();
				}
			}
		});

		queueButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (!commandField.getText().toString().isEmpty()) {
					commands.add(commandField.getText().toString());
					commandField.setText("");
				}
			}
		});

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
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class SendMessage extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Socket socket = new Socket(serverAddress, serverPort);
				StringBuffer commandsSB = new StringBuffer();
				StringBuffer response = new StringBuffer();

				for (String command : commands) {
					commandsSB.append(command);
					commandsSB.append(";");
				}

				commandsSB.append("x");

				PrintWriter printwriter = new PrintWriter(socket.getOutputStream(), true);
				printwriter.write(commandsSB.toString() + (char) 13);

				printwriter.flush();
				printwriter.close();

				commands.clear();

				BufferedInputStream bis = new BufferedInputStream(socket.getInputStream());
				InputStreamReader isr = new InputStreamReader(bis);
				int character;

				while ((character = isr.read()) != 13) {
					response.append((char) character);
				}

				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}
	}
}