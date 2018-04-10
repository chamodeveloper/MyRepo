package com.mytest.myservice;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by indkumar05 on 2/6/2017.
 */

public class NameNumerology extends AppCompatActivity implements View.OnClickListener {

    private AppCompatEditText _editTextName;
    private AppCompatEditText _editTextInitial;
    private Button mButton1;
    private Button mButton2;
    private String mName = null;
    private String mInitial = null;
    private String mFullName = null;
    private String TAG = this.getClass().getSimpleName();
    private boolean isFullName;
    private StringBuilder mTop = new StringBuilder();
    private StringBuilder mBottomRight = new StringBuilder();
    private TextView mTextViewTop;
    private TextView mTextViewBottomLeft;
    private TextView mTextViewBottomRight;
    private final HashMap<Character,Integer> mLookUp = new HashMap<Character, Integer>();
    private int[] initialElements, workArray, myArray;
    //private static int temp = 0;
    private ArrayList<Integer> myList = new ArrayList<Integer>();

    private int superPyramid;
    private Integer mNumOne = 1;
    private Integer mNumTwo = 2;
    private Integer mNumThree = 3;
    private Integer mNumFour = 4;
    private Integer mNumFive = 5;
    private Integer mNumSix = 6;
    private Integer mNumSeven = 7;
    private Integer mNumEight = 8;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG," onCreate ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.name_page);
        _editTextName = (AppCompatEditText) findViewById(R.id.nameText);
        _editTextInitial = (AppCompatEditText) findViewById(R.id.initialText);
        mButton1 = (Button) findViewById(R.id.getNameWithoutInitials);
        mButton2 = (Button) findViewById(R.id.getNameWithInitials);
        mTextViewTop = (TextView) findViewById(R.id.top);
        mTextViewBottomLeft = (TextView) findViewById(R.id.bottomleft);
        mTextViewBottomRight = (TextView) findViewById(R.id.bottomright);

        //Add values to the dictionary
        mLookUp.put('A',mNumOne);
        mLookUp.put('B',mNumTwo);
        mLookUp.put('C',mNumThree);
        mLookUp.put('D',mNumFour);
        mLookUp.put('E',mNumFive);
        mLookUp.put('F',mNumEight);
        mLookUp.put('G',mNumThree);
        mLookUp.put('H',mNumFive);
        mLookUp.put('I',mNumOne);
        mLookUp.put('J',mNumOne);
        mLookUp.put('K',mNumTwo);
        mLookUp.put('L',mNumThree);
        mLookUp.put('M',mNumFour);
        mLookUp.put('N',mNumFive);
        mLookUp.put('O',mNumSeven);
        mLookUp.put('P',mNumEight);
        mLookUp.put('Q',mNumOne);
        mLookUp.put('R',mNumTwo);
        mLookUp.put('S',mNumThree);
        mLookUp.put('T',mNumFour);
        mLookUp.put('U',mNumSix);
        mLookUp.put('V',mNumSix);
        mLookUp.put('W',mNumSix);
        mLookUp.put('X',mNumFive);
        mLookUp.put('Y',mNumOne);
        mLookUp.put('Z',mNumSeven);


        //setlisteners for Buttons and edittext
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        _editTextName.setOnClickListener(this);
        _editTextInitial.setOnClickListener(this);

    }

    @Override
    public void onStart(){
        Log.d(TAG," onStart() ");
        super.onStart();

    }

    @Override
    public void onResume(){
        Log.d(TAG," onResume() ");
        super.onResume();
    }

    @Override
    public void onRestart(){
        Log.d(TAG," onRestart() ");
        super.onRestart();
    }

    @Override
    public void onPause(){
        Log.d(TAG," onPause() ");
        super.onPause();
    }

    @Override
    public void onStop(){
        Log.d(TAG," onStop() ");
        super.onStop();
    }

    @Override
    public void onDestroy(){
        Log.d(TAG, " onDestroy() ");
        super.onDestroy();
    }

    @Override
    public void onBackPressed(){
        Log.d(TAG," onBackPressed() ");
        super.onBackPressed();
    }


    @Override
    public void onClick(View view) {

        System.out.println("onClick called");

        /*switch(view.getId()){

            case R.id.getNameWithoutInitials:
                mName = _editTextName.getText().toString().replaceAll(" ","");
                Log.d(TAG,"getNameWithoutInitials the name is " + mName);
                if(mName!= null) {
                    isFullName = false;
                    calcBottomLeftPyramid(mName);
                    calcTopPyramid(mName);
                    calcBottomRightPyramid(mName);
                }
                else{
                    Toast.makeText(getApplicationContext()," Please enter a valid name ",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.getNameWithInitials:

                mName = _editTextName.getText().toString().replaceAll(" ","");
                mInitial = _editTextInitial.getText().toString();
                Log.d(TAG," getNameWithInitials the name and initial is " + mName + " "+ mInitial);
                if(mInitial != null && mName != null) {
                    isFullName = true;
                    calcBottomLeftPyramid(mName, mInitial);
                    calcTopPyramid(mName,mInitial);
                    calcBottomRightPyramid(mName,mInitial);
                }
                else{
                    Toast.makeText(getApplicationContext()," Please enter a valid name and initial ",Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.nameText:
                if(_editTextName.getText() == null) {
                    _editTextName.setText("");
                }
                _editTextName.setFilters(new InputFilter[] {new InputFilter.AllCaps()});


            case R.id.initialText:
                if(_editTextInitial.getText() == null) {
                    _editTextInitial.setText("");
                }
                _editTextInitial.setFilters(new InputFilter[] {new InputFilter.AllCaps()});

        }*/

    }

    public void calcBottomLeftPyramid(String mName){
        int sum = 0;
        for(char ch : mName.toCharArray()){
            sum += mLookUp.get(ch);
        }
        Log.d(TAG," calcBottomLeftPyramid value : " + sum);
        mTextViewBottomLeft.setTextSize(35);
        mTextViewBottomLeft.setText(Integer.toString(sum));
        mTextViewBottomLeft.setTextColor(Color.CYAN);
    }

    public void calcBottomLeftPyramid(String mName, String mInitial){
        mFullName = getNameWithInitial(mName,mInitial);
        int sum = 0;
        for(char ch : mFullName.toCharArray()){
            sum += mLookUp.get(ch);
        }
        Log.d(TAG," calcBottomLeftPyramid value : " + sum);
        mTextViewBottomLeft.setTextSize(35);
        mTextViewBottomLeft.setText(Integer.toString(sum));
        mTextViewBottomLeft.setTextColor(Color.CYAN);
    }

    private void calcTopPyramid(String mName){
        if(mName != null) {
            myArray = addElementsToArray(mName);
            addIndividualIteration(myArray);
        }
        if(myArray.length != 0) {
            startCalc(myArray);
        }
        mTextViewTop.setText(mTop);
        mTextViewTop.setTextSize(35);
        mTextViewTop.setTextColor(Color.CYAN);
        mTextViewTop.setTextColor(Color.CYAN);
        mTextViewBottomRight.setText(mTop);
        mTextViewBottomRight.setTextSize(35);
        mTextViewBottomRight.setTextColor(Color.CYAN);
        mTop.setLength(0);
    }

    private void calcTopPyramid(String mName, String mInitial){
        mFullName = getNameWithInitial(mName,mInitial);
        if(mFullName != null) {
            myArray = addElementsToArray(mFullName);
            addIndividualIteration(myArray);
        }
        if(myArray.length != 0) {
            startCalc(myArray);
        }
        mTextViewTop.setText(mTop);
        mTextViewTop.setTextSize(35);
        mTextViewTop.setTextColor(Color.CYAN);
        mTop.setLength(0);
    }

    private void calcBottomRightPyramid(String mName, String mInitial){

       mFullName = getNameWithInitial(mName,mInitial);
        if(isFullName) {
            updateBottomRight();
        }
    }

    private void calcBottomRightPyramid(String mName){

        if(mName != null) {
            updateBottomRight();
        }
    }



    private String getNameWithInitial(String mName, String mInitial){
        if(mName != null && mInitial != null) {
            return mInitial.concat(mName);
        }
        return null;
    }

    /* The below method is to add the initial values to the
    to the array. This is required because we need to add the
    individual iteration values for the bottom right pyramid
    This array should be unaltered.
     */
    private int[] addElementsToArray(String name){

        initialElements = new int[name.length()];
        for(int i = 0; i < name.length(); i++){
            initialElements[i] = mLookUp.get(name.charAt(i));
            Log.d(TAG," addElementsToArray initial Elements : " + initialElements[i]);
        }

        return initialElements;

    }

    /* startCalc() holds the core logic to add the values for
    the top pyramid value.We add the consecutive elements of the array here
    until we get a double digit value. This method will be called
    for both name and name with initials
     */
    private void startCalc(int[] myArray){
        workArray = new int[myArray.length-1];
        int sum = 0;
        System.out.println();
        for (int j = 0; j < myArray.length-1; j++) {
            sum = myArray[j] + myArray[j + 1];
            Log.d(TAG," The sum values are : " + sum);
            workArray[j] = sum;
            for (int num : workArray) {
                if (String.valueOf(num).length() > 1) {
                    int tempo = 0;
                    while (num > 0) {
                        tempo = tempo + num % 10;
                        num = num / 10;
                    }
                    workArray[j] = tempo;

                }
            }
        }
        //We need to ensure this flag is set for calling namewithInitials
        //if(isFullName) {
            addIndividualIteration(workArray);
        //}
        arrayLengthCheck(workArray);
    }



    public void arrayLengthCheck(int[] testArray){
        if(testArray.length > 2){
            startCalc(testArray);
        }
        else{
            for(int elements : testArray){
                mTop.append(elements);
            }
            //return;
        }
    }

    public void  addIndividualIteration(int[] addArray) {
        int[] addArray1 = new int[addArray.length];
        for (int i = 0; i < addArray.length; i++) {
            addArray1[i] = addArray[i];
            Log.d(TAG," addIndividualIteration() values added to new array : " + addArray1[i]);
        }
        System.out.println();

        int sum = 0;
        for (int j = 0; j < addArray1.length; j++) {
            sum += addArray1[j];
        }

        Log.d(TAG,"addIndividualIteration sum value : " + sum);
        int values = 0;
        if(String.valueOf(sum).length() > 1){
            values = addElements(sum);
            System.out.print(" Final value " + values);
            myList.add(values);
        }
        else{
            updateFinalList(sum);
        }


    }

    private void updateFinalList(int value){

        //System.out.println(" Value received in updateFinalList " + value);
        /*if(String.valueOf(value).length() > 1){
            addElements(value);
        }*/
        //else{
            Log.d(TAG," updateFinalList() values received : " + value);
            myList.add(value);
        //}

    }



    private int addElements(int sum) {

        int temp = 0;

        while (sum > 0) {
            temp += sum % 10;
            sum = sum / 10;
        }
        if(String.valueOf(temp).length() > 1){
            temp = addElements(temp);
        }

        return temp;

        //updateFinalList(temp);
    }



    private void updateBottomRight(){

        int sum = 0;
        for(int elements : myList){
            Log.d(TAG," updateBottomRight() values in list : " + elements);
            sum += elements;
        }
        Log.d(TAG," updateBottomRight() value : " + sum);
        myList.clear();
        mTextViewBottomRight.setText(String.valueOf(sum));
        mTextViewBottomRight.setTextSize(35);
        mTextViewBottomRight.setTextColor(Color.CYAN);
    }
}
