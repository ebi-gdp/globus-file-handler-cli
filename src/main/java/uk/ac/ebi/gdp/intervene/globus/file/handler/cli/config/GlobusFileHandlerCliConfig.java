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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.download.DefaultGlobusFileDownloader;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.download.IGlobusFileDownloader;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.runner.GlobusFileHandlerCommandLineRunner;

import java.nio.file.Path;

import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ProfileType.CRYPT4GH;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_DOWNLOAD_DESTINATION_PATH_SHORT;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_DOWNLOAD_FILE_SIZE_SHORT;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_DOWNLOAD_SOURCE_PATH_SHORT;

@Configuration
public class GlobusFileHandlerCliConfig {

    @Profile("!" + CRYPT4GH)
    @Bean
    public IGlobusFileDownloader defaultGlobusFileDownloader(final WebClient webClient,
                                                             final RetryTemplate retryTemplate,
                                                             final WebClientProperties webClientProperties,
                                                             @Value("${data.copy.buffer-size:8192}") final int bufferSize) {
        return new DefaultGlobusFileDownloader(
                webClient,
                retryTemplate,
                webClientProperties.getPipeSize(),
                bufferSize);
    }

    @Bean
    public GlobusFileHandlerCommandLineRunner globusFileHandlerCLRunner(final ApplicationContext applicationContext,
                                                                        final IGlobusFileDownloader globusFileDownloader,
                                                                        @Value("${" + GLOBUS_FILE_DOWNLOAD_SOURCE_PATH_SHORT + "}") final Path fileDownloadSourcePath,
                                                                        @Value("${" + GLOBUS_FILE_DOWNLOAD_DESTINATION_PATH_SHORT + "}") final Path fileDownloadDestinationPath,
                                                                        @Value("${" + GLOBUS_FILE_DOWNLOAD_FILE_SIZE_SHORT + "}") final long fileSize) {
        return new GlobusFileHandlerCommandLineRunner(
                applicationContext,
                globusFileDownloader,
                fileDownloadSourcePath,
                fileDownloadDestinationPath,
                fileSize);
    }
}
