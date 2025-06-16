package com.gt.ssm.crypt;

public interface EncryptionServiceForAlgorithm {

    String getAlgorithm();

    byte[] getKeyFromPassword(String keyPassword, String salt);

    byte[] encrypt(byte[] data, byte[] key, String iv);

    byte[] decrypt(byte[] encrypted, byte[] key, String iv);
}
