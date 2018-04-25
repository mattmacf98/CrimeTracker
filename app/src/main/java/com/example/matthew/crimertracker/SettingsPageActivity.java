package com.example.matthew.crimertracker;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsPageActivity extends AppCompatActivity {
    SeekBar seekbar;
    int progress = 24;
    TextView alert_textView;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    EditText ICE_Number_Edit_Text;
    String ICE_Number;
    boolean checkBoxValue;
    CheckBox notifications_check_box;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);
        setTitle("Settings");
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        //sp = getSharedPreferences("Settings", MODE_PRIVATE);
        //get values and set up editor
        sp = getSharedPreferences("settings", 0);
        editor = sp.edit();
        progress = sp.getInt("progress",0);
        ICE_Number = sp.getString("ICE_Number","");
        progress = sp.getInt("progress",0);
        ICE_Number = sp.getString("ICE_Number","");
        checkBoxValue = sp.getBoolean("CheckBoxValue",false);

        //set up seekbar
        seekbar = (SeekBar) findViewById(R.id.alert_seek_bar);
        seekbar.setMax(100);
        seekbar.setProgress(progress);

        //set up notifications check box
        notifications_check_box = (CheckBox) findViewById(R.id.notifications_check_box);
        notifications_check_box.setChecked(checkBoxValue);

        //set up ICE input
        final EditText ICE_Number_Edit_Text;
        ICE_Number_Edit_Text = (EditText) findViewById(R.id.ICE_Number_Edit_Text);
        ICE_Number_Edit_Text.setText(ICE_Number, TextView.BufferType.EDITABLE);

        //set up alert text view
        alert_textView = (TextView) findViewById(R.id.alert_text_view_display_int);
        alert_textView.setText(""+progress);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean fromUser) {
                progress = i;
                alert_textView.setText(""+progress);
                editor.putInt("progress", i);
                editor.commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        notifications_check_box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(notifications_check_box.isChecked()) {
                    editor.putBoolean("CheckBoxValue", true);
                    editor.commit();
                }
                else {
                    editor.putBoolean("CheckBoxValue", false);
                    editor.commit();
                }

            }
        });

        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String temp = ICE_Number_Edit_Text.getText().toString();
                editor.putString("ICE_Number", temp);
                editor.commit();
            }
        };

        ICE_Number_Edit_Text.addTextChangedListener(tw);




    }





}
