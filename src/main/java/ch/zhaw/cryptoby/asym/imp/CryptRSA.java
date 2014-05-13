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
package ch.zhaw.cryptoby.asym.imp;

import ch.zhaw.cryptoby.asym.itf.CryptAsym;
import ch.zhaw.cryptoby.helper.CryptobyHelper;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 *
 * @author Toby
 */
public class CryptRSA implements CryptAsym {

    private static final BigInteger E = BigInteger.valueOf(65537);

    @Override
    public byte[] encrypt(byte[] plainInput, byte[] publicKey) {

        BigInteger n = new BigInteger(publicKey);

        int keySize = publicKey.length;
        int dataBlockSize = keySize + 2 * (keySize / 128);
        int plainBlockSize = keySize - 2;
        int plainPlusOneBlockSize = plainBlockSize + 1;
        int wholeLen = plainInput.length;
        int cryptBlocksLen = (wholeLen / plainBlockSize) * dataBlockSize;
        int dataBlocksLen = cryptBlocksLen + 3 * dataBlockSize;
        int plainBlocksLen = (wholeLen / plainBlockSize) * plainBlockSize;
        int rest = wholeLen - plainBlocksLen;

        byte[] cryptOutput = new byte[dataBlocksLen];
        byte[] cryptBlock;
        byte[] plainBlock = new byte[plainBlockSize];
        byte[] plusOnePlainBlock = new byte[plainPlusOneBlockSize];
        byte[] dataBlock = new byte[dataBlockSize];
        byte[] initVektorBlock;
        byte[] firstVektorBlock;
        byte[] secVektorBlock;
        byte[] cryptBlockSize;
        byte[] one = BigInteger.ONE.toByteArray();
        int halfOfVektor;

        if (plainBlocksLen > keySize) {

            do {
                SecureRandom rnd = new SecureRandom();
                halfOfVektor = dataBlockSize / 2;

                firstVektorBlock = new BigInteger(halfOfVektor * 8 - 1, rnd).toByteArray();
                secVektorBlock = new BigInteger(halfOfVektor * 8 - 1, rnd).toByteArray();

                initVektorBlock = new byte[dataBlockSize];
                System.arraycopy(firstVektorBlock, 0, initVektorBlock, 0, halfOfVektor);
                System.arraycopy(secVektorBlock, 0, initVektorBlock, halfOfVektor, halfOfVektor);

                firstVektorBlock = encryptBlock(firstVektorBlock, n);
                secVektorBlock = encryptBlock(secVektorBlock, n);
            } while (firstVektorBlock.length != keySize || secVektorBlock.length != keySize);

            byte[] prevBlock = new byte[dataBlockSize];
            System.arraycopy(initVektorBlock, 0, prevBlock, 0, dataBlockSize);
            byte[] nextBlock = new byte[dataBlockSize];
            int j = 0;
            for (int i = 0; i < plainBlocksLen; i += plainBlockSize) {
                // Copy Part of PlainInput in to Block
                System.arraycopy(plainInput, i, plainBlock, 0, plainBlockSize);

                // Add a One Byte into first Byte of Plain Array
                System.arraycopy(one, 0, plusOnePlainBlock, 0, one.length);
                System.arraycopy(plainBlock, 0, plusOnePlainBlock, 1, plainBlockSize);

                // Encrypt Block
                cryptBlock = encryptBlock(plusOnePlainBlock, n);
                
                // Copy Crypt Block in to extended DataBlock
                System.arraycopy(cryptBlock, 0, dataBlock, 0, cryptBlock.length);

                // Copy in last Byte of dataBlock the Size of cryptBlock
                cryptBlockSize = BigInteger.valueOf(cryptBlock.length - keySize).toByteArray();
                System.arraycopy(cryptBlockSize, 0, dataBlock, dataBlock.length - 1, cryptBlockSize.length);
                System.arraycopy(dataBlock, 0, nextBlock, 0, dataBlockSize);

                // XOR dataBlock with prevBlock
                dataBlock = CryptobyHelper.xorByteArrays(dataBlock, prevBlock);
                System.arraycopy(nextBlock, 0, prevBlock, 0, dataBlockSize);
                System.arraycopy(dataBlock, 0, cryptOutput, j, dataBlockSize);
                j += dataBlockSize;
            }
            // crypt rest of PlainInput
            plainBlock = new byte[rest];
            plusOnePlainBlock = new byte[rest + 1];
            dataBlock = new byte[dataBlockSize];
            System.arraycopy(plainInput, plainBlocksLen, plainBlock, 0, plainBlock.length);

            // Add a One Byte in to first Byte of Plain Array
            System.arraycopy(one, 0, plusOnePlainBlock, 0, one.length);
            System.arraycopy(plainBlock, 0, plusOnePlainBlock, 1, plainBlock.length);

            // Encrypt rest of PlainInput
            cryptBlock = encryptBlock(plusOnePlainBlock, n);
            System.arraycopy(cryptBlock, 0, dataBlock, 0, cryptBlock.length);

            // Copy in last Byte of dataBlock the Size of cryptBlock
            cryptBlockSize = BigInteger.valueOf(cryptBlock.length - keySize).toByteArray();
            System.arraycopy(cryptBlockSize, 0, dataBlock, dataBlock.length - 1, 1);
            dataBlock = CryptobyHelper.xorByteArrays(dataBlock, prevBlock);
            System.arraycopy(dataBlock, 0, cryptOutput, dataBlocksLen - 3 * dataBlockSize, dataBlockSize);

            // Put crypted initVektor into last 2 Bytes of cryptOutput Array
            System.arraycopy(firstVektorBlock, 0, cryptOutput, dataBlocksLen - 2 * dataBlockSize, firstVektorBlock.length);
            System.arraycopy(secVektorBlock, 0, cryptOutput, dataBlocksLen - 1 * dataBlockSize, secVektorBlock.length);

        } else {
            cryptOutput = encryptBlock(plainInput, n);
        }
        return cryptOutput;
    }

    @Override
    public byte[] decrypt(byte[] cryptInput, byte[] privateKey) {
        byte[] dByteArray = getDfromKey(privateKey);
        byte[] nByteArray = getNfromKey(privateKey);

        BigInteger d = new BigInteger(dByteArray);
        BigInteger n = new BigInteger(nByteArray);

        int keySize = nByteArray.length;
        int dataBlockSize = keySize + 2 * (keySize / 128);
        int plainBlockSize = keySize - 2;
        int wholeLen = cryptInput.length;
        int dataBlocksLen = wholeLen - 3 * dataBlockSize;
        int plainBlocksLen;
        int halfOfVektor = dataBlockSize / 2;

        byte[] plainOutput;
        byte[] allPlainBlocks;
        byte[] cryptBlock;
        byte[] plainBlock = new byte[plainBlockSize];
        byte[] plusOnePlainBlock;
        byte[] dataBlock = new byte[dataBlockSize];
        byte[] prevBlock = new byte[dataBlockSize];
        byte[] initVektorBlock = new byte[dataBlockSize];
        byte[] firstVektorBlock = new byte[keySize];
        byte[] secVektorBlock = new byte[keySize];
        byte[] cryptBlockSize;

        if (wholeLen > keySize) {
            plainBlocksLen = (dataBlocksLen / dataBlockSize) * plainBlockSize;
            while (plainBlocksLen > dataBlocksLen) {
                plainBlocksLen = plainBlocksLen + plainBlockSize;
            }
            allPlainBlocks = new byte[plainBlocksLen];
            // Get initVektor from last 2 Bytes of CryptInput
            System.arraycopy(cryptInput, wholeLen - 2 * dataBlockSize, firstVektorBlock, 0, keySize);
            System.arraycopy(cryptInput, wholeLen - 1 * dataBlockSize, secVektorBlock, 0, keySize);

            // Decrypt Vektors and merge together
            firstVektorBlock = decryptBlock(firstVektorBlock, n, d);
            secVektorBlock = decryptBlock(secVektorBlock, n, d);
            System.arraycopy(firstVektorBlock, 0, initVektorBlock, 0, halfOfVektor);
            System.arraycopy(secVektorBlock, 0, initVektorBlock, halfOfVektor, halfOfVektor);

            System.arraycopy(initVektorBlock, 0, prevBlock, 0, dataBlockSize);
            int j = 0;
            for (int i = 0; i < dataBlocksLen; i += dataBlockSize) {
                System.arraycopy(cryptInput, i, dataBlock, 0, dataBlockSize);
                // XOR with prevBlock
                dataBlock = CryptobyHelper.xorByteArrays(prevBlock, dataBlock);
                System.arraycopy(dataBlock, 0, prevBlock, 0, dataBlockSize);

                // Get Size of cryptBlock
                cryptBlockSize = new byte[1];
                System.arraycopy(dataBlock, dataBlock.length - 1, cryptBlockSize, 0, 1);
                cryptBlock = new byte[keySize + new BigInteger(cryptBlockSize).intValue()];
                System.arraycopy(dataBlock, 0, cryptBlock, 0, cryptBlock.length);

                // Decrypt cryptBlock
                plusOnePlainBlock = decryptBlock(cryptBlock, n, d);

                // Remove one from first Byte of Array
                System.arraycopy(plusOnePlainBlock, 1, plainBlock, 0, plainBlockSize);

                System.arraycopy(plainBlock, 0, allPlainBlocks, j, plainBlockSize);
                j += plainBlockSize;
            }
            dataBlock = new byte[dataBlockSize];
            System.arraycopy(cryptInput, dataBlocksLen, dataBlock, 0, dataBlockSize);

            dataBlock = CryptobyHelper.xorByteArrays(prevBlock, dataBlock);

            cryptBlockSize = new byte[1];
            System.arraycopy(dataBlock, dataBlock.length - 1, cryptBlockSize, 0, 1);
            cryptBlock = new byte[keySize - new BigInteger(cryptBlockSize).intValue()];
            System.arraycopy(dataBlock, 0, cryptBlock, 0, cryptBlock.length);

            plusOnePlainBlock = decryptBlock(cryptBlock, n, d);
            plainBlock = new byte[plusOnePlainBlock.length - 1];
            // Remove one from first Byte of Array
            System.arraycopy(plusOnePlainBlock, 1, plainBlock, 0, plainBlock.length);
            plainOutput = new byte[plainBlocksLen + plainBlock.length];

            System.arraycopy(allPlainBlocks, 0, plainOutput, 0, plainBlocksLen);
            System.arraycopy(plainBlock, 0, plainOutput, plainBlocksLen, plainBlock.length);

        } else {
            plainOutput = decryptBlock(cryptInput, n, d);
        }

        return plainOutput;
    }

    private byte[] encryptBlock(byte[] block, BigInteger n) {

        BigInteger blockInt = new BigInteger(block);
        if (blockInt.compareTo(BigInteger.ZERO) < 0) {
            blockInt = blockInt.modPow(E, n);
            return blockInt.multiply(BigInteger.valueOf(-1)).toByteArray();
        } else {
            return (blockInt).modPow(E, n).toByteArray();
        }
    }

    public byte[] decryptBlock(byte[] block, BigInteger n, BigInteger d) {
        BigInteger blockInt = new BigInteger(block);
        if (blockInt.compareTo(BigInteger.ZERO) < 0) {
            blockInt = blockInt.modPow(d, n);
            return blockInt.multiply(BigInteger.valueOf(-1)).toByteArray();
        } else {
            return (blockInt).modPow(d, n).toByteArray();
        }
    }

    private byte[] getDfromKey(byte[] privateKey) {
        // Get D from the first Part of the PrivateKey
        int midOfKey = privateKey.length / 2;
        byte[] dByteArray = new byte[midOfKey];
        System.arraycopy(privateKey, 0, dByteArray, 0, midOfKey);
        return dByteArray;
    }

    private byte[] getNfromKey(byte[] privateKey) {
        // Get N from the second Part of the PrivateKey
        int midOfKey = privateKey.length / 2;
        byte[] nByteArray = new byte[midOfKey];
        System.arraycopy(privateKey, midOfKey, nByteArray, 0, midOfKey);
        return nByteArray;
    }

}
