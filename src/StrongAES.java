import java.security.Key;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
public class StrongAES 
{
	String key;
	Key aesKey;
	
	public StrongAES(){
    	key = "Bar12345Bar12345"; // 128 bit key
        // Create key and cipher
        aesKey = new SecretKeySpec(key.getBytes(), "AES");
	}
    
    public String encript(String text){
    	String enc = null;
        try 
        {
        Cipher cipher = Cipher.getInstance("AES");
        // encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(text.getBytes());

        StringBuilder sb = new StringBuilder();
        for (byte b: encrypted) {
            sb.append((char)b);
        }

        // the encrypted String
        enc = sb.toString();
        }
        catch(Exception e){
        	
        	e.printStackTrace();
        }
        
        
        return enc;
    }
    
    public String decript(String enc){
    	
    	String decrypted = null;
    	
    	try{
    		
            Cipher cipher = Cipher.getInstance("AES");
            // encrypt the text
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

    		// now convert the string to byte array
            // for decryption
            byte[] bb = new byte[enc.length()];
            for (int i=0; i<enc.length(); i++) {
                bb[i] = (byte) enc.charAt(i);
            }

            // decrypt the text
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            decrypted = new String(cipher.doFinal(bb));
            System.err.println("decrypted:" + decrypted);
    		
            decrypted = new String(cipher.doFinal(bb));
    	}
        catch(Exception e){
        	
        	e.printStackTrace();
        }
    	return decrypted;

    }

}