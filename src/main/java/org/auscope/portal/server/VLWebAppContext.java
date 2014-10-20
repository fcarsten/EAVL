package org.auscope.portal.server;

import java.util.Arrays;

import org.auscope.portal.core.server.PortalProfileXmlWebApplicationContext;

/**
 * Allows us to inject additional XML configuration files in the webapp
 *
 * @author Josh Vote
 *
 */
public class VLWebAppContext extends PortalProfileXmlWebApplicationContext {
    @Override
    protected String[] getDefaultConfigLocations() {
        String[] locations = super.getDefaultConfigLocations();

        String[] auscopeLocations = Arrays.copyOf(locations, locations.length + 1);
        auscopeLocations[auscopeLocations.length - 1] = DEFAULT_CONFIG_LOCATION_PREFIX + "applicationContext-security.xml";
        return auscopeLocations;
    }
}
