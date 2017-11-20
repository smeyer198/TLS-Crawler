/**
 * TLS Crawler
 *
 * Licensed under Apache 2.0
 *
 * Copyright 2017 Ruhr-University Bochum
 */
package de.rub.nds.tlscrawler.core;

import de.rub.nds.tlscrawler.data.*;
import de.rub.nds.tlscrawler.scans.IScan;
import de.rub.nds.tlscrawler.orchestration.IOrchestrationProvider;
import de.rub.nds.tlscrawler.persistence.IPersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * Implements Scan Task Creation.
 *
 * @author janis.fliegenschmidt@rub.de
 */
public class TLSCrawlerMaster extends TLSCrawler {
    private static Logger LOG = LoggerFactory.getLogger(TLSCrawlerMaster.class);

    /**
     * TLS-Crawler master constructor.
     *
     * @param orchestrationProvider A non-null orchestration provider.
     * @param persistenceProvider A non-null persistence provider.
     * @param scans A neither null nor empty list of available scans.
     */
    public TLSCrawlerMaster(IOrchestrationProvider orchestrationProvider, IPersistenceProvider persistenceProvider, List<IScan> scans) {
        super(orchestrationProvider, persistenceProvider, scans);
    }

    public void crawl(List<String> scans, List<String> targets, List<Integer> ports) {
        if (areNotValidArgs(scans, targets, ports)) {
            LOG.error("Crawling task has not been established due to invalid arguments.");
        }

        // TODO: This should be parallelized.

        for (String target : targets) {
            String scanId = UUID.randomUUID().toString();

            IScanTask newTask = new ScanTask(
                    scanId,
                    Instant.now(),
                    null,
                    null,
                    null,
                    target,
                    ports,
                    scans);

            this.getPersistenceProvider().setUpScanTask(newTask);
            this.getOrchestrationProvider().addScanTask(newTask.getId());
        }
    }

    public IMasterStats getStats() {
        IPersistenceProviderStats ppStats = this.getPersistenceProvider().getStats();

        return new MasterStats(ppStats.getTotalTasks(),
                ppStats.getFinishedTasks(),
                ppStats.getEarliestCompletionTimestamp(),
                ppStats.getEarliestCreatedTimestamp());
    }

    private boolean areNotValidArgs(List<String> scans, List<String> targets, List<Integer> ports) {
        List<String> invalidScans = scans.stream().filter(x -> !this.getScanNames().contains(x)).collect(Collectors.toList());
        List<String> invalidTargetIps = targets.stream().filter(x -> !isValidIp(x)).collect(Collectors.toList());
        List<Integer> invalidPorts = ports.stream().filter(x -> x < 1 || x > 65535).collect(Collectors.toList());

        boolean allScansValid = invalidScans.isEmpty();
        boolean allTargetIpsValid = invalidTargetIps.isEmpty();
        boolean allPortsValid = invalidPorts.isEmpty();

        if (!allScansValid) {
            String invalidScanList = invalidScans.stream().map(item -> "'" + item + "'").collect(joining(" "));
            LOG.error(String.format("Invalid Scans: %s", invalidScanList));
        }

        if (!allTargetIpsValid) {
            String invalidTargetIpList = invalidTargetIps.stream().map(item -> "'" + item + "'").collect(joining(" "));
            LOG.error(String.format("Invalid Target IPs: %s", invalidTargetIpList));
        }

        if (!allPortsValid) {
            String invalidPortsList = invalidPorts.stream().map(item -> "'" + item + "'").collect(joining(" "));
            LOG.error(String.format("Invalid Ports: %s", invalidPortsList));
        }

        return !(allScansValid && allTargetIpsValid && allPortsValid);
    }

    private boolean isValidIp(String ip) {
        // TODO: There's gotta be a better way.
        return true;
    }
}
