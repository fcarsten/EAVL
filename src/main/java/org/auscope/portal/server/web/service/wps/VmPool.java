package org.auscope.portal.server.web.service.wps;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.annotation.ThreadSafe;
import org.auscope.portal.core.services.PortalServiceException;
import org.jclouds.ContextBuilder;
import org.jclouds.compute.ComputeService;
import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.RunNodesException;
import org.jclouds.compute.domain.NodeMetadata;
import org.jclouds.compute.domain.Template;
import org.jclouds.compute.options.TemplateOptions;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.springframework.beans.factory.annotation.Autowired;

@ThreadSafe
public class VmPool {
    protected final Log log = LogFactory.getLog(getClass());

	private static final String TYPE_STRING = "openstack-nova";
	private static final String CLOUD_ENDPOINT = "https://keystone.rc.nectar.org.au:5000/v2.0";

	private static final String VM_ID = "Melbourne/84460643-569c-4871-8e2e-d8a8d4db4e2f";

	private static final String INSTANCE_TYPE = "Melbourne/1";

	private static final String groupName = "eavl-wps-r";
	private Set<WpsVm> vmPool = new HashSet<>();
	private VmPoolPersistor persistor;
	private ComputeService computeService;

	private String keypair=null;

	@Autowired
	public VmPool(VmPoolPersistor persistor, String accessKey, String secretKey, String apiVersion) {
        this.persistor = persistor;
        if(persistor!=null)
			try {
				vmPool.addAll(this.persistor.loadVmPool());
			} catch (IOException e) {
				log.error(e.getMessage(),e);
			}

        Properties overrides = new Properties();

        ContextBuilder b = ContextBuilder.newBuilder(TYPE_STRING)
                .endpoint(CLOUD_ENDPOINT)
                .overrides(overrides)
                .credentials(accessKey, secretKey);

        if (apiVersion != null) {
            b.apiVersion(apiVersion);
        }

        ComputeServiceContext context = b.buildView(ComputeServiceContext.class);
        this.computeService = context.getComputeService();

	}

	public WpsVm getFreeVm() throws PortalServiceException {
		checkPoolSize();
		return vmPool.iterator().next();
	}

	private void checkPoolSize() throws PortalServiceException {
		if(vmPool.size()<1) {
			vmPool.add(creatVm());
			if(persistor!=null) {
				try {
					persistor.saveVmPool(vmPool);
				} catch (IOException e) {
					log.error(e.getMessage(),e);
				}
			}
		}
	}

	public static void main(String[] arg) {
		try {
			new VmPool(new FileVmPoolPersister(), "GeophysicsVL:Carsten.Friedrich@csiro.au", "MjI1NDNlMWEwMjMzNWFm", null).checkPoolSize();
		} catch (PortalServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

	private WpsVm creatVm() throws PortalServiceException {
	       TemplateOptions options = ((NovaTemplateOptions)computeService.templateOptions())//.availabilityZone("NCI")
	            .keyPairName(getKeypair())
	            .securityGroups("all");

	        Template template = computeService.templateBuilder()
	                .imageId(VM_ID)
	                .hardwareId(INSTANCE_TYPE)
	                .options(options)
	                .build();

	        //Start up the job, we should have exactly 1 node start
	        Set<? extends NodeMetadata> results;
	        try {
	            results = computeService.createNodesInGroup(groupName, 1, template);
	        } catch (RunNodesException e) {
	            log.error(String.format("An unexpected error '%1$s' occured.'", e.getMessage()));
	            log.debug("Exception:", e);
	            throw new PortalServiceException("An unexpected error has occured while executing your job. Most likely this is from the lack of available resources. Please try using"
	                    + "a smaller virtual machine", "Please report it to cg-admin@csiro.au : " + e.getMessage(),e);
	        }
	        if (results.isEmpty()) {
	            log.error("JClouds returned an empty result set. Treating it as job failure.");
	            throw new PortalServiceException("Unable to start compute node due to an unknown error, no nodes returned");
	        }
	        NodeMetadata result = results.iterator().next();
	        String ipAddress = result.getPublicAddresses().iterator().next();
	        log.info(result.getId()+ ": "+ ipAddress);
	        return new WpsVm(result.getId(), ipAddress);

	}

    public String getKeypair() {
        return keypair != null ? keypair : "truststore-nectar";
    }

    public void setKeypair(String keypair) {
        this.keypair = keypair;
    }

}
