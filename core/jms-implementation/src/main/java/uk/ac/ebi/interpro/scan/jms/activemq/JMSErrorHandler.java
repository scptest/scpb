package uk.ac.ebi.interpro.scan.jms.activemq;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.ErrorHandler;

/**
 *
 * Handle errors in the listener
 * - log the error and either try to recover or exit(1)
 *
 * @author gift nuka
 */
@Service
public class JMSErrorHandler implements ErrorHandler {

    private static final Logger LOGGER = Logger.getLogger(JMSErrorHandler.class.getName());

    private int errorCount = 0;
    @Override
    public void handleError(Throwable t) {
        errorCount ++;
        LOGGER.error("Error in listener", t);

        if(errorCount == 3){
            // We may not be able to recover from this error, likely to be an IO, OS problem
            System.exit(1);
        }
    }

}