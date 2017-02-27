package com.beeva.trustedoverlord.reporter;

import org.apache.logging.log4j.Logger;

/**
 * Created by Beeva
 */
public interface ReporterToBuilder {

    ReporterAndBuilder toMarkdown();
    ReporterAndBuilder toLogger();
    ReporterAndBuilder toLogger(Logger logger);

}
