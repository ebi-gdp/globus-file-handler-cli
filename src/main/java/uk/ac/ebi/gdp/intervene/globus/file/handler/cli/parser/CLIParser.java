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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.lang.System.exit;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.constant.ApplicationStatus.SUCCESS;

public class CLIParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CLIParser.class);
    public static final String GLOBUS_FILE_DOWNLOAD_SOURCE_SHORT = "s";
    public static final String GLOBUS_FILE_DOWNLOAD_SOURCE_LONG = "globus_file_download_source_path";
    public static final String GLOBUS_FILE_DOWNLOAD_DESTINATION_SHORT = "d";
    public static final String GLOBUS_FILE_DOWNLOAD_DESTINATION_LONG = "globus_file_download_destination_path";
    public static final String GLOBUS_FILE_DOWNLOAD_FILE_SIZE = "file_size";
    public static final String OPTIONS_HELP = "h";
    private static final OptionParser optionParser = buildParser();

    private static OptionParser buildParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(OPTIONS_HELP, "Use this option to display help.");
        parser.acceptsAll(List.of(GLOBUS_FILE_DOWNLOAD_SOURCE_SHORT, GLOBUS_FILE_DOWNLOAD_SOURCE_LONG), "Globus file download path/uri (source). Required unless option help -h is provided!")
                .requiredUnless(OPTIONS_HELP)
                .withRequiredArg()
                .ofType(String.class);
        parser.acceptsAll(List.of(GLOBUS_FILE_DOWNLOAD_DESTINATION_SHORT, GLOBUS_FILE_DOWNLOAD_DESTINATION_LONG), "Globus file download path (destination). Required unless option help -h is provided!")
                .requiredUnless(OPTIONS_HELP)
                .withRequiredArg()
                .ofType(String.class);
        parser.acceptsAll(List.of(GLOBUS_FILE_DOWNLOAD_FILE_SIZE), "File size. Required unless option help -h is provided!")
                .requiredUnless(OPTIONS_HELP)
                .withRequiredArg()
                .ofType(Long.class);
        parser.allowsUnrecognizedOptions();
        return parser;
    }

    public static Optional<CLIParameters> parse(final String... args) throws IOException {
        try {
            final OptionSet optionSet = optionParser.parse(args);
            if (optionSet.has(OPTIONS_HELP)) {
                printHelp();
                exit(SUCCESS.getValue());
            }
            return of(new CLIParameters(optionSet));
        } catch (OptionException | IllegalArgumentException | IOException opExp) {
            LOGGER.error(opExp.getMessage());
        }
        printHelp();
        return empty();
    }

    private static void printHelp() throws IOException {
        optionParser.printHelpOn(System.out);
    }
}
