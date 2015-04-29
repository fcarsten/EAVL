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
     * Temporary filename for the data file (TODO - make this auto generate)
     */
    public static final String FILE_TEMP_DATA2_CSV = "data-tmp2.csv";

    /**
     * The output of the data validation (post culling, pre imputation)
     */
    public static final String FILE_VALIDATED_DATA_CSV = "data-validated.csv";

    /**
     * The filename of the data.csv AFTER it has been culled and imputed but not yet uom scaled
     */
    public static final String FILE_IMPUTED_CSV = "data-imputed.csv";

    /**
     * The filename of the data.csv AFTER it has been culled, imputed and uom scaled
     */
    public static final String FILE_IMPUTED_SCALED_CSV = "data-imputed-scaled.csv";
    /**
     * The filename of the data.csv AFTER it has been imputed and centre log ratio'ed
     */
    public static final String FILE_IMPUTED_CENLR_CSV = "data-imputed-cenlr.csv";

    /**
     * The filename containing the kernel density estimate outputs
     */
    public static final String FILE_KDE_JSON_ALL = "data-kde-all.json";
    public static final String FILE_KDE_JSON_HIGH = "data-kde-high.json";

    /**
     * The filename containing the conditional probability outputs (encoded with the CSV data)
     */
    public static final String FILE_CP_CSV = "data-cp.csv";

    /**
     * The parameter details cache suffix
     */
    public static final String PD_CACHE_SUFFIX = ".pdcache.json";

    /**
     * The name of the estimate column encoded in the final results file
     */
    public static final String PARAMETER_ESTIMATE = "probability";
}
