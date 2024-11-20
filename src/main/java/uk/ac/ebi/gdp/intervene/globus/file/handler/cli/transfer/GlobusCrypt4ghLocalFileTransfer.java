/*
 *
 * Copyright 2024 EMBL - European Bioinformatics Institute
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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.transfer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.file.handler.core.listener.ProgressListener;
import uk.ac.ebi.gdp.file.handler.core.stream.ProgressListenerOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.io.IOUtils.copy;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.exception.SystemException.systemException;

public class GlobusCrypt4ghLocalFileTransfer extends DefaultGlobusFileTransfer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobusCrypt4ghLocalFileTransfer.class);
    private final Crypt4gh crypt4gh;
    private final List<String> shellInterpreterCmd;

    public GlobusCrypt4ghLocalFileTransfer(final WebClient webClient,
                                           final RetryTemplate retryTemplate,
                                           final int pipeSize,
                                           final Crypt4gh crypt4gh,
                                           final List<String> shellInterpreterCmd,
                                           final int bufferSize) {
        super(webClient, retryTemplate, pipeSize, bufferSize);
        this.crypt4gh = crypt4gh;
        this.shellInterpreterCmd = shellInterpreterCmd;
    }

    @Override
    public void doDownloadFile(final URI downloadFileSource,
                               final URI destinationFile,
                               final long fileSize,
                               final ProgressListener progressListener) throws Exception {
        LOGGER.info("Establishing connection for Globus InputStream");
        try (final InputStream globusDownloadInputStream = getGlobusDownloadInputStream(downloadFileSource, fileSize)) {
            final ProcessBuilder processBuilder = processBuilder(crypt4gh.crypt4ghDecryptBashCmd(destinationFile));

            LOGGER.info("Bash process is about to start");
            final Process process = processBuilder.start();
            LOGGER.info("Bash process is running");

            try (final OutputStream progressListenerOutputStream = new ProgressListenerOutputStream(process.getOutputStream(),
                    progressListener)) {
                LOGGER.info("Copying Globus InputStream to stdin");
                copy(globusDownloadInputStream, progressListenerOutputStream, bufferSize);
            }

            // Wait for the script to finish
            final int exitCode = process.waitFor();

            if (exitCode == 0) {
                LOGGER.info("File {} has been successfully downloaded at {}",
                        downloadFileSource.getPath(), destinationFile.getPath());
            } else {
                LOGGER.error("Process finished with exit-code: {}", exitCode);
                throw systemException("Unable to download file! Process builder terminated with error code %s".formatted(exitCode));
            }
        } finally {
            LOGGER.info("Delete secret key if present: {}", crypt4gh.deleteSecKey());
        }
    }

    private ProcessBuilder processBuilder(final String crypt4ghBashCmd) {
        final ProcessBuilder processBuilder = new ProcessBuilder();
        final List<String> crypt4ghExecutableCmds = new ArrayList<>(shellInterpreterCmd);
        crypt4ghExecutableCmds.add(crypt4ghBashCmd);
        processBuilder.command(crypt4ghExecutableCmds);
        return processBuilder;
    }
}
