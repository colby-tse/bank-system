/**
 * Represents a bank account.
 * @author Colby Tse
 * @version 1.0
 * @since 1.0
 */

import java.io.*;
import java.util.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class Account {

    //------------------------------------------------------------------------------------------------------------------
    // Properties
    //------------------------------------------------------------------------------------------------------------------

    private String id;
    private Cipher ciph;
    private byte[] encryptedPw;
    private SecretKey key;
    private double balance;

    //------------------------------------------------------------------------------------------------------------------
    // Constructors
    //------------------------------------------------------------------------------------------------------------------
    
    public Account(String id, String pw, double balance) throws Exception {
        this.id = id;
        this.ciph = Cipher.getInstance("AES");
        this.encryptPw(pw);
        this.balance = balance;
    }

    public Account (String id, byte[] encryptedPw, SecretKey key, double balance) throws Exception {
        this.id = id;
        this.ciph = Cipher.getInstance("AES");
        this.encryptedPw = encryptedPw;
        this.key = key;
        this.balance = balance;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Accessors and Mutators
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Gets this account's ID.
     * @return A String representing this account's ID.
     */
    public String getID() {
        return this.id;
    }

    /**
     * Gets this account's encrypted password.
     * @return A byte array representing this account's encrypted
     *         password.
     */
    public byte[] getEncryptedPw() {
        return this.encryptedPw;
    }

    /**
     * Gets this account's secret key used for encryption
     * and decryption.
     * @return A SecretKey representing this account's secret key.
     */
    public SecretKey getKey() {
        return this.key;
    }

    /**
     * Gets this account's decrypted password.
     * @return A String representing this account's decrypted
     *         password.
     */
    public String getDecryptedPw() throws Exception {
        return this.decryptPw();
    }

    /**
     * Sets this account's password.
     * @param pw A String containing this account's password.
     */
    public void setPw(String pw) throws Exception {
        this.encryptPw(pw);
    }

    /**
     * Gets this account's current balance.
     */
    public double getBalance() {
        return this.balance;
    }

    /**
     * Sets this account's balance.
     * @param amount A double containing this account's balance.
     */
    public void setBalance(double amount) {
        this.balance = amount;
    }

    //------------------------------------------------------------------------------------------------------------------
    // Encryption and Decryption Functions
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Encrypts the given password.
     * @param pw The password to encrypt.
     */
    public void encryptPw(String pw) throws Exception {
        try {
            // Generating objects of KeyGenerator and SecretKey
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            SecretKey key = keygen.generateKey();

            // Creating byte array to store password
            byte[] pwToEncrypt = pw.getBytes("UTF8");

            // Encrypting password
            this.ciph.init(Cipher.ENCRYPT_MODE, key);
            byte[] encryptedPw = this.ciph.doFinal(pwToEncrypt);

            // Store encrypted password and secret key
            this.encryptedPw = encryptedPw;
            this.key = key;
        } catch (Exception e) {
            System.out.println("Encryption failed. Ending banking process");
            System.exit(0);
        }
    }

    /**
     * Decrypts this account's encrypted password.
     * @return A String representing the decrypted password.
     */
    public String decryptPw() throws Exception {
        try {
            // Decrypting password
            this.ciph.init(Cipher.DECRYPT_MODE, this.key);
            byte[] decryptedPw = this.ciph.doFinal(this.encryptedPw);
            return new String(decryptedPw);
        } catch (Exception e) {
            System.out.println("Decryption failed. Ending banking process");
            System.exit(0);
        }
        return null;
    }
}