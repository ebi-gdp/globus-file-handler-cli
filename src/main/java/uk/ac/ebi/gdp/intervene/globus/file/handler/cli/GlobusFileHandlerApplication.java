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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParameters;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIPropertySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.System.exit;
import static java.util.Arrays.asList;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ApplicationStatus.INPUT_PROCESSING_ERROR;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ProfileType.CRYPT4GH_SECRET_KEY_LOCAL;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ProfileType.CRYPT4GH_SECRET_KEY_SECRET_MANAGER;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ProfileType.DEFAULT_PLAIN;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.parse;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.transfer.Crypt4gh.SECRET_DETAILS_FILE_SUFFIX;

@SpringBootApplication
public class GlobusFileHandlerApplication {

    public static void main(final String... args) throws IOException {
        final Optional<CLIParameters> cliParameters = parse(args);

        if (cliParameters.isPresent()) {
            final PropertySource<CLIParameters> propertySource = new CLIPropertySource("cliPropertySource", cliParameters.get());
            new SpringApplicationBuilder(GlobusFileHandlerApplication.class)
                    .initializers((applicationContext) -> {
                        applicationContext
                                .getEnvironment()
                                .getPropertySources()
                                .addLast(propertySource);
                        setActiveProfile(cliParameters.get(), applicationContext.getEnvironment());
                    })
                    .run(args);
        } else {
            exit(INPUT_PROCESSING_ERROR.getValue());
        }
    }

    private static void setActiveProfile(final CLIParameters parameters,
                                         final ConfigurableEnvironment configurableEnvironment) {
        final String[] activeProfilesArray = configurableEnvironment.getActiveProfiles();
        final List<String> activeProfilesList = new ArrayList<>(activeProfilesArray.length);
        activeProfilesList.addAll(asList(activeProfilesArray));

        if (parameters.isCrypt4ghEnabled()) {
            activeProfilesList.add(fetchCrypt4ghProfile(parameters));
        } else {
            activeProfilesList.add(DEFAULT_PLAIN);
        }
        configurableEnvironment
                .setActiveProfiles(activeProfilesList.toArray(String[]::new));
    }

    private static String fetchCrypt4ghProfile(final CLIParameters parameters) {
        if (parameters.getCrypt4ghPrivateKeyPath()
                .endsWith(SECRET_DETAILS_FILE_SUFFIX)) {
            return CRYPT4GH_SECRET_KEY_SECRET_MANAGER;
        } else {
            return CRYPT4GH_SECRET_KEY_LOCAL;
        }
    }
}
