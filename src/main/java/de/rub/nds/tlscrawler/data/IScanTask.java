/**
 * TLS Crawler
 *
 * Licensed under Apache 2.0
 *
 * Copyright 2017 Ruhr-University Bochum
 */
package de.rub.nds.tlscrawler.data;

import java.time.Instant;
import java.util.Collection;
import org.bson.Document;

/**
 * Scan task interface. Interface to the document that will eventually be
 * persisted.
 *
 * @author janis.fliegenschmidt@rub.de
 */
public interface IScanTask {

    /**
     * @return The id of the scan task.
     */
    String getId();

    /**
     * Returns the ID of the master which created the scan task.
     *
     * @return The id of the master instance.
     */
    String getInstanceId();

    /**
     * @return The point in time a slave started this task.
     */
    Instant getStartedTimestamp();

    /**
     * @return The point in time a slave accepted this task.
     */
    Instant getAcceptedTimestamp();

    /**
     * @return The point in time the slave finished working on the task.
     */
    Instant getCompletedTimestamp();

    /**
     * @return A list of scans to be performed.
     */
    Collection<String> getScans();

    /**
     * @return Returns the scan target.
     */
    IScanTarget getScanTarget();

    /**
     * @return Returns the scan results.
     */
    Document getResult();

    /**
     * Sets the result of the ScanTask
     *
     * @param result the result to set
     */
    void setResult(Document result);

    /**
     * Sets the instant when the ScanTask was completed
     *
     * @param instant the instant the ScanTask was completed
     */
    void setCompletedTimestamp(Instant instant);
}
