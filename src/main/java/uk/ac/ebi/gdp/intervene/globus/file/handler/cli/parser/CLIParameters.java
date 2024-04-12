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

import joptsimple.OptionSet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static java.net.URI.create;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.CRYPT4GH_OPTION;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.CRYPT4GH_PRIVATE_KEY_PATH_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_DESTINATION_PATH_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_DESTINATION_PATH_SHORT;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_FILE_SIZE_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_SOURCE_PATH_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_SOURCE_PATH_SHORT;

public class CLIParameters {
    private final String fileDownloadSourceLocation;
    private final String fileDownloadDestinationLocation;
    private final Long fileSize;
    private final Boolean isCrypt4ghEnabled;
    private final String crypt4ghPrivateKeyPath;

    public CLIParameters(final OptionSet optionSet) throws IOException {
        this.fileDownloadSourceLocation = extractFileDownloadPathSource(optionSet);
        this.fileDownloadDestinationLocation = extractFileDownloadPathDestination(optionSet);
        this.fileSize = Long.valueOf(optionSet.valueOf(GLOBUS_FILE_TRANSFER_FILE_SIZE_LONG).toString());
        this.isCrypt4ghEnabled = optionSet.has(CRYPT4GH_OPTION);
        this.crypt4ghPrivateKeyPath = extractCrypt4ghPrivateKeyPath(optionSet);
    }

    private String extractFileDownloadPathSource(final OptionSet optionSet) {
        return (optionSet.hasArgument(GLOBUS_FILE_TRANSFER_SOURCE_PATH_SHORT) ?
                optionSet.valueOf(GLOBUS_FILE_TRANSFER_SOURCE_PATH_SHORT) : optionSet.valueOf(GLOBUS_FILE_TRANSFER_SOURCE_PATH_LONG)).toString();
    }

    private String extractFileDownloadPathDestination(final OptionSet optionSet) throws IOException {
        return validateFileDownloadDestinationPath((
                optionSet.hasArgument(GLOBUS_FILE_TRANSFER_DESTINATION_PATH_SHORT) ?
                        optionSet.valueOf(GLOBUS_FILE_TRANSFER_DESTINATION_PATH_SHORT) : optionSet.valueOf(GLOBUS_FILE_TRANSFER_DESTINATION_PATH_LONG)).toString());
    }

    private String extractCrypt4ghPrivateKeyPath(final OptionSet optionSet) {
        return optionSet.hasArgument(CRYPT4GH_PRIVATE_KEY_PATH_LONG) ? optionSet.valueOf(CRYPT4GH_PRIVATE_KEY_PATH_LONG).toString() : null;
    }

    public String getFileDownloadSourceLocation() {
        return fileDownloadSourceLocation;
    }

    public String getFileDownloadDestinationLocation() {
        return fileDownloadDestinationLocation;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Boolean isCrypt4ghEnabled() {
        return isCrypt4ghEnabled;
    }

    public String getCrypt4ghPrivateKeyPath() {
        return crypt4ghPrivateKeyPath;
    }

    private String validateFileDownloadDestinationPath(final String fileDownloadDestination) throws IOException {
        final File fileDownloadDestinationFile = Path.of(create(fileDownloadDestination).getPath())
                .normalize()
                .getParent()
                .toAbsolutePath()
                .toFile();
        if (!fileDownloadDestinationFile.exists() && !fileDownloadDestinationFile.mkdirs()) {
            throw new IOException("Output directory path doesn't exists/ Unable to create destination directory.");
        }
        return fileDownloadDestination;
    }
}
