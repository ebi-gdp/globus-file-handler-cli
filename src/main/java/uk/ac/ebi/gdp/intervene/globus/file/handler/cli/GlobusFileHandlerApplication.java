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
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParameters;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser;

import java.io.IOException;
import java.util.Optional;

import static java.lang.System.exit;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ApplicationStatus.INPUT_PROCESSING_ERROR;

@SpringBootApplication
public class GlobusFileHandlerApplication {
    public static void main(final String... args) throws IOException {
        final Optional<CLIParameters> cliParameters = CLIParser.parse(args);

        if (cliParameters.isPresent()) {
            new SpringApplicationBuilder(GlobusFileHandlerApplication.class)
                    .initializers((applicationContext) ->
                            applicationContext
                                    .getBeanFactory()
                                    .registerSingleton(
                                            "cliParameters",
                                            cliParameters.get()))
                    .run(args);
        } else {
            exit(INPUT_PROCESSING_ERROR.getValue());
        }
    }
}
