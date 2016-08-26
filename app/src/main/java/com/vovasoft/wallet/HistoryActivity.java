package com.vovasoft.wallet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.vovasoft.wallet.Database.CategoryDatabaseHelper;
import com.vovasoft.wallet.Database.SpentDatabaseHelper;
import com.vovasoft.wallet.Database.SpentModel;
import com.vovasoft.wallet.Widgets.LabelledSpinner;
import com.vovasoft.wallet.Widgets.RecyclerItemClickListener;
import com.vovasoft.wallet.Widgets.SimpleDividerItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class HistoryActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ArrayList<SpentModel> spentData;
    private boolean showDetail = true;
    private String fDateFrom;
    private String fDateTill;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        Helper.getInstance().setLocale(prefs.getString("Language", "ru"));
        Helper.getInstance().setLanguage(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        spentData = new ArrayList<>();

        setupActionBar();
        setupDateFields();
        setupRecyclerView();
        updateData();
        loadBudgetInfo();
        setupDetailButton();
    }


    private void setupActionBar() {

        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.action_history));
        }
    }


    private void setupDateFields() {

        fDateFrom = null;
        fDateTill = null;

        final EditText dateFrom = (EditText) findViewById(R.id.date_from);
        final EditText dateTill = (EditText) findViewById(R.id.date_till);

        if (dateFrom != null) {
            dateFrom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
                            String ms = month < 9 ? "0" + (month + 1) : String.format("%d", month + 1);
                            String ds = day < 10 ? "0" + day : String.format("%d", day);

                            dateFrom.setText(String.format("%s.%s.%d", ds, ms, year));
                            fDateFrom = String.format("%d-%s-%s", year, ms, ds);
                            updateData();
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


        if (dateTill != null) {
            dateTill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
                            String ms = month < 9 ? "0" + (month + 1) : String.format("%d", month + 1);
                            String ds = day < 10 ? "0" + day : String.format("%d", day);

                            dateTill.setText(String.format("%s.%s.%d", ds, ms, year));
                            fDateTill = String.format("%d-%s-%s", year, ms, ds);
                            updateData();
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

    }


    private void setupDetailButton() {

        showDetail = true;

        final Button detail = (Button) findViewById(R.id.details);
        if (detail != null) {
            detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View detailView = view.getRootView().findViewById(R.id.list_card);

                    if (showDetail) {
                        detailView.setVisibility(View.VISIBLE);
                        showDetail = false;
                        detail.setText(view.getContext().getText(R.string.hide_details));
                    } else {
                        detailView.setVisibility(View.GONE);
                        showDetail = true;
                        detail.setText(view.getContext().getText(R.string.show_details));
                    }

                }
            });
        }
    }


    private void loadBudgetInfo() {

        long spent = 0;
        for (SpentModel model : spentData) {
            spent += Integer.parseInt(model.getSum());
        }

        TextView spentInfo = (TextView) findViewById(R.id.info_spent);

        if (spentInfo != null) {
            spentInfo.setText(Helper.getInstance().getSpacedString(String.format("%d", spent)));
        }
    }


    public void updateData() {


        if (fDateFrom == null || fDateTill == null)
            return;

        String rawQuery = "SELECT * FROM SPENT WHERE Date>=DATE('"+fDateFrom+"') AND Date<=DATE('"+fDateTill+"')";

        spentData.clear();

        SpentDatabaseHelper databaseHelper = new SpentDatabaseHelper(this);
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        Cursor cursor = database.rawQuery(rawQuery, null);

        if (cursor.moveToFirst()) {

            do {
                SpentModel spentModel = new SpentModel();

                if (cursor.getColumnIndex("_id") >= 0) {
                    spentModel.setId(cursor.getInt(cursor.getColumnIndex("_id")));
                }
                if (cursor.getColumnIndex("Date") >= 0) {
                    spentModel.setDate(cursor.getString(cursor.getColumnIndex("Date")));
                }
                if (cursor.getColumnIndex("Sum") >= 0) {
                    spentModel.setSum(cursor.getString(cursor.getColumnIndex("Sum")));
                }
                if (cursor.getColumnIndex("Category") >= 0) {
                    spentModel.setCategory(cursor.getInt(cursor.getColumnIndex("Category")));
                }
                if (cursor.getColumnIndex("Desc") >= 0) {
                    spentModel.setDesc(cursor.getString(cursor.getColumnIndex("Desc")));
                }
                if (cursor.getColumnIndex("Cash") >= 0) {
                    spentModel.setCash(cursor.getInt(cursor.getColumnIndex("Cash")) != 0);
                }
                if (cursor.getColumnIndex("Card") >= 0) {
                    spentModel.setCard(cursor.getInt(cursor.getColumnIndex("Card")) != 0);
                }

                spentData.add(0, spentModel);
            } while (cursor.moveToNext());
        }

        cursor.close();
        database.close();
        recyclerView.getAdapter().notifyDataSetChanged();
        loadBudgetInfo();
    }


    private void setupRecyclerView() {

        recyclerView = (RecyclerView) findViewById(R.id.spent_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final SpentRecyclerAdapter recyclerAdapter = new SpentRecyclerAdapter(this, spentData);


        RecyclerItemClickListener listener = new RecyclerItemClickListener(recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (position == spentData.size())
                    return;

                LayoutInflater inflater = LayoutInflater.from(recyclerView.getContext());
                View dialogView = inflater.inflate(R.layout.dialog_add_spent_layout, null);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(recyclerView.getContext());
                dialogBuilder.setTitle(R.string.costs);
                dialogBuilder.setView(dialogView);
                final AlertDialog dialog = dialogBuilder.create();

                setupEditSpentDialogView(dialogView, dialog, position);

                dialog.show();
            }
        });

        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addOnItemTouchListener(listener);
        recyclerView.addItemDecoration(new SimpleDividerItem(this));
    }


    private void setupEditSpentDialogView(View dialogView, final AlertDialog dialog, final int position) {

        final SpentModel curModel = spentData.get(position);

        final EditText sum = (EditText) dialogView.findViewById(R.id.sum);
        sum.setText(Helper.getInstance().getSpacedString(curModel.getSum()));
        final EditText desc = (EditText) dialogView.findViewById(R.id.info);
        desc.setText(curModel.getDesc());
        final CheckBox cash = (CheckBox) dialogView.findViewById(R.id.cash);
        cash.setChecked(curModel.isCash());
        final CheckBox card = (CheckBox) dialogView.findViewById(R.id.card);
        card.setChecked(curModel.isCard());
        final LabelledSpinner categorySpinner = (LabelledSpinner) dialogView.findViewById(R.id.category_spinner);
        final ArrayList<Pair<Integer, String>> categories = Helper.getInstance().loadCategoryList(this);

        ArrayList<String> spinnerList = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++)
            spinnerList.add(categories.get(i).second);

        categorySpinner.setItemsArray(spinnerList);
        categorySpinner.setSelection(spinnerList.indexOf(getText(curModel.getCategory()).toString()));

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

        sum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    if (sum.getText().length() == 1 && sum.getText().charAt(0) == '0')
                        sum.setText("");
                } else {
                    if (sum.getText().length() == 0)
                        sum.setText("0");
                }
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

        Button saveSpentBtn = (Button) dialogView.findViewById(R.id.add_spent_btn);
        saveSpentBtn.setText(R.string.save);
        saveSpentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (sum.getText().length() == 0) {
                    sum.setError(getText(R.string.sum_hint).toString());
                } else {

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = dateFormat.format(Calendar.getInstance().getTime());

                    ContentValues values = new ContentValues();
                    values.put("_id", curModel.getId());
                    values.put("Date", date);
                    values.put("Sum", sum.getText().toString().replaceAll(" ", ""));
                    values.put("Category", categories.get(categorySpinner.getSelectedItemPosition()).first);
                    values.put("Desc", desc.getText().toString());
                    values.put("Cash", cash.isChecked());
                    values.put("Card", card.isChecked());

                    SpentDatabaseHelper databaseHelper = new SpentDatabaseHelper(view.getContext());
                    SQLiteDatabase database = databaseHelper.getWritableDatabase();
                    database.insertWithOnConflict("Spent", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    database.close();

                    updateData();

                    dialog.dismiss();
                }

            }
        });

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

}
