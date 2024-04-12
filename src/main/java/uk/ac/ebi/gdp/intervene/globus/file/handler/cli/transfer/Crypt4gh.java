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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.transfer;

import java.net.URI;
import java.nio.file.Path;

import static java.lang.String.join;

public record Crypt4gh(
        Path crypt4ghBinAbsolutePath,
        Path privateKeyAbsolutePath) {
    public String crypt4ghDecryptBashCmd(final URI decryptedDownloadFile) {
        return join(" ",
                crypt4ghBinAbsolutePath.toString(), "decrypt",
                "--sk", privateKeyAbsolutePath.toString(), ">", decryptedDownloadFile.getPath());
    }
}
