package com.vovasoft.wallet;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.vovasoft.wallet.Database.SpentDatabaseHelper;
import com.vovasoft.wallet.Widgets.CustomFragmentPagerAdapter;
import com.vovasoft.wallet.Widgets.LabelledSpinner;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    public static int CASH = 0;
    public static int CARD = 1;
    public static int ALL = 2;

    private ViewPager viewPager;
    private SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        Helper.getInstance().setLocale(prefs.getString("Language", "ru"));
        Helper.getInstance().setLanguage(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (prefs.getBoolean("FirstTime", true)) {
            Intent settings= new Intent(this, SettingsActivity.class);
            startActivityForResult(settings, 0);
        }

        updateSettings();
        setupToolbar();
        setupViewPager();
        setupFAB();
        initRankAdd();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settings= new Intent(this, SettingsActivity.class);
            startActivity(settings);
            return true;
        } else if (id == R.id.action_history) {
            Intent settings= new Intent(this, HistoryActivity.class);
            startActivity(settings);
            return true;
        } else if (id == R.id.action_feedback) {
            writeFeedback();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void updateSettings() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String today = dateFormat.format(Calendar.getInstance().getTime());
        String pastDay = prefs.getString(SettingsActivity.PAST_DAY, today);
        String deadline = prefs.getString(SettingsActivity.DEADLINE, today);

        if (!today.equals(pastDay)) {
            Days days = null;
            try {
                days = Days.daysBetween(new DateTime(dateFormat.parse(today)), new DateTime(dateFormat.parse(deadline)));
            } catch (Exception e) {
                e.printStackTrace();
            }
            int daysLeft = days != null ? days.getDays() + 1 : 0;


            long cash = Long.valueOf(prefs.getString(SettingsActivity.CASH, "0").replaceAll(" ", ""));
            long card = Long.valueOf(prefs.getString(SettingsActivity.CARD, "0").replaceAll(" ", ""));

            long cashSpent = Long.valueOf(prefs.getString(SettingsActivity.CASH_SPENT, "0").replaceAll(" ", ""));
            long cardSpent = Long.valueOf(prefs.getString(SettingsActivity.CARD_SPENT, "0").replaceAll(" ", ""));

            if (daysLeft > 0) {
                cash = (cash - cashSpent) < 0 ? 0 : (cash - cashSpent);
                card = (card - cardSpent) < 0 ? 0 : (card - cardSpent);
                daysLeft--;
            } else {
                cash = 0;
                card = 0;
                deadline = "";
            }

            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(SettingsActivity.DAYS, daysLeft);
            editor.putString(SettingsActivity.PAST_DAY, today);
            editor.putString(SettingsActivity.DEADLINE, deadline);
            editor.putString(SettingsActivity.CASH, String.format("%d", cash));
            editor.putString(SettingsActivity.CARD, String.format("%d", card));
            editor.putString(SettingsActivity.CASH_SPENT, "0");
            editor.putString(SettingsActivity.CARD_SPENT, "0");
            editor.apply();
        }
    }


    private void setupToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.available_today);
        }
    }


    private void setupViewPager() {
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        CustomFragmentPagerAdapter adapter = new CustomFragmentPagerAdapter(getSupportFragmentManager());

        SpentFragment cashFragment = SpentFragment.newInstance(SpentFragment.CASH);
        SpentFragment cardFragment = SpentFragment.newInstance(SpentFragment.CARD);
        SpentFragment allFragment = SpentFragment.newInstance(SpentFragment.ALL);

        adapter.addFragment(cashFragment, getString(R.string.cash));
        adapter.addFragment(cardFragment, getString(R.string.card));
        adapter.addFragment(allFragment, getString(R.string.all));
        if (viewPager != null) {
            viewPager.setAdapter(adapter);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    updateData();
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(viewPager);
        }
    }


    private void setupFAB() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.add_spent);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LayoutInflater inflater = LayoutInflater.from(view.getContext());
                    View dialogView = inflater.inflate(R.layout.dialog_add_spent_layout, null);
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(view.getContext());
                    dialogBuilder.setTitle(R.string.add_spent);
                    dialogBuilder.setView(dialogView);
                    AlertDialog dialog = dialogBuilder.create();

                    setupAddSpentDialogView(dialogView, dialog);

                    dialog.show();
                }
            });
        }
    }


    private void setupAddSpentDialogView(View dialogView, final AlertDialog dialog) {

        final EditText sum = (EditText) dialogView.findViewById(R.id.sum);
        final EditText desc = (EditText) dialogView.findViewById(R.id.info);
        final CheckBox cash = (CheckBox) dialogView.findViewById(R.id.cash);
        final CheckBox card = (CheckBox) dialogView.findViewById(R.id.card);
        final LabelledSpinner categorySpinner = (LabelledSpinner) dialogView.findViewById(R.id.category_spinner);
        final ArrayList<Pair<Integer, String>> categories = Helper.getInstance().loadCategoryList(this);

        ArrayList<String> spinnerList = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++)
            spinnerList.add(categories.get(i).second);

        categorySpinner.setItemsArray(spinnerList);

        sum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                sum.removeTextChangedListener(this);
                if (editable.length() > 0) {
                    Long sumNum = Long.parseLong(editable.toString().replaceAll(" ", ""));
                    String text = Helper.getInstance().getSpacedString(String.format("%d", sumNum));
                    sum.setText(text);
                    sum.setSelection(text.length());
                }
                sum.addTextChangedListener(this);
            }
        });

        cash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                card.setChecked(!cash.isChecked());
            }
        });

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cash.setChecked(!card.isChecked());
            }
        });

        if (viewPager.getCurrentItem() == CARD) {
            card.setChecked(true);
            cash.setChecked(false);
        }

        Button addSpentBtn = (Button) dialogView.findViewById(R.id.add_spent_btn);
        addSpentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sum.getText().length() == 0) {
                    sum.setError(getText(R.string.sum_hint).toString());
                } else {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = dateFormat.format(Calendar.getInstance().getTime());

                    ContentValues values = new ContentValues();
                    values.put("Date", date);
                    values.put("Sum", sum.getText().toString().replaceAll(" ", ""));
                    values.put("Category", categories.get(categorySpinner.getSelectedItemPosition()).first);
                    values.put("Desc", desc.getText().toString());
                    values.put("Cash", cash.isChecked());
                    values.put("Card", card.isChecked());

                    SpentDatabaseHelper databaseHelper = new SpentDatabaseHelper(view.getContext());
                    SQLiteDatabase database = databaseHelper.getWritableDatabase();
                    database.insertWithOnConflict("Spent", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    databaseHelper.close();

                    dialog.dismiss();

                    updateData();
                }

            }
        });

    }


    private void updateData() {
        CustomFragmentPagerAdapter adapter = (CustomFragmentPagerAdapter) viewPager.getAdapter();
        ((SpentFragment)adapter.getItem(viewPager.getCurrentItem())).updateData();
    }


    private void initRankAdd() {

        FrameLayout rankAdd = (FrameLayout)findViewById(R.id.rank_add);
        rankAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://rank.uz"));
                startActivity(browserIntent);
            }
        });
    }


    private void writeFeedback() {
        String mailForFeedBack = "info@rank.uz";
        String titleForFeedback = "Отзыв";
        String textForFeedback = "Очень крутое приложение";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mailForFeedBack));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, titleForFeedback);
        emailIntent.putExtra(Intent.EXTRA_TEXT, textForFeedback);
        startActivity(Intent.createChooser(emailIntent, "Send feedback"));
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == 0) {
            if (getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE).getBoolean("FirstTime", true)) {
                finish();
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        updateSettings();

        if (viewPager != null) {
            updateData();
        }

        if (Helper.getInstance().isLanguageChanged()) {
            Helper.getInstance().setLanguageChanged(false);
            Intent intent = getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            finish();
            startActivity(intent);
        }
    }

}
