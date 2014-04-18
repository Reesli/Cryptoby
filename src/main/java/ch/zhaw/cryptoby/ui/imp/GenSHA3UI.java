/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ch.zhaw.cryptoby.ui.imp;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GenSHA3UI {
 
        public static void genSHA3Key(CryptobyConsole console) {
        final Scanner scanner = new Scanner(System.in);
        // Initial Variables
        int keySize;
        String pwAns;
        String key;
        String password;
        
        // Input Size of the SHA Key: possible are 224,256,384,512
        do {
            System.out.println("Set Key Size");
            System.out.println("Please enter one of these Sizes: 224 256 384 512");
            while (!scanner.hasNextInt()) {
                System.out.println("That's not a number! Enter a positive number:");
                scanner.next();
            }
            keySize = scanner.nextInt();
        } while (keySize != 224 && keySize != 256 && keySize != 384 && keySize != 512 );
        
        // Input a Password or nothing, in the case it will be used a Secure Random number
        do {
            System.out.println("Do want use password. If not, it will be used a SecureRandom password.");
            System.out.println("Enter y or n:");
            pwAns = scanner.next();
        } while (!pwAns.equals("y") && !pwAns.equals("n"));
        
        if(pwAns.equals("y")){
            System.out.println("Enter Password for the Key:");
            password = scanner.next();
        } else {
            password = "";
        }
        
        // Initial Miller Rabin Object
        console.getCore().getClient().setKeyGenArt("SHA3");
        console.getCore().initKeyGen();
        
        // Get Result of Test
        if(password.equals("")) {
            key = console.getCore().getKeyGenPriv().generateKey(keySize);
        } else {
            key = console.getCore().getKeyGenPriv().generateKey(keySize, password);
        }
        
        // Print Key
        System.out.println("SHA3-"+keySize+": "+key);
        
        // Enter for Continues
        try {
            System.in.read();
        } catch (IOException ex) {
            Logger.getLogger(CryptobyConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Back to Menu Choose PrimeTest
        console.chooseGenKey();
    }
}
