package fmg.common;

import org.junit.Assert;
import org.junit.Test;

public class LoggerTest {

    @Test
    public void loggerTest() {
        Logger.debug("not visible");

        Logger.info(null);
        Logger.info("");

        Logger.info("{0}");

        Logger.info("{0}", 0);
        Logger.info("{0}, {1}", 0, '1');
        Logger.info("{0}, {1}, {2}", 0, '1', "2");
        Logger.info("{0}, {1}, {2}, {3}", 0, '1', "2", new byte[] {0,1,2});

        Logger.error("error");
        Logger.error("fail", (Exception)null);
        Logger.error("fail", new Exception());

        // illegal usage
        Logger.info("{1}", 0);
        Logger.info("{{{{");
        Logger.info("{{{{}");

        Assert.assertTrue(true);
    }

}
