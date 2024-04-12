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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.runner;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.listener.BytesTransferredListener;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.transfer.IGlobusFileTransfer;

import java.net.URI;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.lang.System.exit;

public class GlobusFileHandlerCommandLineRunner implements ApplicationRunner {
    private final ApplicationContext applicationContext;
    private final IGlobusFileTransfer globusFileDownloader;
    private final URI fileDownloadSource;
    private final URI fileDownloadDestination;
    private final long fileSize;

    public GlobusFileHandlerCommandLineRunner(final ApplicationContext applicationContext,
                                              final IGlobusFileTransfer globusFileDownloader,
                                              final URI fileDownloadSource,
                                              final URI fileDownloadDestination,
                                              final long fileSize) {
        this.applicationContext = applicationContext;
        this.globusFileDownloader = globusFileDownloader;
        this.fileDownloadSource = fileDownloadSource;
        this.fileDownloadDestination = fileDownloadDestination;
        this.fileSize = fileSize;
    }

    @Override
    public void run(ApplicationArguments args) {
        terminateApplication(() -> globusFileDownloader
                .downloadFile(
                        fileDownloadSource,
                        fileDownloadDestination,
                        fileSize,
                        new BytesTransferredListener(
                                fileDownloadSource.getPath(),
                                new ScheduledThreadPoolExecutor(1)))
                .getValue());
    }

    private void terminateApplication(final ExitCodeGenerator exitCodeGenerator) {
        exit(SpringApplication.exit(applicationContext, exitCodeGenerator));
    }
}
