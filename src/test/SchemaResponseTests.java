package test;
import java.io.File;
import java.io.FileReader;
import java.security.KeyPair;
import java.security.Signature;
import java.util.ArrayList;

import sp_app.SchemaResponse;
import static org.junit.Assert.*;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SchemaResponseTests {
	
	SchemaResponse app = new SchemaResponse();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String[] args = new String[1];
		args[0] = "theAnswerIs42";
		SchemaResponse.main(args);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void isKeyFileGenerated() {
		File output = new File("keys.json");
		assert(output.exists());
	}
	
	@Test
	public void doesFileHaveKeys(){
		File output = new File("keys.json");
		assert(output.exists());
		
		try{
			JSONTokener tokens = new JSONTokener(new FileReader(output));
			JSONObject keyData = new JSONObject(tokens);
			
			assert(keyData.has("private") && keyData.has("public"));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void verifyKeysAndValidateSignature(){
		app = new SchemaResponse();
		app.getKeysFromFile();
		
		String input = "theAnswerIs42";
		
		try{
			KeyPair myKeyPair = app.getMyKeys();
			byte[] inputBytes = input.getBytes("UTF8");
		    Signature sig = Signature.getInstance("SHA256WithRSA");
		    sig.initSign(myKeyPair.getPrivate());
		    sig.update(inputBytes);
		    byte[] signatureBytes = sig.sign();		    

		    sig.initVerify(myKeyPair.getPublic());
		    sig.update(inputBytes);

		    assert(sig.verify(signatureBytes));		    
		}catch(Exception e){
			e.printStackTrace();
		}
	}


}
