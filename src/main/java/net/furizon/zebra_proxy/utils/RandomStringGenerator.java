package net.furizon.zebra_proxy.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class RandomStringGenerator {
    public static final String BASE56 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";

    public static @NotNull String generateRandomString(int length, @NotNull String dictionary) {
        char[] data = new char[length];
        Random rand = new Random();
        int dictLength = dictionary.length();

        for (int i = 0; i < length; i++) {
            rand.nextInt();
            data[i] = dictionary.charAt(rand.nextInt(dictLength));
        }

        return new String(data);
    }
}
