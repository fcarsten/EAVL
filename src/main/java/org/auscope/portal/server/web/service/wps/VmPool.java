package org.auscope.portal.server.web.service.wps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

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
import org.jclouds.openstack.nova.v2_0.NovaApi;
import org.jclouds.openstack.nova.v2_0.compute.options.NovaTemplateOptions;
import org.jclouds.openstack.nova.v2_0.domain.zonescoped.AvailabilityZone;
import org.jclouds.openstack.nova.v2_0.extensions.AvailabilityZoneApi;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Optional;

@ThreadSafe
public class VmPool {

    /**
     * @author fri096
     *
     */
    public class VerifyVmAndCheckPoolTask extends VerifyVmTask {

        public VerifyVmAndCheckPoolTask(WpsVm vm) {
            super(vm);
        }

        @Override
        public Void call() throws Exception {
            super.call();
            checkAndFixPoolSizeAsync();
            return null;
        }
    }

    private int vmPoolSize = 1;

    /**
     * @return the vmPoolSize
     */
    public int getVmPoolSize() {
        return vmPoolSize;
    }

    /**
     * @param vmPoolSize
     *            the vmPoolSize to set
     */
    public void setVmPoolSize(int vmPoolSize) {
        if (vmPoolSize < 1)
            throw new IllegalArgumentException(
                    "VM Pool size must be bigger than 0");
        this.vmPoolSize = vmPoolSize;
        checkAndFixPoolSizeAsync();
    }

    protected final Log log = LogFactory.getLog(getClass());

    private static final String TYPE_STRING = "openstack-nova";
    private static final String CLOUD_ENDPOINT = "https://keystone.rc.nectar.org.au:5000/v2.0";

    private static final String VM_ID = "Melbourne/85f8903b-2cc4-4c3a-a9e9-38f5599f7996";

    private static final String INSTANCE_TYPE = "Melbourne/1";

    private static final String groupName = "eavl-wps-r";
    private Deque<WpsVm> vmPool = new LinkedList<>();
    private VmPoolPersistor persistor;
    private ComputeService computeService;

    private String keypair = null;

    private ThreadPoolExecutor executor;
    private Set<String> skippedZones = new HashSet<String>();

    private NovaApi lowLevelApi;

    /**
     * Gets the set of zone names that should be skipped when attempting to find
     * a zone to run a job at.
     *
     * @return
     */
    public Set<String> getSkippedZones() {
        return skippedZones;
    }

    /**
     * Sets the set of zone names that should be skipped when attempting to find
     * a zone to run a job at.
     *
     * @param skippedZones
     */
    public void setSkippedZones(Set<String> skippedZones) {
        this.skippedZones = skippedZones;
    }

    @Autowired
    public VmPool(VmPoolPersistor persistor, String accessKey,
            String secretKey, ThreadPoolExecutor executor) {
        this.persistor = persistor;
        this.executor = executor;
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
        this.lowLevelApi = b.buildApi(NovaApi.class);

        if (persistor != null) {
            try {
                final Set<WpsVm> peristedVms = VmPool.this.persistor
                        .loadVmPool();
                numOrderedVms = peristedVms.size();

                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        verifyVmPool(peristedVms);
                        checkAndFixPoolSizeAsync();
                    }
                });
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        } else {
            checkAndFixPoolSizeAsync();
        }
    }

    /**
     * @author fri096
     *
     */
    private class VerifyVmTask implements Callable<Void> {

        private WpsVm vm;

        public VerifyVmTask(WpsVm vm) {
            this.vm = vm;
        }

        @Override
        public Void call() throws Exception {
            switch (vm.getOrWaitForStableState()) {
            case READY: {
                synchronized (vmPool) {
                    vmPool.add(vm);
                    numOrderedVms--;
                }
                break;
            }
            case FAILED: {
                log.warn("VM no longer good: " + vm.getId() + " ("
                        + vm.getIpAddress() + ")");
                synchronized (vmPool) {
                    numOrderedVms--;
                }
                terminateVm(vm);
                break;
            }
            default:
                break;
            }
            return null;
        }
    }

    private void verifyVmPool(Set<WpsVm> set) {
        ArrayList<Future<Void>> futures = new ArrayList<Future<Void>>(
                set.size());
        for (WpsVm wpsVm : set) {
            futures.add(executor.submit(new VerifyVmTask(wpsVm)));
        }
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error verifying VM: " + e.getMessage(), e);
            }
        }
    }

    public WpsVm getFreeVm() throws PortalServiceException {
        while (true) {
            checkAndFixPoolSizeAsync();
            try {
                synchronized (vmPool) {
                    // Do round robin. Needs to be synchronized so size is not
                    // incorrect.
                    WpsVm res = vmPool.pop();
                    vmPool.addLast(res);
                    return res;
                }
            } catch (NoSuchElementException e) {
                log.warn("No VM available yet. Waiting some more ...");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    log.warn("Ignoring InterruptedException despite better knowledge");
                }
            }
        }
    }

    /**
     * Only access within synchronized (vmPool) {}
     */
    private int numOrderedVms = 0;

    private void checkAndFixPoolSizeAsync() {
        int currentPoolSize = 0;
        int numNewVmsNeeded = 0;
        ConcurrentLinkedQueue<WpsVm> badVms = new ConcurrentLinkedQueue<WpsVm>();

        log.info("Checking vm pool size");
        synchronized (vmPool) {
            boolean poolChanged = false;
            //
            // sort out any VMs that have been marked as bad or decommissioned
            //
            for (Iterator<WpsVm> iter = vmPool.iterator(); iter.hasNext();) {
                WpsVm vm = iter.next();
                if (vm.getStatus() == VmStatus.FAILED
                        || vm.getStatus() == VmStatus.DECOMMISSIONED) {
                    badVms.add(vm);
                    iter.remove();
                    poolChanged = true;
                }
            }
            currentPoolSize = vmPool.size() + numOrderedVms;
            numNewVmsNeeded = vmPoolSize - currentPoolSize;
            numOrderedVms += numNewVmsNeeded;
            if (poolChanged) {
                log.info("Removed failed or decomissioned VMs. Persisting changed VM Pool");
                try {
                    persistor.saveVmPool(new HashSet<>(vmPool));
                } catch (IOException e) {
                    log.error("Coudl not persist VM pool: " + e.getMessage(), e);
                }
            }
        }

        final int delta = numNewVmsNeeded;

        if (delta < 0) {
            retireVms(-delta);
        } else if (delta > 0) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    createVmsAsync(delta);
                }
            });
        }
        terminateVmAsync(badVms);
    }

    private void terminateVmAsync(final ConcurrentLinkedQueue<WpsVm> badVms) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                for (WpsVm wpsVm : badVms) {
                    terminateVm(wpsVm);
                }
            }
        });
    }

    private void createVmsAsync(int delta) {
        for (int i = 0; i < delta; i++) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    createVm();
                }
            });
        }
    }

    protected void createVm() {
        WpsVm vm;
        VmStatus state;
        try {
            log.info("Trying to start new VM");
            vm = startVmOnCloudNova();
            log.info(" VM " + vm.getId() + " (" + vm.getIpAddress()
                    + ") has started. Getting status...");
            state = vm.getOrWaitForStableState();
            log.info(" VM " + vm.getId() + " (" + vm.getIpAddress()
                    + ") reached stable state: " + state.toString());
        } catch (Exception e1) {
            synchronized (vmPool) {
                numOrderedVms--;
            }
            log.error("Could not create VM: " + e1.getMessage(), e1);
            return;
        }

        switch (state) {
        case READY: {
            synchronized (vmPool) {
                log.info(" VM " + vm.getId() + " (" + vm.getIpAddress()
                        + ") ready for use. Adding to VM pool.");
                vmPool.add(vm);
                numOrderedVms--;
                if (persistor != null) {
                    try {
                        log.info("Persisting VMPool state...");
                        persistor.saveVmPool(new HashSet<>(vmPool));
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }

                }
            }
            break;
        }
        case FAILED:
            synchronized (vmPool) {
                numOrderedVms--;
            }
            terminateVm(vm);
        default:
            log.error("VM in inconsisten state: " + state.toString());
        }
    }

    private void terminateVm(WpsVm vm) {
        log.error("Terminating VM not implemented yet. Can't terminate: "
                + vm.getId());
    }

    private void retireVms(int i) {
        // TODO Implement VmPool#retireVms()
    }

    public static void main(String[] arg) {
        VmPool pool = new VmPool(new FileVmPoolPersister(),
                "GeophysicsVL:Carsten.Friedrich@csiro.au", "",
                new ThreadPoolExecutor(10, 20, 60, TimeUnit.SECONDS,
                        new LinkedBlockingDeque<Runnable>()));
        try {
            WpsVm vm = pool.getFreeVm();
            vm.updateStatus();
            System.out.println("VM Status: " + vm.getStatus());
        } catch (PortalServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        System.exit(0);
    }

    private WpsVm startVmOnCloud() throws PortalServiceException {
        TemplateOptions options = ((NovaTemplateOptions) computeService
                .templateOptions())
        // .availabilityZone("NCI")
        // .keyPairName(getKeypair())
                .securityGroups("all");

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
        res.setOrderTime(System.currentTimeMillis());
        res.setStatus(VmStatus.STARTING);
        return res;
    }

    private WpsVm startVmOnCloudNova() throws PortalServiceException {
        for (String location : lowLevelApi.getConfiguredZones()) {
            Optional<? extends AvailabilityZoneApi> serverApi = lowLevelApi
                    .getAvailabilityZoneApi(location);
            Iterable<? extends AvailabilityZone> zones = serverApi.get().list();

            log.info(String.format("Trying location '%1$s' ...", location));

            for (AvailabilityZone currentZone : zones) {
                if (skippedZones.contains(currentZone.getName())) {
                    log.info(String.format(
                            "skipping: '%1$s' - configured as a skipped zone",
                            currentZone.getName()));
                    continue;
                }

                log.info(String.format("Trying zone '%1$s' ...",
                        currentZone.getName()));

                if (!currentZone.getState().available()) {
                    log.info(String.format("skipping: '%1$s' - not available",
                            currentZone.getName()));
                    continue;
                }
                TemplateOptions options = ((NovaTemplateOptions) computeService
                        .templateOptions()).availabilityZone(
                        currentZone.getName())
                // .keyPairName(getKeypair())
                        .securityGroups("all");

                Template template = computeService.templateBuilder()
                        .imageId(VM_ID).hardwareId(INSTANCE_TYPE)
                        .options(options).build();

                // Start up the job, we should have exactly 1 node start
                Set<? extends NodeMetadata> results;
                try {
                    results = computeService.createNodesInGroup(groupName, 1,
                            template);
                } catch (RunNodesException e) {
                    log.error(String.format(
                            "An unexpected error '%1$s' occured.'",
                            e.getMessage()));
                    log.error(
                            "An unexpected error has occured while staring VM: "
                                    + e.getMessage(), e);
                    continue;
                }
                if (results.isEmpty()) {
                    log.error("JClouds returned an empty result set. Treating it as job failure.");
                    throw new PortalServiceException(
                            "Unable to start compute node due to an unknown error, no nodes returned");
                }
                NodeMetadata result = results.iterator().next();
                String ipAddress = result.getPublicAddresses().iterator()
                        .next();
                log.info(result.getId() + ": " + ipAddress);
                WpsVm res = new WpsVm(result.getId(), ipAddress);
                res.setOrderTime(System.currentTimeMillis());
                res.setStatus(VmStatus.STARTING);
                return res;
            }
        }
        throw new PortalServiceException("Cloud unavailable...");
    }

    protected String getKeypair() {
        return keypair != null ? keypair : "vgl-developers";
    }

    protected void setKeypair(String keypair) {
        this.keypair = keypair;
    }

    public void verifyVm(WpsServiceClient wpsClient) {
        synchronized (vmPool) {
            WpsVm vm = findVm(wpsClient.getEndpoint());
            if (vm == null)
                return;
            //
            // Temporarily remove VM for testing
            //
            vmPool.remove(vm);
            numOrderedVms++;
            //
            // Test VM and add again if ok
            // If not ok, new VM will be requested automatically
            //
            vm.setStatus(VmStatus.UNKNOWN);
            executor.submit(new VerifyVmAndCheckPoolTask(vm));
        }

    }

    private WpsVm findVm(String endpoint) {
        synchronized (vmPool) {
            for (WpsVm vm : vmPool) {
                if (endpoint.contains(vm.getServiceUrl())) {
                    return vm;
                }
            }
        }
        return null;
    }

}
