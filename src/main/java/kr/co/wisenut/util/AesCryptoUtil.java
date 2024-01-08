package kr.co.wisenut.util;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AesCryptoUtil {

	private static final byte[] key = hexStringToByteArray("2BDF247745B276BB847D93F950672642EF943B2AD14D2A57BCA743E048CE516F");	// STT Secret Key
	private static String dbKey = "passwordencrypt123456789abcdefgh";	// DB Connection Key
	private static String secretKey = "wisetextanalyzer";	// 관리도구 Secret Key
	
	private static Cipher cipher;
	private static String alg = "AES/CBC/PKCS5Padding";
	private static String enc = "UTF-8";
	
	// 1. 관리도구
	//	  1) 관리도구 학습/분석 암호화
	public static String encryption(String text) {
		try {
			cipher = Cipher.getInstance(alg);
			cipher.init( Cipher.ENCRYPT_MODE
					   , new SecretKeySpec(secretKey.getBytes(), "AES")
					   , new IvParameterSpec(new byte[16]));
			
			return new String(Base64.getEncoder().encode(cipher.doFinal(text.getBytes(enc))));
		} catch (Exception e) {
			return text;
		}
	}
	
	//	  2) 관리도구 학습/분석 복호화
	public static String decryption(String text) {
		try {
			cipher = Cipher.getInstance(alg);
			cipher.init( Cipher.DECRYPT_MODE
					   , new SecretKeySpec(secretKey.getBytes(), "AES")
					   , new IvParameterSpec(new byte[16]));
			
			return new String(cipher.doFinal(Base64.getDecoder().decode(text)), enc);
		} catch (Exception e) {
			return text;
		}
	}
	
	// 2. STT
	//	  1) STT Secret Key 설정 (16byte)
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    //	  2) STT 암호화
    public static String encryptStt(String string) {
        String retValue = "";
 
        try {
 
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
 
            byte[] encData = cipher.doFinal(string.getBytes());
 
            retValue = Base64.getEncoder().encodeToString(encData);
 
        } catch (InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("[ERROR_99] UnExpected Exception - " + e.getMessage());
        }
 
        return retValue;
    }
 
    //	  3) STT 복호화
    public static String decryptStt(String base64Str) {
        String retValue = "";
 
        try {
            byte[] encData = Base64.getDecoder().decode(base64Str);
 
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
            byte[] decData = cipher.doFinal(encData);
 
            retValue = new String(decData);
 
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException e) {
            System.out.println("[ERROR_99] UnExpected Exception - " + e.getMessage());
        }
 
        return retValue;
    }
    
    // 3. DB Properties (16 bit)
	public static String decryptionDb(String text) {
		
		try {
			byte [] keyData = dbKey.getBytes();
			SecretKey secureKey =new SecretKeySpec(keyData,"AES");
			String IV = dbKey.substring(0,16);
			
			cipher = Cipher.getInstance(alg);
			cipher.init( Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(IV.getBytes(enc)));
			byte[] byteStr = org.apache.commons.codec.binary.Base64.decodeBase64(text.getBytes());	
			
			return new String(cipher.doFinal(byteStr), enc);
		} catch (Exception e) {
			return text;
		}
	}
}
