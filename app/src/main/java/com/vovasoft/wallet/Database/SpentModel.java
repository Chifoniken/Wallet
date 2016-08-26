package com.vovasoft.wallet.Database;

/**
 * Created by arsen on 02.03.2016.
 */
public class SpentModel {

    private int id;
    private String date;
    private String sum;
    private int category;
    private String desc;
    private boolean cash;
    private boolean card;


    public SpentModel() {
        id = -1;
        cash = true;
        card = false;
    }

    public void setId(int v) {
        id = v;
    }


    public void setDate(String str) {
        date = str;
    }


    public void setSum(String str) {
        sum = str.length() > 0 ? str : "0";
    }


    public void setCategory(int i) {
        category = i;
    }


    public void setDesc(String str) {
        desc = str;
    }


    public void setCash(boolean b) {
        cash = b;
        card = !b;
    }


    public void setCard(boolean b) {
        card = b;
        cash = !b;
    }


    public int getId() {
        return id;
    }


    public String getDate() {
        return date;
    }


    public String getSum() {
        sum = sum.length() > 0 ? sum : "0";
        return sum;
    }


    public int getCategory() {
        return category;
    }


    public String getDesc() {
        return desc;
    }


    public boolean isCash() {
        return cash;
    }


    public boolean isCard() {
        return card;
    }

}
