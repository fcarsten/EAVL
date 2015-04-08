package org.auscope.portal.server.web.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

/**
 * Rudimentary unit of measure conversions
 *
 * TODO: outsource this to a remote service
 *
 * @author Josh Vote
 *
 */
@Service
public class UomService {
    /**
     * A union of a Trace Element name and its converting scale factor
     */
    public class TraceElementConversion {
        private String traceElement;
        private double conversion;
        public TraceElementConversion(String traceElement, double conversion) {
            this.traceElement = traceElement;
            this.conversion = conversion;
        }
        public String getTraceElement() {
            return traceElement;
        }
        public double getConversion() {
            return conversion;
        }
    }

    private Map<String, TraceElementConversion> lookups;

    public UomService() {
        lookups = new HashMap<String, TraceElementConversion>();
        lookups.put("Ac2O3", new TraceElementConversion("Ac", 9044.044497));
        lookups.put("Ag2O", new TraceElementConversion("Ag", 9310.120101));
        lookups.put("Ag2O", new TraceElementConversion("Ag", 9309.253398));
        lookups.put("Al2O3", new TraceElementConversion("Al", 5292.405398));
        lookups.put("As2O3", new TraceElementConversion("As", 7574.036204));
        lookups.put("As2O5", new TraceElementConversion("As", 6519.329813));
        lookups.put("B2O3", new TraceElementConversion("B", 3105.686512));
        lookups.put("BaO", new TraceElementConversion("Ba", 8953.353031));
        lookups.put("BeO", new TraceElementConversion("Be", 3603.214067));
        lookups.put("Bi2O3", new TraceElementConversion("Bi", 8970.218873));
        lookups.put("Bi2O5", new TraceElementConversion("Bi", 8393.486654));
        lookups.put("CaO", new TraceElementConversion("Ca", 7146.941109));
        lookups.put("Cb2O5", new TraceElementConversion("Cb", 6990.56274));
        lookups.put("CdO", new TraceElementConversion("Cd", 8754.267706));
        lookups.put("CeO2", new TraceElementConversion("Ce", 8140.670791));
        lookups.put("Ce2O3", new TraceElementConversion("Ce", 8537.522411));
        lookups.put("CoO", new TraceElementConversion("Co", 7864.726701));
        lookups.put("CO2", new TraceElementConversion("C", 2728.959721));
        lookups.put("Co3O4", new TraceElementConversion("Co", 7342.143906));
        lookups.put("Cr2O3", new TraceElementConversion("Cr", 6841.817187));
        lookups.put("Cs2O", new TraceElementConversion("Cs", 9432.182607));
        lookups.put("CuO", new TraceElementConversion("Cu", 7988.496565));
        lookups.put("Cu2O", new TraceElementConversion("Cu", 8881.783462));
        lookups.put("Dy2O3", new TraceElementConversion("Dy", 8713.078331));
        lookups.put("Er2O3", new TraceElementConversion("Er", 8745.080892));
        lookups.put("FeO", new TraceElementConversion("Fe", 7773.027594));
        lookups.put("Fe2O3", new TraceElementConversion("Fe", 6994.474365));
        lookups.put("Fe3O4", new TraceElementConversion("Fe", 7235.890014));
        lookups.put("Ga2O3", new TraceElementConversion("Ga", 7439.922625));
        lookups.put("Gd2O3", new TraceElementConversion("Gd", 8676.036786));
        lookups.put("GeO2", new TraceElementConversion("Ge", 6940.588562));
        lookups.put("HfO2", new TraceElementConversion("Hf", 8479.606546));
        lookups.put("HgO", new TraceElementConversion("Hg", 9260.974254));
        lookups.put("In2O3", new TraceElementConversion("In", 8271.298594));
        lookups.put("IrO", new TraceElementConversion("Ir", 9231.905465));
        lookups.put("K2O", new TraceElementConversion("K", 8301.510875));
        lookups.put("La2O3", new TraceElementConversion("La", 8526.603001));
        lookups.put("Li2O", new TraceElementConversion("Li", 4644.89758));
        lookups.put("MgO", new TraceElementConversion("Mg", 6030.999337));
        lookups.put("MnO", new TraceElementConversion("Mn", 7744.733581));
        lookups.put("MnO2", new TraceElementConversion("Mn", 6319.115324));
        lookups.put("Mn2O3", new TraceElementConversion("Mn", 6959.910913));
        lookups.put("MoO3", new TraceElementConversion("Mo", 6665.3336));
        lookups.put("Na2O", new TraceElementConversion("Na", 7418.397626));
        lookups.put("Nb2O5", new TraceElementConversion("Nb", 6990.56274));
        lookups.put("Nd2O3", new TraceElementConversion("Nd", 8573.388203));
        lookups.put("NiO", new TraceElementConversion("Ni", 7858.546169));
        lookups.put("P2O5", new TraceElementConversion("P", 4364.144191));
        lookups.put("PbO", new TraceElementConversion("Pb", 9731.413001));
        lookups.put("PbO2", new TraceElementConversion("Pb", 8662.508663));
        lookups.put("Pr2O3", new TraceElementConversion("Pr", 8544.817568));
        lookups.put("PtO", new TraceElementConversion("Pt", 9242.144177));
        lookups.put("RaO", new TraceElementConversion("Ra", 9338.812103));
        lookups.put("Rb2O", new TraceElementConversion("Rb", 9144.111192));
        lookups.put("ReO", new TraceElementConversion("Re", 9208.9511));
        lookups.put("RhO", new TraceElementConversion("Rh", 6428.801029));
        lookups.put("Ru", new TraceElementConversion("RuO", 8633.341967));
        lookups.put("SO2", new TraceElementConversion("S", 5005.005005));
        lookups.put("SO3", new TraceElementConversion("S", 4004.966158));
        lookups.put("SO4", new TraceElementConversion("S", 3337.56091));
        lookups.put("Sb2O3", new TraceElementConversion("Sb", 8336.112037));
        lookups.put("Sc2O3", new TraceElementConversion("Sc", 6519.754857));
        lookups.put("SeO2", new TraceElementConversion("Se", 7116.424708));
        lookups.put("SeO3", new TraceElementConversion("Se", 6219.292245));
        lookups.put("SiO2", new TraceElementConversion("Si", 4674.426214));
        lookups.put("Sm2O3", new TraceElementConversion("Sm", 8623.663332));
        lookups.put("SnO2", new TraceElementConversion("Sn", 7876.496534));
        lookups.put("SrO", new TraceElementConversion("Sr", 8455.944529));
        lookups.put("Ta2O5", new TraceElementConversion("Ta", 8190.00819));
        lookups.put("Tb2O3", new TraceElementConversion("Tb", 8688.097307));
        lookups.put("TeO2", new TraceElementConversion("Te", 7994.883275));
        lookups.put("ThO2", new TraceElementConversion("Th", 8788.118464));
        lookups.put("TiO2", new TraceElementConversion("Ti", 5995.203837));
        lookups.put("Tl2O3", new TraceElementConversion("Tl", 8949.346698));
        lookups.put("UO2", new TraceElementConversion("U", 8815.232722));
        lookups.put("U3O8", new TraceElementConversion("U", 8480.325645));
        lookups.put("VO2", new TraceElementConversion("V", 6142.128862));
        lookups.put("V2O5", new TraceElementConversion("V", 5601.613265));
        lookups.put("WO3", new TraceElementConversion("W", 7929.585283));
        lookups.put("Y2O3", new TraceElementConversion("Y", 7874.635798));
        lookups.put("ZnO", new TraceElementConversion("Zn", 8033.419023));
        lookups.put("ZrO2", new TraceElementConversion("Zr", 7403.020432));

    }

    /**
     * Provides a best effort of matching an arbitrary oxide chemical name
     * to an underlying trace element name and scaling factor from pct to ppm.
     *
     * Returns null if no match can be made.
     *
     * @param name
     * @return
     */
    public TraceElementConversion oxidePctToTracePpm(String name) {
        //Tidy up name as best we can
        name = name.replaceAll("[ _-]", "");
        name = name.toLowerCase();

        for (String key : lookups.keySet()) {
            if (name.contains(key.toLowerCase())) {
                return lookups.get(key);
            }
        }

        return null;
    }
}
