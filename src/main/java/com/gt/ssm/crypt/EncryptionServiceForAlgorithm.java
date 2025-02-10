package com.gt.ssm.crypt;

public interface EncryptionServiceForAlgorithm {

    String getAlgorithm();

    byte[] getKeyFromPassword(String keyPassword, String salt);

    String encrypt(String data, byte[] key, String iv);

    String decrypt(String encrypted, byte[] key, String iv);
}
