package com.example.sqlitersa;
import java.security.SecureRandom;
import android.app.Application;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

//==================================================
//
// RSA Code Class - Generate, Save, Load, Restore Keys, Obfuscation, UNICODE
// RSAeAESCrypto APP RSACode Class
//
// API 29 Tested
//
//==================================================
//
// RSACode class
//
// 16.11.2022 Added method for simple obfuscation - ObfuscationD
//            Method for fragmenting large texts (for SQLite) - eFragment
//            Method for DEfragmenting large texts (for SQLite) - eDEFragment
//
//==================================================
public class RSACode  extends Application {
    public static String PubKey=""; // String from file - public key
    public static String PrivateKey=""; // String from file - private key
    final static String LOG_TAG = "myLogs";
    KeyPair kp;  // Key Pair
    public static Context context;

    int dnaCode;

    public boolean equals(RSACode man) { 
        return this.dnaCode ==  man.dnaCode;
    }

    public static void main() { // https://javarush.ru/groups/posts/equals-java-sravnenie-strok

        RSACode man1 = new RSACode();
        man1.dnaCode = 1111222233;

        RSACode man2 = new RSACode();
        man2.dnaCode = 1111222233;

        //// System.out.println(man1.equals(man2));
        Log.d(LOG_TAG, "== == ECUALS == == " + (man1.equals(man2)));


    }

    public void onCreate(){
        super.onCreate();
        context = MainActivity.Maincontext;
        Log.e("CONTEXT", "== == == == CONTEXT == == == ==" + context.toString());

    }

    public static Context getAppContext() { //Для работы с контекстом
        return RSACode.context;
    }

    //==========================================================
    // GENERATE Key Pair
    //==========================================================
    public Key[] RSAKeyGen() {
        Key[] KeyMass = new Key[2]; // An array of two keys to return values from a method
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(1024); // Key SIZE
            KeyPair kp = kpg.genKeyPair();
            KeyMass[0] = kp.getPublic(); //publicKey
            KeyMass[1] = kp.getPrivate(); //privateKey

        } catch (Exception e) {
            Log.e("Crypto", "== == RSAKeyGen.RSA key pair error == ==");
        }

        return KeyMass; //Returning an array of two keys Public and Private KeyMass[0] = publicKey and KeyMass[1] = privateKey

    }
    //==========================================================


    //==========================================================
    // ReGenerate Public and Private Keys
    //==========================================================
    public Key[] RSAKeyReGenerate(String publicstr, String prvatestr) {
        // Recovering Public Key and Private Key RSA from Text Variable
        byte[] publicKeyBytes = Base64.decode(publicstr, Base64.DEFAULT); //Convert Public Key String to Byte Array
        byte[] privateKeyBytes = Base64.decode(prvatestr, Base64.DEFAULT); //Convert Private Key String to Byte Array
        String ReturnCode = "Restore Keys OK";//String RSAKeyReGenerate Code
        Key[] KeyMass = new Key[2]; //An array of two keys to return values from a method

        // Generate Restored Public Key
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            Key privateKey = keyFactory.generatePrivate(privateKeySpec);
            Key publicKey = keyFactory.generatePublic(publicKeySpec);
            KeyMass[0] = publicKey;
            KeyMass[1] = privateKey;
            Log.d(LOG_TAG, "== == RSA Publik and Privatekey Keys Full RESTORED == == " + KeyMass[0] + " " + KeyMass[1]);
            ReturnCode = "RSAKeyReGenerate.RSA Key pair Full RESTORED";
        } catch (Exception e) {
            Log.e("Crypto", "== == RSA key pair error == ==");
            ReturnCode = "RSAKeyReGenerate.RSA key pair error";
        }

        return KeyMass; //Returning an array of two keys Public and Private KeyMass[0] = publicKey and KeyMass[1] = privateKey
    }
    //==========================================================


    //==========================================================
    // Encrypt Text
    //==========================================================
    public byte[] RSATextEncode(Key publicKey, Key privateKey, String PlainText) {
        byte[] encodedBytes = null; //RSA
        try {
            Cipher c = Cipher.getInstance("RSA");
            c.init(Cipher.ENCRYPT_MODE, publicKey);
            ///////////c.init(Cipher.ENCRYPT_MODE, privateKey);
            encodedBytes = c.doFinal(PlainText.getBytes());
        } catch (Exception e) {
            Log.e("Crypto", "RSA encryption error");
        }
        return encodedBytes; //Return coded Text (bytes array)

    }
    //==========================================================


    //==========================================================
    // Decode encrypted Text
    //==========================================================
    public byte[] RSATextDecode(Key publicKey, Key privateKey, byte[] encodedBytes) {
        // String DecodedText = "";//String Decoded Text
        byte[] decodedBytes = null; //RSA

        // Decode the encoded data with RSA public key
        try {
            Cipher c = Cipher.getInstance("RSA");
            Log.d("== == SQLite","==================1================");

            c.init(Cipher.DECRYPT_MODE, privateKey);
            //////////////c.init(Cipher.DECRYPT_MODE, publicKey);
            Log.d("== == SQLite","==================2================");
            decodedBytes = c.doFinal(encodedBytes);
            Log.d("== == SQLite","==================3================");
        } catch (Exception e) {
            Log.e("Crypto", "RSATextDecode.PUBLIC Key " + publicKey.toString());
            Log.e("Crypto", "RSATextDecode.RSACode RSA decryption error");
        }

        return decodedBytes; //Return Decoded Text (bytes array)
    }



    //==========================================================
    //   READ Text file
    //==========================================================
    public String Load(String fNAME, String sSTR2, Context  Maincontext)  {
        String lstr="";

        Log.d(LOG_TAG, "== == RSA Load FILENAME == ==" + fNAME);
        try {//Read data file - String fNAME
            // Opening a stream for reading
            BufferedReader br = new BufferedReader(new InputStreamReader( Maincontext.openFileInput(fNAME)));

            while ((lstr = br.readLine()) != null) {
                sSTR2 = sSTR2 + lstr; //Save read data
                // Crypto RSA (c) by Valery Shmelev https://www.linkedin.com/in/valery-shmelev-479206227/
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sSTR2;
    }
    //==========================================================


    //==========================================================
    // WRITE data to the Text file  MODE_PRIVATE
    //==========================================================
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void Save(String fNAME, String sSTR, Context Maincontext) {

        try {
            // The data is encoded. We tear off the stream for writing data to the file oflameron.txt
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    Maincontext.openFileOutput(fNAME, MODE_PRIVATE)));
            // We write data to the file String fNAME in the isolated memory area of this application
            // A file on a smartphone in the Device File Explorer folder /data/data/com.example.cryptonote/files/fname
            bw.write(sSTR); //Text to write to the oflameron.txt file from a variable str from EText
            // Closing the stream
            Log.d(LOG_TAG, "== == File Save == ==");
            bw.flush();
            bw.close();

        } catch (FileNotFoundException e) {
            Log.d(LOG_TAG, "== == RSACode.Err NO WRITE File == ==");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(LOG_TAG, "== == RSACode.Err NO WRITE File == ==");
            e.printStackTrace();
        }
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
    //==========================================================


    //==========================================================
    // Lib Info. Return Information about Library
    //==========================================================
    public String LibInfo() {
        String lstr="RSACode Library (Class) for encrypting and decoding data on Android devices using RSA protocols. Allows you to generate, convert, save and restore encryption keys, encrypt, decode, save to a file and read data from a file. (c) by Valery Shmelev https://www.linkedin.com/in/valery-shmelev-479206227/ Open Source GNU GPL";
        return lstr; //Return Text (Library Information)
    }
    //==========================================================

    //==========================================================
    // Convert String to CharCodeString
    // Convert TEXT (STRING) to UNICODE (STRING) to work with national alphabets
    //==========================================================
    public String String2Code(String PlainText) {
        String chcstr="";
        String charc="";
        int i = 0; // Number of characters in String
        char[] buf = new char[PlainText.length()]; // Array for PlainText string letters (for encryption)
        int code=0; // Symbol code
        buf = PlainText.toCharArray(); // The string to encrypt - into an array of characters

        for(i = 0; i < PlainText.length(); i++) {
            code = (int) buf[i]; // Character code from array
            charc = String.format("\\u%04x", (int) buf[i]); // Convert Character CODE to TEXT (STRING)
            chcstr = chcstr + charc; // Generate a character string of character codes of a string FOR encryption
        }
        Log.d(LOG_TAG, "== ==| FULL UNICODE TEXT |== ==" + chcstr);

        return chcstr; //CharCodeString
    }
    //==========================================================


    //==========================================================
    // Convert CharCodeString to String
    // Convert UNICODE (STRING) to text string (STRING) to work with national alphabets
    //==========================================================
    public String Code2String(String StringInText) {
        String chcstr="";
        String charc="";
        String RestoreTXT=""; // Recovered from UNICODE STRING
        int i = 0; // Number of characters in a string StringInText - string of character codes (6 bytes each)
        int e = StringInText.length()/6; // Number of letters in original text string (before encoding) = StringInText/6
        int in = 0;

        char[] buf = new char[6]; // Array of 6 characters - one UNICODE
        char[] buftxt = new char[e]; // CHAR Array = length of original string before encryption

        for(i = 0; i < StringInText.length(); i = i+6) {
            StringInText.getChars(i, i+6, buf, 0); //Write 6 characters i through i+6 to buf array starting from position 0
            chcstr = StringInText.substring(i,i+6);
            // Log.d(LOG_TAG, "== ==| ONE UNICODE |== ==" + chcstr);
            charc = StringInText.substring(i+2,i+6); // Remove UNICODE prefix /u - Get character code
            RestoreTXT = RestoreTXT + String.valueOf(Character.toChars(Integer.parseInt(charc, 16)));
        }
        Log.d(LOG_TAG, "== ==| RESTORD UNICODE to STRING |== == " + RestoreTXT); // UNICODE - in STRING, taking into account non-Latin letters
        return RestoreTXT; //CharCodeString
    }
    //==========================================================


    //==========================================================

    //==========================================================
    // Simple digit data obfuscation
    // String SourceText - String to obfuscate
    // int e - The digit to be replaced in the original string
    // int d - Number to change to int e (New value)
    // Used to process a UNICODE string, or Keys
    //==========================================================
    public String ObfuscationD(String SourceText, String e, String d) {
        String lstr =""; // Obfuscated text
        String newlstr =""; // Obfuscated text
        String math1 = "5"; 
        String math2 = "7"; 
        int h = 0;
        ////char[] buf = new char[1];
        String l = "*"; 
        if(Integer.parseInt(math1) < 10)
        {
            if(Integer.parseInt(math2) < 10)
            {
                Log.d(LOG_TAG, "== ==| MATH1 < 10 |== == ");
                for(h = 0; h < SourceText.length(); h++) {
                    math1=SourceText.substring(h,h+1); //Write 6 characters i through i+1 to buf array starting from position 0
                    //// chr = e;
                    //// Log.d(LOG_TAG, "== ==| OBFUSCATION IF |== == " + math1 + " " + math2);
                    if (math1.equals(e)) {  //Если найдена цифра 5
                        Log.d(LOG_TAG, "== ==| OBFUSCATION IF 2 |== == " + d);
                        lstr = lstr + l; // Заменяем на *
                    } else {
                        lstr = lstr + math1;
                        //// Log.d(LOG_TAG, "== ==| NO |== == ");
                    }
                }


                for(h = 0; h < lstr.length(); h++) {
                    math1=lstr.substring(h,h+1); //Write 6 characters i through i+1 to buf array starting from position 0
                    //// chr = e;
                    //// Log.d(LOG_TAG, "== ==| OBFUSCATION IF |== == " + math1 + " " + math2);
                    if (math1.equals(d)) {  
                        Log.d(LOG_TAG, "== ==| OBFUSCATION IF 3 |== == " + d);
                        newlstr = newlstr + e;
                    } else {
                        newlstr = newlstr + math1;
                        //// Log.d(LOG_TAG, "== ==| NO |== == ");
                    }
                }
                Log.d(LOG_TAG, "== ==| OBFUSCATION 2 |== == " + newlstr);



                lstr = "";
                for(h = 0; h < newlstr.length(); h++) {
                    math1=newlstr.substring(h,h+1); //Write 6 characters i through i+1 to buf array starting from position 0
                    //// chr = e;
                    //// Log.d(LOG_TAG, "== ==| OBFUSCATION IF |== == " + math1 + " " + math2);
                    if (math1.equals(l)) {  //
                        Log.d(LOG_TAG, "== ==| OBFUSCATION IF 4 |== == " + d);
                        lstr = lstr + d;
                    } else {
                        lstr = lstr + math1;
                        //// Log.d(LOG_TAG, "== ==| NO |== == ");
                    }
                }
                Log.d(LOG_TAG, "== ==| OBFUSCATION 3 |== == " + lstr);


            } else {
                return lstr = "== One Digit Only =="; //Return Obfucated Text
            }
        } else {
            return lstr = "== One Digit Only =="; //Return Obfucated Text
        }

        Log.d(LOG_TAG, "== ==| OBFUSCATION ALL |== == " + lstr);
        return lstr; //Return Obfucated Text
    }
    //==========================================================

    // ============================================================
    //
    // eText string fragmentation
    // Tested in RSALargeText.java
    //
    // FRAGMENTATION
    //
    // USE:
    //
    //         eFragment = eFragment(eText); // Fragmentation
    //
    //  eFragment - Array with text fragments to encrypt
    //  eText - Text to fragment
    //  eFragment.lenght - Number of elements in the array
    //
    // ============================================================
    public String[] eFragment(String eText) { // TEXT fragmentation
        int eL = eText.length(); // eText line length
        String[] masString = new String[(int) (eL/50)+1]; // Array for string fragments - full 100 bytes each and for the rest
        if (eText != null || eText.length() > 0) { // If eText string exists and length > zero
            String Text100 = "";
            int i = 0; // Number of blocks/fragments
            int y = 0; // The starting position of the block in the eText line
            int e = 0; // Fragment count in masString summit
            if (eL >= 50) {
                int x = eL / 50;
                i = (int) x; // Integer from division
                while (i > 0) { // As long as there is text for at least one block
                    Text100 = eText.substring(y, y + 50);
                    i = i - 1;
                    if (e == 0) {
                        masString [e] = Text100; // Write to block array
                    } else {
                        
                        masString[e] = Text100; // Write to block array
                    }
                    Log.d("== Block==", "== == Large Block == == ["+ e +"] " + masString [e]);
                    e = e + 1; // Next Block Array Number
                    y = y + 50;
                }
                if (i == 0) {
                    Text100 =eText.substring(y, eL);
                    
                    masString [e] = Text100; // Write to block array
                    Log.d("== Block==", "== == eText Block == == ["+ e +"] " + masString [e]);
                }
            } else {
                //i = 0;
                if (i == 0) {
                    Text100 =eText.substring(y, eL);
                   
                    masString [e] = Text100; // Write to block array
                    Log.d("== Block==", "== == eText Block == == ["+ e +"] " + masString [e]);
                }
            }


            // Crypto RSA (c) by Valery Shmelev https://www.linkedin.com/in/valery-shmelev-479206227/
        }
        return masString; //Return Text array - Fragments array

    } // eFragment(String eText)


    // ============================================================
    //
    // toDecode string DEfragmentation
    // Tested in RSALargeText.java
    //
    // DEFRAGMENTATION
    //
    // USE:
    //
    //  splitted = eDEFragment(toDecode); // Defragmentation
    //
    //  toDecode - Ciphertext with delimiters to split into blocks
    //  splitted - Array of blocks to decode without delimiters
    //  splitted.lenght - Number of elements in the array
    //
    // ============================================================
    public String[] eDEFragment(String toDecode) {
        // ============================================================
        // Defragmenting an eText string
        // ============================================================
        String[] splitted = toDecode.split("<oflameron>"); // Split encrypted string by delimiter and write to array
        return splitted; //Return Text array
    }






}