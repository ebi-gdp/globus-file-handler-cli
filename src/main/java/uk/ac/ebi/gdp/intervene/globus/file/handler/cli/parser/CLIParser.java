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
    public static final String GLOBUS_FILE_TRANSFER_SOURCE_PATH_SHORT = "s";
    public static final String GLOBUS_FILE_TRANSFER_SOURCE_PATH_LONG = "globus_file_transfer_source_path";
    public static final String GLOBUS_FILE_TRANSFER_DESTINATION_PATH_SHORT = "d";
    public static final String GLOBUS_FILE_TRANSFER_DESTINATION_PATH_LONG = "globus_file_transfer_destination_path";
    public static final String GLOBUS_FILE_TRANSFER_FILE_SIZE_SHORT = "l";
    public static final String GLOBUS_FILE_TRANSFER_FILE_SIZE_LONG = "file_size";
    public static final String CRYPT4GH_OPTION = "crypt4gh";
    public static final String CRYPT4GH_PRIVATE_KEY_PATH_SHORT = "sk";
    public static final String CRYPT4GH_PRIVATE_KEY_PATH_LONG = "private_key";
    public static final String OPTIONS_HELP = "h";
    private static final OptionParser optionParser = buildParser();

    private static OptionParser buildParser() {
        final OptionParser parser = new OptionParser();
        parser.accepts(OPTIONS_HELP, "Use this option to display help");
        parser.acceptsAll(List.of(GLOBUS_FILE_TRANSFER_SOURCE_PATH_SHORT, GLOBUS_FILE_TRANSFER_SOURCE_PATH_LONG), "Globus file download path/uri (source)")
                .requiredUnless(OPTIONS_HELP)
                .withRequiredArg()
                .ofType(String.class);
        parser.acceptsAll(List.of(GLOBUS_FILE_TRANSFER_DESTINATION_PATH_SHORT, GLOBUS_FILE_TRANSFER_DESTINATION_PATH_LONG), "Globus file download path (destination)")
                .requiredUnless(OPTIONS_HELP)
                .withRequiredArg()
                .ofType(String.class);
        parser.acceptsAll(List.of(GLOBUS_FILE_TRANSFER_FILE_SIZE_SHORT, GLOBUS_FILE_TRANSFER_FILE_SIZE_LONG), "File size")
                .requiredUnless(OPTIONS_HELP)
                .withRequiredArg()
                .ofType(Long.class);
        parser.accepts(CRYPT4GH_OPTION, "Crypt4gh decryption! Use this option to decrypt file encrypted by Crypt4gh")
                .availableUnless(OPTIONS_HELP);
        parser.acceptsAll(List.of(CRYPT4GH_PRIVATE_KEY_PATH_SHORT, CRYPT4GH_PRIVATE_KEY_PATH_LONG), "Crypt4gh private key path")
                .requiredIf(CRYPT4GH_OPTION)
                .withRequiredArg()
                .ofType(String.class);
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
