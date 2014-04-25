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

package ch.zhaw.cryptoby.ui.imp.console;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MillerRabinUI {
    
        public static void testMillerRabin(CryptobyConsole console){
        final Scanner scanner = new Scanner(System.in);
        // Initial Variables
        int rounds;
        String result;
        String percent;
        BigInteger number;
        
        // Input Number for Primenumber Testing
        do {
            System.out.println("Set Primenumber to Test.");
            System.out.print("Please enter a positive number: ");
            while (!scanner.hasNextBigInteger()) {
                System.out.print("That's not a number! Enter a positive number: ");
                scanner.next();
            }
            number = scanner.nextBigInteger();
        } while (number.compareTo(BigInteger.ONE) < 0 );
        
        // Set the rounds of the Miller Rabin Test
        do {
            System.out.println("Set rounds parameter between 1 and 15.");
            System.out.print("Please enter the number of rounds: ");
            while (!scanner.hasNextInt()) {
                System.out.print("That's not a number! Enter a valid number: ");
                scanner.next();
            }
            rounds = scanner.nextInt();
        } while (rounds < 1 || rounds > 15);
        
        // Initial Miller Rabin Object
        console.getCore().getClient().setPrimTestArt("MillerRabin");
        console.getCore().getClient().setPrimetestrounds(rounds);
        console.getCore().initPrimeTest();
        
        // Get Result of Test
        if(console.getCore().getPrimetest().isPrime(number)) {
            result = "is probably a Primenumber";
        } else {
            result = "is not a Primenumber";
        }
        
        // Get Probably
        percent = String.valueOf(console.getCore().getPrimetest().getProbability());
        
        // Print Results
        System.out.println("Result: "+result+", Probably: "+percent);
        
        // Enter for Continues
        try {
            System.in.read();
        } catch (IOException ex) {
            Logger.getLogger(CryptobyConsole.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Back to Menu Choose PrimeTest
        console.menuPrimeTest();
    }
    
}
