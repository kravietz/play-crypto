# play-crypto

This sample [Play](http://www.playframework.com/) application demonstrates state of cryptography in the Play 2.1.

## Quick start

Clone this repo, which is a simple, ready-to-use Play application:

	$ git clone https://github.com/kravietz/play-crypto.git
	$ cd play-crypto
	$ play run
  
Now you can visit [http://localhost:9000/PlayCrypto](http://localhost:9000/PlayCrypto) ahd [http://localhost:9000/StringEnvelope](http://localhost:9000/PlayCrypto) to see views using Play Crypto and [StringEnvelope](https://github.com/kravietz/StringEnvelope) implementations respectively.

## Cryptography in Play framework

Main motivation for this work was to counter security issues caused by Play implementation of session variables. Session variable is a store that allows a web application to set variables related to a particular user's session. In the traditional Java [HttpServlet](http://docs.oracle.com/javaee/1.3/api/javax/servlet/http/HttpServlet.html) a session variable can be set using [setAttribute()](http://docs.oracle.com/javaee/1.4/api/javax/servlet/http/HttpSession.html#setAttribute(java.lang.String, java.lang.Object)) and retrieved using [getAttribute()](http://docs.oracle.com/javaee/1.4/api/javax/servlet/http/HttpSession.html#getAttribute(java.lang.String)). **These values never leave the web application - they are stored on server side.**

Play also offers [session variables](http://www.playframework.com/documentation/2.1.x/JavaSessionFlash) which may be set using `session()` (actual session variables) or `flash()` methods (short lived messages). They are **stored in HTTP cookie in the client's browser.** This is clearly explained in the documentation and was motivated by stateless architecture of Play apps but nonetheless it's causing security problems:

* Integrity. Whatever is sent to the user's browser may come back tampered with. Server-side variables don't really have this problem because they never leave the trusted web applicaiton environment. Fortunately, Play provides integrity protection for variables set with `session()` but not `flash()` variables.
* Confidentiality. Whatever is stored in the cookie is sent over HTTP and persisted in the user's browser. While confidentiality in transit can be protected to some extent with TLS and cookie flags such as *Secure*, it's creates a number of challenges and introduces additional complexity caused by a need to protect something that shouldn't probably ever leave the server.

Play has a built-in [Crypto library](https://github.com/playframework/playframework/blob/2.1.x/framework/src/play/src/main/scala/play/api/libs/Crypto.scala) that offers basic cryptographic functions such as hashing, digital signature and encryption.

The  integrity protection for `session()` uses this library `sign()` method, which is basically a [HMAC](https://en.wikipedia.org/wiki/HMAC) over the session data using SHA-1 and constant `application.secret` as secret key, which isperfectly sufficient in most cases. The hex string in the below sample cookie is the HMAC over  plaintext `variable` that follows it:

	PLAY_SESSION="a71463aa0bddc8edc9e7e694f11ad6f4b2b8aa6a-variable=value"; Path=/; HTTPOnly
  
If the HMAC doesn't match the data, the cookie is discarded and `session()` call returns `null` (this is implemented in [Http](https://github.com/playframework/playframework/blob/2.1.x/framework/src/play/src/main/scala/play/api/mvc/Http.scala) module).

While the integrity protection is quite robust, most application architects will also face the problem of confidentiality and Play's Crypto library is not really production-ready in this aspect. The `encryptAES()` function in Play is very simple and has two major issues:

* it always uses the same key for encryption (again `application.secret`), which means the same plaintext will be always encrypted to the same ciphertext;
* it uses AES in [ECB mode](https://en.wikipedia.org/wiki/Block_cipher_modes_of_operation#Electronic_codebook_.28ECB.29), which results in the same blocks of data producing the same ciphertexts.

While the module theoretically allows other encryption modes via a configuration variable, it doesn't  have any interface for setting encryption parameters, which makes it impossible to use anything apart from ECB.

## StringEnvelope

As a quick fix I have created [StringEnvelope](https://github.com/kravietz/StringEnvelope) class that reimplements the cryptographic and can be easily integrated with any Play application. Installation:

* Place [StrinEnvelope.jar](https://github.com/kravietz/StringEnvelope/releases/download/1.0/StringEnvelope.jar) inside `lib` directory of your Play application
* import `org.owasp.StringEnvelope`
* initialize `StringEnvelope env = new StringEnvelope()`
* use `env.wrap(plaintext, key)` to encrypt and `env.unwrap(ciphertext, key)` to decrypt

Have a look at [app/controllers/Application.java](https://github.com/kravietz/play-crypto/blob/master/app/controllers/Application.java) - it's all there.

