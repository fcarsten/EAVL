package org.auscope.portal.server.eavl;

/**
 * Job file names and other "global" state items
 * @author Josh Vote
 *
 */
public final class EAVLJobConstants {
    /**
     * The filename of the current working CSV file
     */
    public static final String FILE_DATA_CSV = "data.csv";
    /**
     * Temporary filename for the data file (TODO - make this auto generate)
     */
    public static final String FILE_TEMP_DATA_CSV = "data-tmp.csv";

    /**
     * The filename of the data.csv AFTER it has been imputed
     */
    public static final String FILE_IMPUTED_CSV = "data-imputed.csv";
}
