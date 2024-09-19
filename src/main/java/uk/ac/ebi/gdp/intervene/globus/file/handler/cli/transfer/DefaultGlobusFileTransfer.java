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
import uk.ac.ebi.gdp.file.handler.core.stream.RetryInputStream;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ApplicationStatus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import static java.nio.file.Paths.get;
import static org.apache.commons.io.IOUtils.copy;
import static uk.ac.ebi.gdp.file.handler.core.utils.Checksum.getMD5MessageDigest;
import static uk.ac.ebi.gdp.file.handler.core.utils.Checksum.normalize;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ApplicationStatus.APPLICATION_FAILED;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ApplicationStatus.SUCCESS;

public class DefaultGlobusFileTransfer implements IGlobusFileTransfer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGlobusFileTransfer.class);
    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    private final int pipeSize;
    protected final int bufferSize;

    public DefaultGlobusFileTransfer(final WebClient webClient,
                                     final RetryTemplate retryTemplate,
                                     final int pipeSize,
                                     final int bufferSize) {
        this.webClient = webClient;
        this.retryTemplate = retryTemplate;
        this.pipeSize = pipeSize;
        this.bufferSize = bufferSize;
    }

    @Override
    public ApplicationStatus downloadFile(final URI downloadFileSource,
                                          final URI downloadFileDestination,
                                          final long fileSize,
                                          final ProgressListener progressListener) {
        try {
            LOGGER.info("File download process started for {}", downloadFileSource.getPath());
            doDownloadFile(downloadFileSource, downloadFileDestination, fileSize, progressListener);
            LOGGER.info("File download process completed for {}", downloadFileSource.getPath());
        } catch (Exception e) {
            LOGGER.error("Error while downloading file %s. %s".formatted(downloadFileSource.getPath(), e.getMessage()), e);
            deleteOutputFile(downloadFileDestination);
            return APPLICATION_FAILED;
        }
        return SUCCESS;
    }

    private void deleteOutputFile(final URI downloadFileDestination) {
        try {
            Files.deleteIfExists(Path.of(downloadFileDestination));
        } catch (IOException e) {
            LOGGER.error("Unable to delete output file {}", downloadFileDestination.getPath());
        }
    }

    protected void doDownloadFile(final URI downloadFileSourceURI,
                                  final URI destinationFileURI,
                                  final long fileSize,
                                  final ProgressListener progressListener) throws Exception {
        final MessageDigest messageDigest = getMD5MessageDigest();
        final File destinationFile = get(destinationFileURI.getPath()).toFile();
        try (final OutputStream digestOutputStream = new DigestOutputStream(
                new ProgressListenerOutputStream(
                        new FileOutputStream(destinationFile), progressListener),
                messageDigest);
             final InputStream globusDownloadInputStream = getGlobusDownloadInputStream(downloadFileSourceURI, fileSize)) {
            copy(globusDownloadInputStream, digestOutputStream, bufferSize);
        }
        final String downloadedFileMD5 = normalize(messageDigest);
        LOGGER.info("File {} has been successfully downloaded at {}, MD5: {}",
                downloadFileSourceURI.getPath(), destinationFile.getAbsolutePath(), downloadedFileMD5);
    }

    protected InputStream getGlobusDownloadInputStream(final URI downloadFileSourceURI,
                                                       final long fileSize) throws IOException {
        return new RetryInputStream(
                webClient,
                retryTemplate,
                Path.of(downloadFileSourceURI.getPath()),
                0,
                fileSize - 1,
                pipeSize
        );
    }
}
