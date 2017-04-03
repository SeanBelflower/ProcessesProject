package team8.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class OnlineOptions extends AppCompatActivity
{
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_options);

        Spinner chipsSpinner = (Spinner) findViewById(R.id.chipsSpinner);
        ArrayAdapter<CharSequence> chipsAdapter = ArrayAdapter.createFromResource(this,
                R.array.chipsOptions, android.R.layout.simple_spinner_item);
        chipsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chipsSpinner.setAdapter(chipsAdapter);

        final Spinner botsSpinner = (Spinner) findViewById(R.id.botsSpinner);
        ArrayAdapter<CharSequence> botsAdapter = ArrayAdapter.createFromResource(this,
                R.array.onlineBotsOptions, android.R.layout.simple_spinner_item);
        botsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        botsSpinner.setAdapter(botsAdapter);

        final EditText botNameField = new EditText(this);
        botsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {

            public void onNothingSelected(AdapterView<?> parent)
            {

            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(botsSpinner.getSelectedItem().toString().equals("0"))
                {
                    EditText bot1Name = (EditText)findViewById(R.id.bot1Name);
                    bot1Name.setVisibility(View.GONE);
                    EditText bot2Name = (EditText)findViewById(R.id.bot2Name);
                    bot2Name.setVisibility(View.GONE);
                    EditText bot3Name = (EditText)findViewById(R.id.bot3Name);
                    bot3Name.setVisibility(View.GONE);
                    EditText bot4Name = (EditText)findViewById(R.id.bot4Name);
                    bot4Name.setVisibility(View.GONE);
                }
                if(botsSpinner.getSelectedItem().toString().equals("1"))
                {
                    EditText bot1Name = (EditText)findViewById(R.id.bot1Name);
                    bot1Name.setVisibility(View.VISIBLE);
                    EditText bot2Name = (EditText)findViewById(R.id.bot2Name);
                    bot2Name.setVisibility(View.GONE);
                    EditText bot3Name = (EditText)findViewById(R.id.bot3Name);
                    bot3Name.setVisibility(View.GONE);
                    EditText bot4Name = (EditText)findViewById(R.id.bot4Name);
                    bot4Name.setVisibility(View.GONE);
                }
                if(botsSpinner.getSelectedItem().toString().equals("2"))
                {
                    EditText bot1Name = (EditText)findViewById(R.id.bot1Name);
                    bot1Name.setVisibility(View.VISIBLE);
                    EditText bot2Name = (EditText)findViewById(R.id.bot2Name);
                    bot2Name.setVisibility(View.VISIBLE);
                    EditText bot3Name = (EditText)findViewById(R.id.bot3Name);
                    bot3Name.setVisibility(View.GONE);
                    EditText bot4Name = (EditText)findViewById(R.id.bot4Name);
                    bot4Name.setVisibility(View.GONE);
                }
                if(botsSpinner.getSelectedItem().toString().equals("3"))
                {
                    EditText bot1Name = (EditText)findViewById(R.id.bot1Name);
                    bot1Name.setVisibility(View.VISIBLE);
                    EditText bot2Name = (EditText)findViewById(R.id.bot2Name);
                    bot2Name.setVisibility(View.VISIBLE);
                    EditText bot3Name = (EditText)findViewById(R.id.bot3Name);
                    bot3Name.setVisibility(View.VISIBLE);
                    EditText bot4Name = (EditText)findViewById(R.id.bot4Name);
                    bot4Name.setVisibility(View.GONE);
                }
                if(botsSpinner.getSelectedItem().toString().equals("4"))
                {
                    EditText bot1Name = (EditText)findViewById(R.id.bot1Name);
                    bot1Name.setVisibility(View.VISIBLE);
                    EditText bot2Name = (EditText)findViewById(R.id.bot2Name);
                    bot2Name.setVisibility(View.VISIBLE);
                    EditText bot3Name = (EditText)findViewById(R.id.bot3Name);
                    bot3Name.setVisibility(View.VISIBLE);
                    EditText bot4Name = (EditText)findViewById(R.id.bot4Name);
                    bot4Name.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
