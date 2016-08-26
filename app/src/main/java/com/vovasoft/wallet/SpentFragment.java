package com.vovasoft.wallet;

import android.app.Activity;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.vovasoft.wallet.Database.SpentDatabaseHelper;
import com.vovasoft.wallet.Database.SpentModel;
import com.vovasoft.wallet.Widgets.LabelledSpinner;
import com.vovasoft.wallet.Widgets.RecyclerItemClickListener;
import com.vovasoft.wallet.Widgets.SimpleDividerItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by arsen on 28.03.2016.
 */
public class SpentFragment extends Fragment {

    public static final String TYPE = "type";
    public static final String CASH = "cash";
    public static final String CARD = "card";
    public static final String ALL = "all";

    View rootView;
    private String type;

    private TextView spentInfo;
    private TextView availableInfo;

    private RecyclerView recyclerView;
    private ArrayList<SpentModel> spentData;

    private SharedPreferences prefs;

    public static SpentFragment newInstance(String type) {
        SpentFragment myFragment = new SpentFragment();

        Bundle args = new Bundle();
        args.putString(TYPE, type);
        myFragment.setArguments(args);

        return myFragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_spent, container, false);
        }
        type = getArguments().getString(TYPE);

        spentData = new ArrayList<>();
        prefs = getContext().getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);

        setupBudgetInfo();
        setupRecyclerView();
        updateData();

        return rootView;
    }


    private void saveSpents() {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(Calendar.getInstance().getTime());

        String cashQuery = "SELECT sum(Sum) as sum FROM SPENT WHERE Cash=1 AND Date=DATE('" + date + "')";
        String cardQuery = "SELECT sum(Sum) as sum FROM SPENT WHERE Card=1 AND Date=DATE('" + date + "')";

        String cashSpent = "0";
        String cardSpent = "0";

        SpentDatabaseHelper databaseHelper = new SpentDatabaseHelper(getContext());
        SQLiteDatabase database = databaseHelper.getReadableDatabase();

        Cursor cashCursor = database.rawQuery(cashQuery, null);
        if (cashCursor.moveToFirst()) {
            do {
                if (cashCursor.getColumnIndex("sum") >= 0) {
                    cashSpent = cashCursor.getString(cashCursor.getColumnIndex("sum"));
                }
            } while (cashCursor.moveToNext());
        }
        cashCursor.close();

        Cursor cardCursor = database.rawQuery(cardQuery, null);
        if (cardCursor.moveToFirst()) {
            do {
                if (cardCursor.getColumnIndex("sum") >= 0) {
                    cardSpent = cardCursor.getString(cardCursor.getColumnIndex("sum"));
                }
            } while (cardCursor.moveToNext());
        }
        cardCursor.close();

        database.close();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(SettingsActivity.CASH_SPENT, cashSpent);
        editor.putString(SettingsActivity.CARD_SPENT, cardSpent);
        editor.apply();
    }


    private void setupBudgetInfo() {
        spentInfo = (TextView) rootView.findViewById(R.id.info_spent);
        availableInfo = (TextView) rootView.findViewById(R.id.info_available);
    }


    private void updateBudgetInfo() {

        long budget;

        switch (type) {
            case CASH:
                budget = Long.valueOf(prefs.getString(SettingsActivity.CASH, "0").replaceAll(" ", ""));
                break;
            case CARD:
                budget = Long.valueOf(prefs.getString(SettingsActivity.CARD, "0").replaceAll(" ", ""));
                break;
            default:
                budget = Long.valueOf(prefs.getString(SettingsActivity.CASH, "0").replaceAll(" ", "")) +
                        Long.valueOf(prefs.getString(SettingsActivity.CARD, "0").replaceAll(" ", ""));
                break;
        }

        int days = prefs.getInt(SettingsActivity.DAYS, 0);

        budget = days > 0 ? budget / days : 0;

        long spent = 0;
        for (SpentModel model : spentData) {
            spent += Integer.parseInt(model.getSum());
        }

        long available = budget - spent;

        spentInfo.setText(Helper.getInstance().getSpacedString(String.format("%d", spent)));
        availableInfo.setText(Helper.getInstance().getSpacedString(String.format("%d", available)));

        setMinusColor(available <= 0);
    }


    protected void setMinusColor(boolean isMinus) {

        TextView labelAvailable = (TextView) rootView.findViewById(R.id.label_info_available);

        if (availableInfo == null) {
            availableInfo = (TextView) rootView.findViewById(R.id.info_available);
        }

        if (isMinus) {
            availableInfo.setTextColor(getResources().getColor(R.color.colorAccent));
            labelAvailable.setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            availableInfo.setTextColor(getResources().getColor(R.color.colorAppText));
            labelAvailable.setTextColor(getResources().getColor(R.color.colorAppText));
        }
    }


    public void updateData() {

        if (type == null)
            return;

        String rawQuery = null;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(Calendar.getInstance().getTime());

        switch (type) {
            case ALL:
                rawQuery = "SELECT * FROM SPENT WHERE Date=DATE('" + date + "')";
                break;
            case CASH:
                rawQuery = "SELECT * FROM SPENT WHERE Cash=1 AND Date=DATE('" + date + "')";
                break;
            case CARD:
                rawQuery = "SELECT * FROM SPENT WHERE Card=1 AND Date=DATE('" + date + "')";
                break;
        }

        spentData.clear();

        SpentDatabaseHelper databaseHelper = new SpentDatabaseHelper(getContext());
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
        updateBudgetInfo();
        saveSpents();
    }


    private void setupRecyclerView() {

        recyclerView = (RecyclerView) rootView.findViewById(R.id.spent_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        final SpentRecyclerAdapter recyclerAdapter = new SpentRecyclerAdapter(getContext(), spentData);

        RecyclerItemClickListener listener = new RecyclerItemClickListener(recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (position == spentData.size())
                    return;

                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.dialog_add_spent_layout, null);
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
                dialogBuilder.setTitle(R.string.costs);
                dialogBuilder.setView(dialogView);
                final AlertDialog dialog = dialogBuilder.create();

                setupEditSpentDialogView(dialogView, dialog, position);

                dialog.show();
            }
        });

        recyclerView.setAdapter(recyclerAdapter);
        recyclerView.addOnItemTouchListener(listener);
        recyclerView.addItemDecoration(new SimpleDividerItem(getContext()));
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
        final ArrayList<Pair<Integer, String>> categories = Helper.getInstance().loadCategoryList(getContext());

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

                    SpentDatabaseHelper databaseHelper = new SpentDatabaseHelper(getContext());
                    SQLiteDatabase database = databaseHelper.getWritableDatabase();
                    database.insertWithOnConflict("Spent", null, values, SQLiteDatabase.CONFLICT_REPLACE);
                    database.close();

                    updateData();

                    dialog.dismiss();
                }

            }
        });

    }

}
