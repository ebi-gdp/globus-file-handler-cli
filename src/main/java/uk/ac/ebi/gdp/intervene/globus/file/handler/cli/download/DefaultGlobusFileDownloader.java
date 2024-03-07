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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.download;

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
import java.nio.file.Path;
import java.security.DigestOutputStream;
import java.security.MessageDigest;

import static org.apache.commons.io.IOUtils.copy;
import static uk.ac.ebi.gdp.file.handler.core.utils.Checksum.getMD5MessageDigest;
import static uk.ac.ebi.gdp.file.handler.core.utils.Checksum.normalize;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ApplicationStatus.APPLICATION_FAILED;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ApplicationStatus.SUCCESS;

public class DefaultGlobusFileDownloader implements IGlobusFileDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGlobusFileDownloader.class);
    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    private final int pipeSize;
    protected final int bufferSize;

    public DefaultGlobusFileDownloader(final WebClient webClient,
                                       final RetryTemplate retryTemplate,
                                       final int pipeSize,
                                       final int bufferSize) {
        this.webClient = webClient;
        this.retryTemplate = retryTemplate;
        this.pipeSize = pipeSize;
        this.bufferSize = bufferSize;
    }

    @Override
    public ApplicationStatus downloadFile(final Path downloadFileSourcePath,
                                          final Path downloadFileDestinationPath,
                                          final long fileSize,
                                          final ProgressListener progressListener) {
        final Path fileToBeDownloadedName = downloadFileSourcePath.getFileName();
        final Path destinationFilePath = downloadFileDestinationPath.resolve(fileToBeDownloadedName);
        try {
            LOGGER.info("File download process started for {}", downloadFileSourcePath);
            doDownloadFile(downloadFileSourcePath, destinationFilePath, fileSize, progressListener);
            LOGGER.info("File download process completed for {}", downloadFileSourcePath);
        } catch (IOException e) {
            LOGGER.error("Error while downloading file %s".formatted(fileToBeDownloadedName), e);
            return APPLICATION_FAILED;
        }
        return SUCCESS;
    }

    protected void doDownloadFile(final Path downloadFileSourcePath,
                                  final Path destinationFilePath,
                                  final long fileSize,
                                  final ProgressListener progressListener) throws IOException {
        final MessageDigest messageDigest = getMD5MessageDigest();
        final File destinationFile = destinationFilePath.toFile();
        try (final OutputStream digestOutputStream = new DigestOutputStream(
                new ProgressListenerOutputStream(
                        new FileOutputStream(destinationFile), progressListener),
                messageDigest);
             final InputStream globusDownloadInputStream = getDownloadInputStream(downloadFileSourcePath, fileSize)) {
            copy(globusDownloadInputStream, digestOutputStream, bufferSize);
        }
        final String downloadedFileMD5 = normalize(messageDigest);
        LOGGER.info("File {} has been successfully downloaded at {}, MD5: {}",
                downloadFileSourcePath.getFileName(), destinationFile.getAbsolutePath(), downloadedFileMD5);
    }

    protected InputStream getDownloadInputStream(final Path downloadLocation,
                                                 final long fileSize) throws IOException {
        return new RetryInputStream(
                webClient,
                retryTemplate,
                downloadLocation,
                0,
                fileSize - 1,
                pipeSize
        );
    }
}
