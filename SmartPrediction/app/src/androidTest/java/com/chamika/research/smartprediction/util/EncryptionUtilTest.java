package com.chamika.research.smartprediction.util;


import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class EncryptionUtilTest {
    @Test
    public void testDecrypt() {
        Context context = context();
        String input = "Smart Predictions";
        assertEquals(input, decryptText(context, encryptText(context, input)));
    }

    @Test
    public void testEncrypt() {
        Context context = context();
        String encryptedText1 = encryptText(context, "Smart Prediction");
        String encryptedText2 = encryptText(context, "Smart Prediction");
        assertEquals(encryptedText1, encryptedText2);
    }

    private Context context() {
        return InstrumentationRegistry.getTargetContext();
    }

    private String decryptText(Context context, String input) {
        String decryptedText = null;
        try {
            decryptedText = EncryptionUtil.decrypt(context, input);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Decryption failed");
        }
        return decryptedText;
    }

    private String encryptText(Context context, String input) {
        String encryptedText = null;
        try {
            encryptedText = EncryptionUtil.encrypt(context, input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptedText;
    }
}
