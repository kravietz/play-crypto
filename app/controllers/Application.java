package controllers;

import com.typesafe.config.ConfigFactory;
import org.owasp.StringEnvelope;
import play.api.libs.Crypto;
import play.mvc.Controller;
import play.mvc.Result;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;

public class Application extends Controller {

    static final String plaintext = "my plaintext мой открытый текст 我的明文";
    static final StringEnvelope env = new StringEnvelope();

    public Application() throws NoSuchPaddingException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, UnsupportedEncodingException, InvalidAlgorithmParameterException {
           if(!env.selfTest())
               throw new InvalidParameterException("Self test failed");
    }

    public static Result index2()
            throws NoSuchPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

        final String title =  "StringEnvelope";
        final String sessionVar = "sessStringEnvelope";

        String enc, decrypted = null;
        String key = ConfigFactory.load().getString("application.secret");

        enc = session(sessionVar);

        if (enc != null) {
            // encrypted flash found
            decrypted = env.unwrap(enc, key);
        }

        enc = env.wrap(plaintext, key);
        session().clear();
        session(sessionVar, enc);

        return(ok(views.html.crypto.render(title, enc, decrypted)));

    }

    public static Result index() {

        final String title =  "Play Crypto";
        final String sessionVar = "sessCrypto";

        String enc, decrypted = null;

        enc = session(sessionVar);

        if (enc != null) {
            decrypted = Crypto.decryptAES(enc);

        }

        enc = Crypto.encryptAES(plaintext);
        session().clear();
        session(sessionVar, enc);
        return(ok(views.html.crypto.render(title, enc, decrypted)));

    }

}
