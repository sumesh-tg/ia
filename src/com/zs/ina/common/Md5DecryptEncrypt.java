/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zs.ina.common;

/**
 *
 * @author SUMESH
 */
import org.apache.commons.codec.binary.Base64;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import javax.swing.JOptionPane;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * This class provides methods to encrypt and decrypt data.
 *
 * @author Stephane Moreau
 */
public class Md5DecryptEncrypt {

    private static byte[] sharedvector = {
        0x01, 0x02, 0x03, 0x05, 0x07, 0x0B, 0x0D, 0x11
    };

    /**
     *
     * @param RawText
     * @return
     */
    public static String EncryptText(String RawText) {
        String EncText = "";
        byte[] keyArray = new byte[24];
        byte[] temporaryKey;
        String key = "developerclubwizard";
        byte[] toEncryptArray = null;

        try {

            toEncryptArray = RawText.getBytes("UTF-8");
            MessageDigest m = MessageDigest.getInstance("MD5");
            temporaryKey = m.digest(key.getBytes("UTF-8"));

            if (temporaryKey.length < 24) // DESede require 24 byte length key
            {
                int index = 0;
                for (int i = temporaryKey.length; i < 24; i++) {
                    keyArray[i] = temporaryKey[index];
                }
            }

            Cipher c = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyArray, "DESede"), new IvParameterSpec(sharedvector));
            byte[] encrypted = c.doFinal(toEncryptArray);
            EncText = Base64.encodeBase64String(encrypted);

        } catch (NoSuchAlgorithmException NoEx) {
            JOptionPane.showMessageDialog(null, NoEx);
        } catch (UnsupportedEncodingException encodingException) {
            encodingException.printStackTrace();
        } catch (NoSuchPaddingException noSuchPaddingException) {
            noSuchPaddingException.printStackTrace();
        } catch (InvalidKeyException invalidKeyException) {
            invalidKeyException.printStackTrace();

        } catch (InvalidAlgorithmParameterException algorithmParameterException) {
            algorithmParameterException.printStackTrace();
        } catch (IllegalBlockSizeException blockSizeException) {
            blockSizeException.printStackTrace();
        } catch (BadPaddingException badPaddingException) {
            badPaddingException.printStackTrace();
        }

        return EncText;
    }

    /**
     *
     * @param EncText
     * @return
     */
    public static String DecryptText(String EncText) {

        String RawText = "";
        byte[] keyArray = new byte[24];
        byte[] temporaryKey;
        String key = "developerclubwizard";
        byte[] toEncryptArray = null;

        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            temporaryKey = m.digest(key.getBytes("UTF-8"));

            if (temporaryKey.length < 24) // DESede require 24 byte length key
            {
                int index = 0;
                for (int i = temporaryKey.length; i < 24; i++) {
                    keyArray[i] = temporaryKey[index];
                }
            }

            Cipher c = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyArray, "DESede"), new IvParameterSpec(sharedvector));
            byte[] decrypted = c.doFinal(Base64.decodeBase64(EncText));

            RawText = new String(decrypted, "UTF-8");
        } catch (NoSuchAlgorithmException NoEx) {
            JOptionPane.showMessageDialog(null, NoEx);
        } catch (UnsupportedEncodingException encodingException) {
            encodingException.printStackTrace();
        } catch (NoSuchPaddingException noSuchPaddingException) {
            noSuchPaddingException.printStackTrace();
        } catch (InvalidKeyException invalidKeyException) {
            invalidKeyException.printStackTrace();

        } catch (InvalidAlgorithmParameterException algorithmParameterException) {
            algorithmParameterException.printStackTrace();
        } catch (IllegalBlockSizeException blockSizeException) {
            blockSizeException.printStackTrace();
        } catch (BadPaddingException badPaddingException) {
            badPaddingException.printStackTrace();
        }

        return RawText;

    }

    /**
     *
     * @param args
     */
    public static void main(String args[]){
        Md5DecryptEncrypt obj =new Md5DecryptEncrypt();
        System.out.println(obj.EncryptText("jins672"));
         System.out.println(obj.DecryptText("VImIc9CP7s0="));
         
    }
}
