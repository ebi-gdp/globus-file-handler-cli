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

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.download.GlobusFileDownloader;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.download.IGlobusFileDownloader;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParameters;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.runner.GlobusFileHandlerCommandLineRunner;

@Configuration
public class GlobusFileHandlerCliConfig {

    @Bean
    public IGlobusFileDownloader globusFileDownloader(final WebClient webClient,
                                                      final RetryTemplate retryTemplate,
                                                      final WebClientProperties webClientProperties) {
        return new GlobusFileDownloader(
                webClient,
                retryTemplate,
                webClientProperties.getPipeSize());
    }

    @Bean
    public GlobusFileHandlerCommandLineRunner globusFileHandlerCommandLineRunner(final ApplicationContext applicationContext,
                                                                                 final IGlobusFileDownloader globusFileDownloader,
                                                                                 final CLIParameters cliParameters) {
        return new GlobusFileHandlerCommandLineRunner(
                applicationContext,
                globusFileDownloader,
                cliParameters);
    }
}
