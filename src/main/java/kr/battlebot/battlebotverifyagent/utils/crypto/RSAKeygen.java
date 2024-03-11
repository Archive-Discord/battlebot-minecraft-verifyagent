package kr.battlebot.battlebotverifyagent.utils.crypto;

import kr.battlebot.battlebotverifyagent.BattlebotVerifyAgent;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.logging.Logger;

/**
 * An RSA key pair generator.
 *
 * @author Blake Beaupain
 */
public class RSAKeygen {


    /**
     * Generates an RSA key pair.
     *
     * @param bits
     *            The amount of bits
     * @return The key pair
     */
    public static KeyPair generate(int bits) throws Exception {
        BattlebotVerifyAgent.logger.info("Battlebot Verify Agent is generating an RSA key pair...");
        KeyPairGenerator keygen = KeyPairGenerator.getInstance("RSA");
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(bits,
                RSAKeyGenParameterSpec.F4);
        keygen.initialize(spec);
        return keygen.generateKeyPair();
    }

}
