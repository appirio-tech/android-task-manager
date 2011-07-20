package com.mytaskmanager;


import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class AboutActivity extends Activity{	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
		
		this.setTitle(this.getResources().getString((R.string.about)));
		String descText = getResources().getString(R.string.message_about);
		descText = descText.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
		((TextView)findViewById(R.id.Description)).setText(Html.fromHtml(descText));
	}	
}
