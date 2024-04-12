/*
 *
 * Copyright 2020 EMBL - European Bioinformatics Institute
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

import org.springframework.core.env.PropertySource;

import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.CRYPT4GH_PRIVATE_KEY_PATH_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.CRYPT4GH_PRIVATE_KEY_PATH_SHORT;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_DESTINATION_PATH_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_DESTINATION_PATH_SHORT;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_FILE_SIZE_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_FILE_SIZE_SHORT;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_SOURCE_PATH_LONG;
import static uk.ac.ebi.gdp.intervene.globus.file.handler.cli.parser.CLIParser.GLOBUS_FILE_TRANSFER_SOURCE_PATH_SHORT;

public class CLIPropertySource extends PropertySource<CLIParameters> {
    public CLIPropertySource(final String name, final CLIParameters source) {
        super(name, source);
    }

    @Override
    public Object getProperty(final String name) {
        return switch (name) {
            case GLOBUS_FILE_TRANSFER_SOURCE_PATH_SHORT, GLOBUS_FILE_TRANSFER_SOURCE_PATH_LONG ->
                    getSource().getFileDownloadSourceLocation();
            case GLOBUS_FILE_TRANSFER_DESTINATION_PATH_SHORT, GLOBUS_FILE_TRANSFER_DESTINATION_PATH_LONG ->
                    getSource().getFileDownloadDestinationLocation();
            case GLOBUS_FILE_TRANSFER_FILE_SIZE_SHORT, GLOBUS_FILE_TRANSFER_FILE_SIZE_LONG -> getSource().getFileSize();
            case CRYPT4GH_PRIVATE_KEY_PATH_SHORT, CRYPT4GH_PRIVATE_KEY_PATH_LONG ->
                    getSource().getCrypt4ghPrivateKeyPath();
            default -> null;
        };
    }
}
