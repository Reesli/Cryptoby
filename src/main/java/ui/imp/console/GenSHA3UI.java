/*
 * Copyright (C) 2014 Toby
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ui.imp.console;

import filemgr.CryptobyFileManager;
import helper.CryptobyHelper;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides menus in console UI for SHA3 implementation.
 *
 * @author Tobias Rees
 */
public class GenSHA3UI {
    
    private static final String quit = "QuitCrypt";

    /**
     *
     * @param console
     */
    public static void genSHA3KeyText(CryptobyConsole console) {
        final Scanner scanner = new Scanner(System.in);
        // Initial Variables
        int keySize;
        int choice;
        String pwAns;
        String key;
        String password;

        // Set Default Key Size
        keySize = 256;

        do {
            System.out.println("\n");
            System.out.println("Select Key Size in Bit");
            System.out.println("----------------------\n");
            System.out.println("1 - 224");
            System.out.println("2 - 256");
            System.out.println("3 - 384");
            System.out.println("4 - 512");
            System.out.println("5 - Back");
            System.out.print("Enter Number: ");
            while (!scanner.hasNextInt()) {
                System.out.println("That's not a number! Enter 1,2,3,4 or 5:");
                scanner.next();
            }
            choice = scanner.nextInt();
        } while (choice < 1 || choice > 5);

        switch (choice) {
            case 1:
                keySize = 224;
                break;
            case 2:
                keySize = 256;
                break;
            case 3:
                keySize = 384;
                break;
            case 4:
                keySize = 512;
                break;
            case 5:
                console.menuGenKey();
                break;
            default:
                console.menuGenKey();
        }

        // Input a Password or nothing, in the case it will be used a Secure Random number
        do {
            System.out.println("Do you want to use a password. If not, it will be used a SecureRandom password.");
            System.out.print("Enter y or n: ");
            pwAns = scanner.next();
        } while (!pwAns.equals("y") && !pwAns.equals("n"));

        if (pwAns.equals("y")) {
            System.out.print("Enter Password for the Key: ");
            password = scanner.next();
        } else {
            password = "";
        }

        // Initial Key Generator
        console.getCore().getClient().setKeySymArt("SHA3");
        console.getCore().initSymKey();

        // Get Result of Test
        if (password.equals("")) {
            key = console.getCore().getKeyGenSym().generateKey(keySize);
        } else {
            key = console.getCore().getKeyGenSym().generateKey(keySize, password);
        }

        // Print Key
        System.out.println("SHA3-" + keySize + ": " + key);

        // Enter for Continues
        try {
            System.in.read();
        } catch (IOException ex) {
            Logger.getLogger(CryptobyConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
        scanner.close();
        // Back to Menu Choose PrimeTest
        console.menuGenKey();
    }

    /**
     *
     * @param console
     */
    public static void genSHA3KeyFile(CryptobyConsole console) {
        Scanner scanner = new Scanner(System.in);
        String keyPath;
        // Initial Variables
        int keySize;
        int choice;
        String pwAns;
        String key;
        String password;

        // Set Default Key Size
        keySize = 256;

        do {
            System.out.println("\n");
            System.out.println("Select Key Size in Bit");
            System.out.println("----------------------\n");
            System.out.println("1 - 224");
            System.out.println("2 - 256");
            System.out.println("3 - 384");
            System.out.println("4 - 512");
            System.out.println("5 - Back");
            System.out.print("Enter Number: ");
            while (!scanner.hasNextInt()) {
                System.out.println("That's not a number! Enter 1,2,3,4 or 5:");
                scanner.next();
            }
            choice = scanner.nextInt();
        } while (choice < 1 || choice > 5);

        switch (choice) {
            case 1:
                keySize = 224;
                break;
            case 2:
                keySize = 256;
                break;
            case 3:
                keySize = 384;
                break;
            case 4:
                keySize = 512;
                break;
            case 5:
                console.menuGenKey();
                break;
            default:
                console.menuGenKey();
        }

        // Input a Password or nothing, in the case it will be used a Secure Random number
        do {
            System.out.println("Do you want to use a password. If not, it will be used a SecureRandom password.");
            System.out.print("Enter y or n: ");
            pwAns = scanner.next();
        } while (!pwAns.equals("y") && !pwAns.equals("n"));

        if (pwAns.equals("y")) {
            System.out.print("Enter Password for the Key: ");
            password = scanner.next();
        } else {
            password = "";
        }
        scanner.close();
        // Input Path for saving Private Key
        scanner = new Scanner(System.in);
        System.out.println("Enter Path to saving Private Key(Type '" + quit + "' to Escape):");
        scanner.useDelimiter("\n");
        if (scanner.hasNext(quit)) {
        	scanner.close();
            console.menuGenKey();
        }
        keyPath = scanner.next();

        // Initial Key Generator
        console.getCore().getClient().setKeySymArt("SHA3");
        console.getCore().initSymKey();

        // Get Result of Test
        if (password.equals("")) {
            key = console.getCore().getKeyGenSym().generateKey(keySize);
        } else {
            key = console.getCore().getKeyGenSym().generateKey(keySize, password);
        }

        // Save Key
               try {
            //Put private Key to File
            CryptobyFileManager.putKeyToFile(keyPath, key);
        } catch (IOException ex) {
            CryptobyHelper.printIOExp();
            scanner.close();
            console.menuGenKey();
        }
        System.out.println("\nAES Key File saved to this Path:");
        System.out.println(keyPath);

        // Enter for Continues
        try {
            System.in.read();
        } catch (IOException ex) {
        	scanner.close();
            Logger.getLogger(CryptobyConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
        scanner.close();
        // Back to Menu Choose PrimeTest
        console.menuGenKey();
    }

}
