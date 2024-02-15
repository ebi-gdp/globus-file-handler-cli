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

import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_DOWNLOAD_DESTINATION_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_DOWNLOAD_DESTINATION_SHORT;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_DOWNLOAD_FILE_SIZE;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_DOWNLOAD_SOURCE_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_DOWNLOAD_SOURCE_SHORT;

public class CLIParameters {
    private final Path fileDownloadLocationSource;
    private final Path fileDownloadLocationDestination;
    private final long fileSize;

    public CLIParameters(final OptionSet optionSet) throws IOException {
        this.fileDownloadLocationSource = extractFileDownloadPathSource(optionSet);
        this.fileDownloadLocationDestination = extractFileDownloadPathDestination(optionSet);
        this.fileSize = (long) optionSet.valueOf(GLOBUS_FILE_DOWNLOAD_FILE_SIZE);
    }

    private Path extractFileDownloadPathSource(final OptionSet optionSet) {
        return Path.of((optionSet.hasArgument(GLOBUS_FILE_DOWNLOAD_SOURCE_SHORT) ? optionSet.valueOf(GLOBUS_FILE_DOWNLOAD_SOURCE_SHORT) : optionSet.valueOf(GLOBUS_FILE_DOWNLOAD_SOURCE_LONG)).toString());
    }

    private Path extractFileDownloadPathDestination(final OptionSet optionSet) throws IOException {
        return validateFileDownloadDestinationPath(Path.of((optionSet.hasArgument(GLOBUS_FILE_DOWNLOAD_DESTINATION_SHORT) ? optionSet.valueOf(GLOBUS_FILE_DOWNLOAD_DESTINATION_SHORT) : optionSet.valueOf(GLOBUS_FILE_DOWNLOAD_DESTINATION_LONG)).toString()));
    }

    public Path getFileDownloadLocationSource() {
        return fileDownloadLocationSource;
    }

    public Path getFileDownloadLocationDestination() {
        return fileDownloadLocationDestination;
    }

    public long getFileSize() {
        return fileSize;
    }

    private Path validateFileDownloadDestinationPath(final Path fileDownloadLocationDestinationPath) throws IOException {
        final File fileDownloadLocationDestination = fileDownloadLocationDestinationPath
                .normalize()
                .toAbsolutePath()
                .toFile();
        if (!fileDownloadLocationDestination.exists() && !fileDownloadLocationDestination.mkdirs()) {
            throw new IOException("Output directory path doesn't exists/ Unable to create destination directory.");
        }
        return fileDownloadLocationDestinationPath;
    }
}
