package com.vovasoft.wallet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;

import com.vovasoft.wallet.Widgets.LabelledSpinner;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class SettingsActivity extends AppCompatActivity {

    public static String RUS_LANGUAGE = "ru";
    public static String ENG_LANGUAGE = "en";
    public static String UZB_LANGUAGE = "uz";
    public static String CASH = "cash";
    public static String CARD = "card";
    public static String CASH_SPENT = "cash_spent";
    public static String CARD_SPENT = "card_spent";
    public static String DAYS = "days";
    public static String DEADLINE = "deadline";
    public static String PAST_DAY = "past_day";


    private SharedPreferences prefs;

    private final String[] languages = {RUS_LANGUAGE, ENG_LANGUAGE, UZB_LANGUAGE};

    private EditText cash;
    private EditText card;
    private EditText date;

    private String locale = null;
    private boolean isOnBackPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        Helper.getInstance().setLocale(prefs.getString("Language", "ru"));
        Helper.getInstance().setLanguage(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        locale = Helper.getInstance().getLocale();

        if (prefs.getBoolean("FirstTime", true)) {
            Helper.getInstance().fillCategoriesFirstTime(this);
        } else {
            setupActionBar();
        }
        setupSaveButton();

        setupLanguageSpinner();

        setupBudgetEditTexts();
    }


    private void setupBudgetEditTexts() {
        cash = (EditText) findViewById(R.id.cash);
        card = (EditText) findViewById(R.id.card);
        date = (EditText) findViewById(R.id.date);

        cash.addTextChangedListener(textWatcher(cash));
        card.addTextChangedListener(textWatcher(card));


        String cashText = prefs.getString(CASH, "");
        String cardText = prefs.getString(CARD, "");
        String dateText = prefs.getString(DEADLINE, "");

        cash.setText(cashText.length() == 1 && cashText.charAt(0) == '0' ? "" : cashText);
        card.setText(cardText.length() == 1 && cardText.charAt(0) == '0' ? "" : cashText);
        date.setText(dateText);

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
                        String ms = month < 10 ? "0"+(month + 1) : String.format("%d", month + 1);
                        String ds = day < 10 ? "0" + day : String.format("%d", day);
                        date.setText(String.format("%s.%s.%d", ds, ms, year));
                    }
                };

                DatePickerDialog datePickerDialog = new DatePickerDialog(view.getContext(),
                        dateSetListener,
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

                datePickerDialog.show();

            }
        });
    }


    private TextWatcher textWatcher(final EditText editText) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                editText.removeTextChangedListener(this);
                if (editable.length() > 0) {
                    Long sum = Long.parseLong(editable.toString().replaceAll(" ", ""));
                    String text = Helper.getInstance().getSpacedString(String.format("%d", sum));
                    editText.setText(text);
                    editText.setSelection(text.length());
                }
                editText.addTextChangedListener(this);
            }
        };
    }


    private boolean saveData() {

        boolean ok = true;

        if (date.getText().length() == 0) {
            date.setError("");
            ok = false;
        }

        if (ok) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
            String today = dateFormat.format(Calendar.getInstance().getTime());
            String deadline = date.getText().toString();

            Days days = null;
            try {
                days = Days.daysBetween(new DateTime(dateFormat.parse(today)), new DateTime(dateFormat.parse(deadline)));
            } catch (Exception e) {
                e.printStackTrace();
            }

            int daysLeft = days != null ? days.getDays() + 1 : 0;
            String sCash = cash.getText().length() > 0 ? cash.getText().toString() : "0";
            String sCard = card.getText().length() > 0 ? card.getText().toString() : "0";

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(CASH, sCash);
            editor.putString(CARD, sCard);
            editor.putString(DEADLINE, deadline);
            editor.putString(PAST_DAY, today);
            editor.putString(CASH_SPENT, "0");
            editor.putString(CARD_SPENT, "0");
            editor.putInt(DAYS, daysLeft);

            editor.apply();
        }

        return ok;
    }


    private void setupLanguageSpinner() {

        final ArrayList<String> languages = new ArrayList<>();
        languages.add(getText(R.string.rus).toString());
        languages.add(getText(R.string.eng).toString());
        languages.add(getText(R.string.uzb).toString());

        LabelledSpinner languageSpinner = (LabelledSpinner) findViewById(R.id.language_spinner);
        languageSpinner.setItemsArray(languages);

        if (locale.equals(RUS_LANGUAGE)) {
            languageSpinner.setSelection(0);
        } else if (locale.equals(ENG_LANGUAGE)) {
            languageSpinner.setSelection(1);
        } else {
            languageSpinner.setSelection(2);
        }

        languageSpinner.setOnItemChosenListener(new LabelledSpinner.OnItemChosenListener() {
            @Override
            public void onItemChosen(View labelledSpinner, AdapterView<?> adapterView, View itemView, int position, long id) {
                if (!locale.equals(SettingsActivity.this.languages[position])) {
                    locale = SettingsActivity.this.languages[position];

                    Helper.getInstance().setLanguageChanged(true);
                    Helper.getInstance().setLocale(locale);
                    Helper.getInstance().setLanguage(getBaseContext());

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("Language", locale);
                    editor.apply();

                    Intent intent = getIntent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    finish();
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingChosen(View labelledSpinner, AdapterView<?> adapterView) {

            }
        });

    }


    private void setupActionBar() {

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.action_settings));
        }
    }


    private void setupSaveButton() {
        Button saveButton = (Button) findViewById(R.id.save_button);
        if (saveButton != null) {
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (saveData()) {

                        if (prefs.getBoolean("FirstTime", true)) {
                            prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putBoolean("FirstTime", false);
                            editor.apply();
                        }

                        onBackPressed();
                    }
                }
            });
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {

        if (getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE).getBoolean("FirstTime", true)) {
            setResult(0);
            finish();
        } else {
            isOnBackPressed = true;
            super.onBackPressed();
        }
    }


    @Override
    public void onPause() {
        super.onPause();

        if (!isOnBackPressed) {
            overridePendingTransition(0, 0);
        }
    }


}
