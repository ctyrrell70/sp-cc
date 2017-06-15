package sp_app;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringWriter;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import org.json.*;

public class SchemaResponse {
	
	private KeyPair myKeys;

	/**
	* This is the main method which makes use of convertInput, init/generateKeyPairs, and persistKeys methods.
	* @param args input is used in convertInput method to sign and encode the string.
	* @return Nothing.
	*/
	public static void main(String[] args) {		
		SchemaResponse app = new SchemaResponse();
        if (args.length > 0){        	
        	if(!app.initKeyPairs())
        		app.generateKeyPairs();
        	
        	String outJson = app.convertInput(args[0]);        	
        	System.out.println(outJson);
        	
        	app.persistKeys();
        }
    }
	
	/**
	* This method calls getKeysFromFile and will tell the main function to generate new keys if necessary.
	* @param None.
	* @return boolean.
	*/
	public boolean initKeyPairs(){
		if(getKeysFromFile())
			return true;
		else
			return false;
	}
	
	/**
	* This method generates a random RSA key pair if there isn't a key pair file, or if the file doesn't contain the keys.
	* @param None.
	* @return Nothing.
	*/
	public void generateKeyPairs(){
		try{
			KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		    kpg.initialize(1024, random);
		    KeyPair keyPair = kpg.genKeyPair();	
		    setMyKeys(keyPair);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	* This is the method that will call generateSignature and generatePubkey, which will sign and encode the input / encode the public key.
	* @param input passed to the signing function generateSignature.
	* @return a String of the JSON object that contains the output.
	*/
	public String convertInput(String input){
		String out = new String();
		
		try{			
			out = new JSONObject()
				.put("message", input)
				.put("signature", generateSignature(input))
				.put("pubkey", generatePubkey())
				.toString();
			
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return out;
	}
	
	/**
	* This method signs the input in SHA256 with the private key and then encodes it in Base64.
	* @param input This is the value that is signed and then encoded.
	* @return a String containing the result of the signing / encoding of the input.
	*/
	public String generateSignature(String input){				
		try{
			KeyPair myKeyPair = getMyKeys();
			byte[] inputBytes = input.getBytes("UTF8");
		    Signature sig = Signature.getInstance("SHA256WithRSA");
		    sig.initSign(myKeyPair.getPrivate());
		    sig.update(inputBytes);
		    byte[] signatureBytes = sig.sign();
		    
		    return Base64.getEncoder().encodeToString(signatureBytes);
		}catch(Exception e){
			e.printStackTrace();
			return "";
		}				
	}
	
	/**
	* This method encodes the public key and formats it in PEM.
	* @param None.
	* @return a String with the PEM formatted public key.
	*/
	public String generatePubkey(){
		StringWriter sw = new StringWriter();
		byte[] publicKeyByteArray = getMyKeys().getPublic().getEncoded();
		String encodedPub = Base64.getEncoder().encodeToString(publicKeyByteArray);
        
		sw.write("-----BEGIN CERTIFICATE-----\n");
        sw.write(encodedPub.replaceAll("(.{64})", "$1\n"));
        sw.write("\n-----END CERTIFICATE-----\n");

	    return sw.toString();
	}
	
	/**
	* This is method writes the public and private keys to a local file.
	* @param None.
	* @return Nothing.
	*/
	public void persistKeys(){
		try{
			byte[] privateKey = getMyKeys().getPrivate().getEncoded();
			byte[] publicKey = getMyKeys().getPublic().getEncoded();
			JSONObject keys = new JSONObject()
				.put("private", Base64.getEncoder().encodeToString(privateKey))
				.put("public", Base64.getEncoder().encodeToString(publicKey));
			
        	FileWriter file = new FileWriter("keys.json");
            file.write(keys.toString());
            file.flush();
            file.close();
    	}catch(Exception e){
    		e.printStackTrace();
    	}
	}
	
	/**
	* This method determines whether there is a local key file and validates it.  If it's valid we need to parse the file and load the 
	* data to initiate and set the value of the app's private and public keys.
	* @param None.
	* @return boolean is returned to determine if the file's validation has passed and the key's have been loaded.
	*/
	public boolean getKeysFromFile(){
		File keysFile = new File("keys.json");
		if(!keysFile.exists())
			return false;
		else{
			try{
				JSONTokener tokens = new JSONTokener(new FileReader(keysFile));
				JSONObject keyData = new JSONObject(tokens);
				
				if(keyData.has("private") && keyData.has("public")){					
					String privString = keyData.getString("private");
				    byte[] decodedPriv = Base64.getDecoder().decode(privString);
				    String pubString = keyData.getString("public");
				    byte[] decodedPub = Base64.getDecoder().decode(pubString);

				    
				    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedPriv);
				    X509EncodedKeySpec pubkeySpec = new X509EncodedKeySpec(decodedPub);
		    	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		    	    PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		    	    PublicKey publicKey = keyFactory.generatePublic(pubkeySpec);
		    	    
		    	    KeyPair keys = new KeyPair(publicKey, privateKey);
				    setMyKeys(keys);
				    return true;
				}else{
					return false;
				}
				
			}catch(Exception e){
				e.printStackTrace();
				return false;
			}
		}
	}

	/**
	* This method gets our app's KeyPair which contains a PublicKey and PrivateKey.
	* @param None.
	* @return KeyPair containing our Public and Private keys.
	*/
	public KeyPair getMyKeys() {
		return myKeys;
	}

	/**
	* This method sets our app's KeyPair, which contains our Public and Private keys.
	* @param keys used to update the current stored value.
	* @return Nothing.
	*/
	public void setMyKeys(KeyPair keys) {
		myKeys = keys;
	}
	
}
