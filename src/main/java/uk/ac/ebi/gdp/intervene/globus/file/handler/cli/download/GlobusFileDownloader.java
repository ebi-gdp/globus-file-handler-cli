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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.download;

import org.apache.commons.io.IOUtils;
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

import static uk.ac.ebi.gdp.file.handler.core.utils.Checksum.getMD5MessageDigest;

public class GlobusFileDownloader implements IGlobusFileDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobusFileDownloader.class);
    private final WebClient webClient;
    private final RetryTemplate retryTemplate;
    private final int pipeSize;

    public GlobusFileDownloader(final WebClient webClient,
                                final RetryTemplate retryTemplate,
                                final int pipeSize) {
        this.webClient = webClient;
        this.retryTemplate = retryTemplate;
        this.pipeSize = pipeSize;
    }

    @Override
    public ApplicationStatus downloadFile(final Path fileDownloadSourcePath,
                                          final Path fileDownloadDestinationPath,
                                          final long fileSize,
                                          final ProgressListener progressListener) {
        final Path fileToBeDownloadedName = fileDownloadSourcePath.getFileName();
        final File destinationFile = fileDownloadDestinationPath.resolve(fileToBeDownloadedName).toFile();
        try {
            LOGGER.info("File download process started for {}", fileDownloadSourcePath);
            doDownloadFile(fileDownloadSourcePath, destinationFile, fileSize, progressListener);
            LOGGER.info("File download process completed for {}", fileDownloadSourcePath);
        } catch (IOException e) {
            LOGGER.error("Error while downloading file %s".formatted(fileToBeDownloadedName), e);
            return ApplicationStatus.APPLICATION_FAILED;
        }
        return ApplicationStatus.SUCCESS;
    }

    private void doDownloadFile(final Path fileDownloadSourcePath,
                                final File destinationFile,
                                final long fileSize,
                                final ProgressListener progressListener) throws IOException {
        final MessageDigest messageDigest = getMD5MessageDigest();
        try (final OutputStream os = new DigestOutputStream(
                new ProgressListenerOutputStream(
                        new FileOutputStream(destinationFile), progressListener),
                messageDigest);
             final InputStream in = doGetDownloadInputStream(fileDownloadSourcePath, fileSize)) {
            IOUtils.copyLarge(in, os);
        }
        //final String downloadedFileMD5 = normalize(messageDigest);//TODO use md5 to validate file
        LOGGER.info("File {} has been successfully downloaded at {}", fileDownloadSourcePath.getFileName(), destinationFile.getAbsolutePath());
    }

    private InputStream doGetDownloadInputStream(final Path downloadLocation,
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
