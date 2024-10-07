package monster.giz;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFLogger {

    public static final Logger LOGGER = LoggerFactory.getLogger(SimpleFilters.NAMESPACE);

    public static void log(String msg) {
        LOGGER.info(msg);
    }

}
