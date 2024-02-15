/*
 *
 * Copyright 2023 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import uk.ac.ebi.gdp.file.handler.core.listener.ProgressListener;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BytesTransferredListener implements ProgressListener {
    private final Logger LOGGER = LoggerFactory.getLogger(BytesTransferredListener.class);
    private static final float BYTES_TO_MIB = (1024 * 1024);
    private static final long PERIOD = 10;
    private final String filename;
    private long lastTotalBytes;
    private long totalBytes;

    public BytesTransferredListener(final String filename,
                                    final ScheduledExecutorService scheduledExecutorService) {
        lastTotalBytes = 0;
        totalBytes = 0;
        this.filename = filename;
        new StopWatch(filename).start();
        scheduledExecutorService.scheduleAtFixedRate(this::report, 1, PERIOD, TimeUnit.SECONDS);
    }

    @Override
    public void progress(final long bytesTransferred) {
        totalBytes = bytesTransferred;
    }

    public void report() {
        long currentBytes = totalBytes;
        float speed = ((float) (currentBytes - lastTotalBytes) / BYTES_TO_MIB) / PERIOD;
        LOGGER.info("File download progress: {}, Transfer rate: {} MiBs", filename, speed);
        lastTotalBytes = currentBytes;
    }
}
