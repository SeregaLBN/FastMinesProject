package fmg.swing.app.serializers;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import fmg.common.crypt.Simple3DES;
import fmg.core.app.AProjSettings;
import fmg.core.app.serializers.PlayersSerializer;

/** Players (de)serializer */
public class PlayersSwingSerializer extends PlayersSerializer {

    @Override
    protected byte[] writeTransform(byte[] data) throws IOException {
        // crypt data
        try  {
            return new Simple3DES(getSerializeKey()).encrypt(data);

        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    @Override
    protected byte[] readTransform(byte[] data) throws IOException {
        // decrypt data
        try {
            return new Simple3DES(getSerializeKey()).decrypt(data);
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    private static String getSerializeKey() throws NoSuchAlgorithmException {
        byte[] digest = MessageDigest.getInstance("MD5")
                                     .digest(Long.toString(VERSION)
                                                 .getBytes(StandardCharsets.UTF_8));
        return String.format("%032X", new BigInteger(1, digest));
    }

    @Override
    protected File getStatisticsFile() {
        return new File(AProjSettings.getStatisticsFileName());
    }

}
