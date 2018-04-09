package com.mytest.myservice;


import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.support.v7.app.ActionBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by indkumar05 on 2/6/2017.
 */

public class DOBNumerology extends AppCompatActivity implements View.OnClickListener,DatePickerDialog.OnDateSetListener {

    private static DOBNumerology myDOBInstance = null;

    private EditText _editText = null;
    private int[] mArray1,mArray2;
    private int mLength = 0;
    private int[] finalArray;
    SimpleDateFormat _dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private ActionBar _actionBar;
    private String mDateString;
    private String TAG = "DOBNumerology";
    private TextView mTextViewTop;
    private TextView mTextViewBottomLeft;
    private TextView mTextViewBottomRight;
    private Button mButton;
    Calendar _calendar = Calendar.getInstance();
    private int mYear;
    private int mDay;
    private int mMonth;
    private String _currentDate = _dateFormat.format(_calendar.getTime());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dob_page);
        _editText = (EditText) findViewById(R.id.editBox);
        _actionBar = getSupportActionBar();
        _actionBar.setLogo(R.drawable.home);
        _actionBar.setDisplayUseLogoEnabled(true);
        _actionBar.setDisplayShowHomeEnabled(true);
        _editText.setText(_currentDate);
        mYear = _calendar.get(Calendar.YEAR);
        mMonth = _calendar.get(Calendar.MONTH);
        mDay = _calendar.get(Calendar.DAY_OF_YEAR);
        mButton = (Button) findViewById(R.id.getNumber);
        mTextViewTop = (TextView) findViewById(R.id.top);
        mTextViewBottomLeft = (TextView) findViewById(R.id.bottomleft);
        mTextViewBottomRight = (TextView) findViewById(R.id.bottomright);

        //Add Listeners
        _editText.setOnClickListener(this);
        mButton.setOnClickListener(this);
    }


    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onRestart(){
        super.onRestart();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();


    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
    }


    public static DOBNumerology getDOBInstance(){
        myDOBInstance = new DOBNumerology();
        return myDOBInstance ;
    }

    public String getCurrentDateToString(){

     StringBuilder builder = new StringBuilder();
        builder.append(mDay + "/");
        builder.append(mMonth + 1 + "/");
        builder.append(mYear);
        return builder.toString();
    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        _editText.setText(getCurrentDateToString());
    }

    @Override
    public void onClick(View view) {
        System.out.println("onClick called");

       /* switch(view.getId()){

            case R.id.editBox:
                DatePickerDialog mDatePickerDialog = new DatePickerDialog(DOBNumerology.this,android.R.style.Theme_Holo_Dialog,this,_calendar.get(Calendar.YEAR),_calendar.get(Calendar.MONTH),_calendar.get(Calendar.DAY_OF_MONTH));
                mDatePickerDialog.getDatePicker().setCalendarViewShown(false);
                mDatePickerDialog.getDatePicker().setSpinnersShown(true);
                mDatePickerDialog.show();
                break;

            case R.id.getNumber:
                mDateString =  _editText.getText().toString();
                finalArray = storeInArray();
                calcPyramid();
                calcLinearPyramid();
                mTextViewBottomLeft.setText(Integer.toString(mDay));
                mTextViewBottomLeft.setTextSize(35);
                mTextViewBottomLeft.setTextColor(Color.YELLOW);
                break;
        }*/
    }

    public void calcPyramid(){

        Log.d(TAG," Birthdate to be predicted : " + mDateString);
        System.out.println(" The date string is " + mDateString);
        System.out.println(" The length of date is  " + mDateString.length());
        int temp = 0;
        mArray2 = new int[mArray1.length-1];
        for (int j = 0; j < mArray1.length - 1; j++) {
            temp = mArray1[j] + mArray1[j + 1];
            mArray2[j] = temp;
            for (int num : mArray2) {
                if (String.valueOf(num).length() > 1) {
                    int sum = 0;
                    while (num > 0) {
                        sum = sum + num % 10;
                        num = num / 10;
                    }
                    mArray2[j] = sum;
                }
            }
        }
        System.out.println(" Inside startAdd " + Arrays.toString(mArray1));
        replaceArray();
    }

    public void replaceArray() {
        mArray1 = mArray2;
        mArray2 = null;
        int myLength = mArray1.length;
        System.out.println(" The length of the modified array is " + myLength);

        if(myLength > 2){
            calcPyramid();
        }

        else{
           // Toast.makeText(getApplicationContext(),Arrays.toString(mArray1),Toast.LENGTH_LONG).show();
            setPyramidValue(mArray1);
            System.out.println("return");
        }
    }

    public int[] storeInArray(){

        mDateString = mDateString.replaceAll("/","");
        mArray1 = new int[mDateString.length()];
        for(int i = 0;i < mDateString.length();i++){
               mArray1[i] = Character.getNumericValue(mDateString.charAt(i));
        }

        return mArray1;
    }

    public void setPyramidValue(int[] array){

        StringBuilder stringText = new StringBuilder();

        for(int elements : array){
            stringText.append(elements);
        }
        mTextViewTop.setText(stringText);
        mTextViewTop.setTextSize(35);
        mTextViewTop.setTextColor(Color.YELLOW);
    }

    public void calcLinearPyramid(){
        int temp = 0;
        for(int i = 0;i < finalArray.length;i++){
            temp += finalArray[i];
        }
        mTextViewBottomRight.setText(Integer.toString(temp));
        mTextViewBottomRight.setTextSize(35);
        mTextViewBottomRight.setTextColor(Color.YELLOW);
    }
}

