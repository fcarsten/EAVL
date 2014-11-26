package org.auscope.portal.server.web.service.wps;

import java.io.IOException;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.annotation.ThreadSafe;
import org.auscope.portal.core.services.PortalServiceException;
import org.auscope.portal.server.web.service.wps.WpsVm.VmStatus;
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
	private static final int VM_POOL_SIZE = 1;

	protected final Log log = LogFactory.getLog(getClass());

	private static final String TYPE_STRING = "openstack-nova";
	private static final String CLOUD_ENDPOINT = "https://keystone.rc.nectar.org.au:5000/v2.0";

	private static final String VM_ID = "Melbourne/84460643-569c-4871-8e2e-d8a8d4db4e2f";

	private static final String INSTANCE_TYPE = "Melbourne/1";

	private static final String groupName = "eavl-wps-r";
	private Deque<WpsVm> vmPool = new LinkedList<>();
	private VmPoolPersistor persistor;
	private ComputeService computeService;

	private String keypair = null;

	@Autowired
	public VmPool(VmPoolPersistor persistor, String accessKey, String secretKey) {
		this.persistor = persistor;
		Properties overrides = new Properties();

		ContextBuilder b = ContextBuilder.newBuilder(TYPE_STRING)
				.endpoint(CLOUD_ENDPOINT).overrides(overrides)
				.credentials(accessKey, secretKey);

		// if (apiVersion != null) {
		// b.apiVersion(apiVersion);
		// }

		ComputeServiceContext context = b
				.buildView(ComputeServiceContext.class);
		this.computeService = context.getComputeService();

		if (persistor != null)
			try {
				verifyVmPool(this.persistor.loadVmPool());
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
	}

	private void verifyVmPool(Set<WpsVm> set) {
		for (WpsVm wpsVm : set) {
			switch (wpsVm.getOrWaitForStableState()) {
			case READY: {
				synchronized (vmPool) {
					vmPool.add(wpsVm);
				}
				break;
			}
			case FAILED: {
				log.warn("VM no longer good: " + wpsVm.getId() + " ("
						+ wpsVm.getIpAddress() + ")");
				terminateVm(wpsVm);
			}
			default:
				break;
			}
		}
	}

	public WpsVm getFreeVm() throws PortalServiceException {
		while (true) {
			checkAndFixPoolSize();
			try {
				synchronized (vmPool) {
					// Do round robin. Needs to be synchronized so size is not
					// incorrect.
					WpsVm res = vmPool.pop();
					vmPool.addLast(res);
					return res;
				}
			} catch (NoSuchElementException e) {
				log.warn("No VM available despite trying to fix pool size...");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					log.warn("Ignoring InterruptedException despite better knowledge");
				}
			}
		}
	}

	private synchronized void checkAndFixPoolSize()
			throws PortalServiceException {
		int currentPoolSize = 0;
		synchronized (vmPool) {
			currentPoolSize = vmPool.size();
		}

		int delta = VM_POOL_SIZE - currentPoolSize;

		if (delta < 0) {
			retireVms(-delta);
		} else if (delta > 0) {
			createVms(delta);
		}
	}

	private void createVms(int delta) throws PortalServiceException {
		// TODO: Do loop in parallel
		for (int i = 0; i < delta; i++) {
			WpsVm vm = creatVm();
			VmStatus state = vm.getOrWaitForStableState();
			switch (state) {
			case READY: {
				synchronized (vmPool) {
					vmPool.add(vm);
					if (persistor != null) {
						try {
							persistor.saveVmPool(new HashSet<>(vmPool));
						} catch (IOException e) {
							log.error(e.getMessage(), e);
						}

					}
				}
				break;
			}
			case FAILED:
				terminateVm(vm);
			default:
				log.error("VM in inconsisten state: " + state.toString());
			}
		}
	}

	private void terminateVm(WpsVm vm) {
		// TODO Implement VmPool#terminateVm()
	}

	private void retireVms(int i) {
		// TODO Implement VmPool#retireVms()
	}

	public static void main(String[] arg) {
	}

	private WpsVm creatVm() throws PortalServiceException {
		TemplateOptions options = ((NovaTemplateOptions) computeService
				.templateOptions())// .availabilityZone("NCI")
				.keyPairName(getKeypair()).securityGroups("all");

		Template template = computeService.templateBuilder().imageId(VM_ID)
				.hardwareId(INSTANCE_TYPE).options(options).build();

		// Start up the job, we should have exactly 1 node start
		Set<? extends NodeMetadata> results;
		try {
			results = computeService.createNodesInGroup(groupName, 1, template);
		} catch (RunNodesException e) {
			log.error(String.format("An unexpected error '%1$s' occured.'",
					e.getMessage()));
			log.debug("Exception:", e);
			throw new PortalServiceException(
					"An unexpected error has occured while executing your job. Most likely this is from the lack of available resources. Please try using"
							+ "a smaller virtual machine",
					"Please report it to cg-admin@csiro.au : " + e.getMessage(),
					e);
		}
		if (results.isEmpty()) {
			log.error("JClouds returned an empty result set. Treating it as job failure.");
			throw new PortalServiceException(
					"Unable to start compute node due to an unknown error, no nodes returned");
		}
		NodeMetadata result = results.iterator().next();
		String ipAddress = result.getPublicAddresses().iterator().next();
		log.info(result.getId() + ": " + ipAddress);
		WpsVm res = new WpsVm(result.getId(), ipAddress);
		res.setStatus(VmStatus.STARTING);
		return res;
	}

	public String getKeypair() {
		return keypair != null ? keypair : "vgl-developers";
	}

	public void setKeypair(String keypair) {
		this.keypair = keypair;
	}

}
