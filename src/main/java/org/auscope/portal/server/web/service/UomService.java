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
        lookups.put("Ac2O3", new TraceElementConversion("Ac", 11057));
        lookups.put("Ag2O", new TraceElementConversion("Ag", 10741));
        lookups.put("Ag2O", new TraceElementConversion("Ag", 10742));
        lookups.put("Al2O3", new TraceElementConversion("Al", 18895));
        lookups.put("As2O3", new TraceElementConversion("As", 13203));
        lookups.put("As2O5", new TraceElementConversion("As", 15339));
        lookups.put("B2O3", new TraceElementConversion("B", 32199));
        lookups.put("BaO", new TraceElementConversion("Ba", 11169));
        lookups.put("BeO", new TraceElementConversion("Be", 27753));
        lookups.put("Bi2O3", new TraceElementConversion("Bi", 11148));
        lookups.put("Bi2O5", new TraceElementConversion("Bi", 11914));
        lookups.put("CaO", new TraceElementConversion("Ca", 13992));
        lookups.put("Cb2O5", new TraceElementConversion("Cb", 14305));
        lookups.put("CdO", new TraceElementConversion("Cd", 11423));
        lookups.put("CeO2", new TraceElementConversion("Ce", 12284));
        lookups.put("Ce2O3", new TraceElementConversion("Ce", 11713));
        lookups.put("CoO", new TraceElementConversion("Co", 12715));
        lookups.put("CO2", new TraceElementConversion("C", 36644));
        lookups.put("Co3O4", new TraceElementConversion("Co", 13620));
        lookups.put("Cr2O3", new TraceElementConversion("Cr", 14616));
        lookups.put("Cs2O", new TraceElementConversion("Cs", 10602));
        lookups.put("CuO", new TraceElementConversion("Cu", 12518));
        lookups.put("Cu2O", new TraceElementConversion("Cu", 11259));
        lookups.put("Dy2O3", new TraceElementConversion("Dy", 11477));
        lookups.put("Er2O3", new TraceElementConversion("Er", 11435));
        lookups.put("FeO", new TraceElementConversion("Fe", 12865));
        lookups.put("Fe2O3", new TraceElementConversion("Fe", 14297));
        lookups.put("Fe3O4", new TraceElementConversion("Fe", 13820));
        lookups.put("Ga2O3", new TraceElementConversion("Ga", 13441));
        lookups.put("Gd2O3", new TraceElementConversion("Gd", 11526));
        lookups.put("GeO2", new TraceElementConversion("Ge", 14408));
        lookups.put("HfO2", new TraceElementConversion("Hf", 11793));
        lookups.put("HgO", new TraceElementConversion("Hg", 10798));
        lookups.put("In2O3", new TraceElementConversion("In", 12090));
        lookups.put("IrO", new TraceElementConversion("Ir", 10832));
        lookups.put("K2O", new TraceElementConversion("K", 12046));
        lookups.put("La2O3", new TraceElementConversion("La", 11728));
        lookups.put("Li2O", new TraceElementConversion("Li", 21529));
        lookups.put("MgO", new TraceElementConversion("Mg", 16581));
        lookups.put("MnO", new TraceElementConversion("Mn", 12912));
        lookups.put("MnO2", new TraceElementConversion("Mn", 15825));
        lookups.put("Mn2O3", new TraceElementConversion("Mn", 14368));
        lookups.put("MoO3", new TraceElementConversion("Mo", 15003));
        lookups.put("Na2O", new TraceElementConversion("Na", 13480));
        lookups.put("Nb2O5", new TraceElementConversion("Nb", 14305));
        lookups.put("Nd2O3", new TraceElementConversion("Nd", 11664));
        lookups.put("NiO", new TraceElementConversion("Ni", 12725));
        lookups.put("P2O5", new TraceElementConversion("P", 22914));
        lookups.put("PbO", new TraceElementConversion("Pb", 10276));
        lookups.put("PbO2", new TraceElementConversion("Pb", 11544));
        lookups.put("Pr2O3", new TraceElementConversion("Pr", 11703));
        lookups.put("PtO", new TraceElementConversion("Pt", 10820));
        lookups.put("RaO", new TraceElementConversion("Ra", 10708));
        lookups.put("Rb2O", new TraceElementConversion("Rb", 10936));
        lookups.put("ReO", new TraceElementConversion("Re", 10859));
        lookups.put("RhO", new TraceElementConversion("Rh", 15555));
        lookups.put("Ru", new TraceElementConversion("RuO", 11583));
        lookups.put("SO2", new TraceElementConversion("S", 19980));
        lookups.put("SO3", new TraceElementConversion("S", 24969));
        lookups.put("SO4", new TraceElementConversion("S", 29962));
        lookups.put("Sb2O3", new TraceElementConversion("Sb", 11996));
        lookups.put("Sc2O3", new TraceElementConversion("Sc", 15338));
        lookups.put("SeO2", new TraceElementConversion("Se", 14052));
        lookups.put("SeO3", new TraceElementConversion("Se", 16079));
        lookups.put("SiO2", new TraceElementConversion("Si", 21393));
        lookups.put("Sm2O3", new TraceElementConversion("Sm", 11596));
        lookups.put("SnO2", new TraceElementConversion("Sn", 12696));
        lookups.put("SrO", new TraceElementConversion("Sr", 11826));
        lookups.put("Ta2O5", new TraceElementConversion("Ta", 12210));
        lookups.put("Tb2O3", new TraceElementConversion("Tb", 11510));
        lookups.put("TeO2", new TraceElementConversion("Te", 12508));
        lookups.put("ThO2", new TraceElementConversion("Th", 11379));
        lookups.put("TiO2", new TraceElementConversion("Ti", 16680));
        lookups.put("Tl2O3", new TraceElementConversion("Tl", 11174));
        lookups.put("UO2", new TraceElementConversion("U", 11344));
        lookups.put("U3O8", new TraceElementConversion("U", 11792));
        lookups.put("VO2", new TraceElementConversion("V", 16281));
        lookups.put("V2O5", new TraceElementConversion("V", 17852));
        lookups.put("WO3", new TraceElementConversion("W", 12611));
        lookups.put("Y2O3", new TraceElementConversion("Y", 12699));
        lookups.put("ZnO", new TraceElementConversion("Zn", 12448));
        lookups.put("ZrO2", new TraceElementConversion("Zr", 13508));
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
