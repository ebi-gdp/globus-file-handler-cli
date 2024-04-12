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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.transfer.Crypt4gh;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.transfer.GlobusCrypt4ghLocalFileTransfer;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.transfer.IGlobusFileTransfer;

import java.nio.file.Path;
import java.util.List;

import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ProfileType.CRYPT4GH;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.CRYPT4GH_PRIVATE_KEY_PATH_LONG;

@Profile(CRYPT4GH)
@Configuration
public class Crypt4ghConfig {

    @Bean
    public IGlobusFileTransfer crypt4ghGlobusFileTransfer(final WebClient webClient,
                                                          final RetryTemplate retryTemplate,
                                                          final WebClientProperties webClientProperties,
                                                          final Crypt4gh crypt4gh,
                                                          @Value("#{'${crypt4gh.shell-path}'.split(' ')}") final List<String> shellInterpreterCmds,
                                                          @Value("${data.copy.buffer-size:8192}") final int bufferSize) {
        return new GlobusCrypt4ghLocalFileTransfer(
                webClient,
                retryTemplate,
                webClientProperties.getPipeSize(),
                crypt4gh,
                shellInterpreterCmds,
                bufferSize);
    }

    @Bean
    public Crypt4gh crypt4gh(@Value("${crypt4gh.binary-path}") final Path binPath,
                             @Value("${" + CRYPT4GH_PRIVATE_KEY_PATH_LONG + "}") final Path privateKeyPath) {
        return new Crypt4gh(
                binPath,
                privateKeyPath);
    }
}
