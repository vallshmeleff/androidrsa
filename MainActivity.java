package com.example.rsaeaescrypto;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.security.Key;


//=====================================================
//
// This example shows how easy it is to generate PCA key pairs,
// encrypt text, write text, read text from a file, decode text,
// store encryption keys, and recover encryption keys. If you have
// a special class RSACode.java
// Supports national alphabets (as an example - Czech)
//
//=====================================================


public class MainActivity extends AppCompatActivity {
    //=====================================================
    //
    // RSA and Write encoded text to file and Read from file oflameron.txt
    // rsaload.Load(FILENAME, str2)
    //
    //=====================================================
    final static String LOG_TAG = "myLogs";
    public static String str=" "; //File contents oflameron.txt
    public static String str2=" "; //File contents oflameron.txt
    public static String str3=" "; //File contents key.txt - public key
    public static String str4=" "; //File contents pkey.txt - private key
    public static String FILENAME = "oflameron.txt";//File for writing encoded data
    public static String Content = "EditText Content";//String variable for text copy

    public static Key publicKey = null; //RSA
    public static Key privateKey = null; //RSA
    public static Key publicKey2 = null; //RSA
    public static Key privateKey2 = null; //RSA
    public static Key kpprivateKey = null; //RSA restored Private Key
    public static Key kppublicKey = null; //RSA restored Public Key
    public static byte[] privateKeyBytes = null; //RSA
    public static byte[] publicKeyBytes = null; //RSA
    public static byte[] encodedBytes = null; //RSA
    public static byte[] decodedBytes = null; //RSA
    // Original text (RSA)
    public static String testText = "Open Source Java Project Valery Shmelev OFLAMERON. Česká Republika";
    public static Context Maincontext;
    public ClipboardManager clipboard;


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Maincontext = getApplicationContext(); //To work with context

        // ============================================================
        // Write to clipboard for export/import
        // ============================================================
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip;
        String text = "Text Example for ClipBoard";
        myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
        // ============================================================

        Log.d(LOG_TAG, "== == Activity NAME == ==" + this.getClass().getSimpleName()); //The name of the current Activity - to access the context

        //TextView originalTextView = (TextView) findViewById(R.id.TXTV);
        EditText originalEditText = (EditText) findViewById(R.id.TXTV);
        originalEditText.setMovementMethod(new ScrollingMovementMethod()); //Scrolling text + android:scrollbars = "vertical" in Activity_main.xml
        originalEditText.setText("[ORIGINAL]:\n" + testText + "\n");

        // ============================================================
        // Generate key pair for 1024-bit RSA encryption and decryption
        // ============================================================
        RSACode rsagente = new RSACode(); // Class instance RSACode
        Key[] KeyPMass = new Key[2]; //An array of two keys to return values from a method
        KeyPMass = rsagente.RSAKeyGen(); //GENERATE Key Pair
        publicKey = KeyPMass[0];
        privateKey = KeyPMass[1];
        // ============================================================


        // Convert Text to CharCodeString
        String cyrtxt = rsagente.String2Code(testText);
        Log.d(LOG_TAG, "== ==| Text to CharachterString |== ==" + cyrtxt);
        // Convert CharCodeString to Text
        String txtcyr = rsagente.Code2String(cyrtxt);
        Log.d(LOG_TAG, "== ==| UNICODE CharachterString to TEXT|== ==" + txtcyr);


        // ============================================================
        // Encode the original text with RSA private key
        // ============================================================
        encodedBytes = rsagente.RSATextEncode(publicKey, privateKey, testText); //Encode text via RSACode.java class

        EditText encodedEditText = (EditText)findViewById(R.id.EditTextEncoded);
        encodedEditText.setMovementMethod(new ScrollingMovementMethod()); //Scrolling text + android:scrollbars = "vertical" in Activity_main.xml
        encodedEditText.setText("[ENCODED]:\n" + Base64.encodeToString(encodedBytes, Base64.DEFAULT) + "\n");

        //--------------------------------------------------------
        // Coded Text -> str -> Save to file
        //--------------------------------------------------------
        str =  Base64.encodeToString(encodedBytes, Base64.DEFAULT); //Convert Byte Array to String
        rsagente.Save("oflameron.txt",str, Maincontext);  //Write Coded Text to file oflameron.txt  from   str
        encodedBytes = null; // This line is optional. For debugging only

        //--------------------------------------------------------
        // Load Coded Text from file -> str2
        //--------------------------------------------------------
        //////RSACode rsalib = new RSACode(); // Class instance RSALib
        str2 = rsagente.Load(FILENAME, str2, Maincontext); //Here we have erased the ciphertext from the variable encodedBytes. We have already written it to the file oflameron.txt in the module Save();. Now read and decode.
        Log.d(LOG_TAG, "== == RSACode.RSA Readed Coded text == ==" + str2);
        encodedBytes = Base64.decode(str2, Base64.DEFAULT); //Convert String to Byte Array
        //--------------------------------------------------------

        //--------------------------------------------------------
        // The most important part of encryption/decoding is saving
        // and restoring the public and private keys. Otherwise, after
        // restarting the application, you will not be able to decrypt
        // the encoded text, because new keys will be generated.
        //
        // Save Keys -> to file
        //--------------------------------------------------------
        publicKeyBytes = publicKey.getEncoded();  //Записать в массив байт publicKey, закодированный в X.509
        privateKeyBytes = privateKey.getEncoded();  //Записать в массив байт privateKey, закодированный в PKCS#8

        str =  Base64.encodeToString(publicKeyBytes, Base64.DEFAULT); //Convert Byte Array (Public Key) to String
        rsagente.Save("key.pub",str,  Maincontext);  //Write Public Key to file key.txt  from   str
        str =  Base64.encodeToString(privateKeyBytes, Base64.DEFAULT); //Convert Byte Array (Private Key) to String
        rsagente.Save("pkey.pri",str,  Maincontext);  //Write Private Key to file pkey.txt  from   str

        Log.d(LOG_TAG, "== == RSA Write Public Key to key.txt == ==" + str);
        //encodedBytes = Base64.decode(str, Base64.DEFAULT);
        publicKey = null; // This line is optional. For debugging only
        privateKey = null; // This line is optional. For debugging only

        str3 = rsagente.Load("key.pub", str3,  Maincontext); //Here we read and decode Public Key (RSACode class)
        str4 = rsagente.Load("pkey.pri", str4,  Maincontext); //Here we read and decode Private Key (RSACode class)

        //--------------------------------------------------------
        // Referring to the special class RSACode.java
        // To restore saved keys from files
        //--------------------------------------------------------
        Key[] KeyMass = new Key[2]; //An array of two keys to return values from a method
        KeyMass = rsagente.RSAKeyReGenerate(str3, str4); // We pass to the method str3 and str4 - String from the file. Get the recovered keys as an array
        publicKey = KeyMass[0];
        privateKey = KeyMass[1];
        Log.d(LOG_TAG, "== == RSACode.RSAKeyReGenerate.RSA Publickey and Privatekey Full RESTORED == ==" + publicKey + " "+ privateKey);

        ClipBrdWrite(privateKey.toString()); //Write privateKey to ClipBoard
        //--------------------------------------------------------

        // If you run the application, you will see that the original text is correctly decoded.
        // Those. we run the application and immediately encode the text and immediately decode it. Everything is working.

        // ============================================================
        // Decoding the ciphertext
        // ============================================================

        // Let's call a method from the class RSACode.java
        decodedBytes = rsagente.RSATextDecode(KeyMass[0], KeyMass[1], encodedBytes); //Text decoding (publicKey = KeyMass[0], privateKey = KeyMass[1])
        EditText decodedEditText = (EditText)findViewById(R.id.EditTextDecoded);
        decodedEditText.setMovementMethod(new ScrollingMovementMethod()); //Scrolling text + android:scrollbars = "vertical" in Activity_main.xml
        decodedEditText.setText("[DECODED]:\n" + new String(decodedBytes) + "\n"); //Show decoded text

        Button btncpy = (Button)findViewById(R.id.button1);
    } //OnCreate


    //==========================================================
    // Read text (RSA Key) from Android Clipboard
    //==========================================================
    public void ReadClipBrd() {
        String text = "Primer RSA Key"; //RSA Key Text from clipboard
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData primaryClipData = clipboard.getPrimaryClip();
        ClipData.Item item = primaryClipData.getItemAt(0);
        text = item.getText().toString();
        TextView decodedTextView = (TextView)findViewById(R.id.EditTextDecoded);
        decodedTextView.setText("== RSA Key == " + text);

        //return text; //Return RSA Key Text
    }

    public void onClickR(View v) { //For Button
        ReadClipBrd(); // Read RSA Key from ClipBoard
    }
    //==========================================================


    //==========================================================
    // Write text to the Android Clipboard
    //==========================================================
    public void ClipBrdWrite(String text) {
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip;
        myClip = ClipData.newPlainText("text", text);
        myClipboard.setPrimaryClip(myClip);
    }

    public void onClickW(View v) { //For Button
        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData myClip;
        myClip = ClipData.newPlainText("text", Content); // public static String
        myClipboard.setPrimaryClip(myClip);
    }
    //==========================================================
    // Crypto RSA (c) by Valery Shmelev https://www.linkedin.com/in/valery-shmelev-479206227/
    // GNU GPL. Freelance Programmer

}