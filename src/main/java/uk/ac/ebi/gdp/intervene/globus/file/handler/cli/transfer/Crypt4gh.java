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
import java.util.StringJoiner;

import static java.util.Objects.requireNonNull;

/**
 * Crypt4gh builder class to provision necessary utilities to execute crypt4gh
 * commands e.g. encryption/decryption etc.
 */
public class Crypt4gh {
    private final Path crypt4ghBinAbsolutePath;
    private final Path privateKeyAbsolutePath;

    private Crypt4gh(final Builder builder) {
        this.crypt4ghBinAbsolutePath = builder.crypt4ghBinAbsolutePath;
        this.privateKeyAbsolutePath = builder.privateKeyAbsolutePath;
    }

    /**
     * Builder function to initialize Builder class with mandatory parameters.
     *
     * @param crypt4ghBinAbsolutePath absolute bin path to crypt4gh executable binary.
     * @param privateKeyAbsolutePath absolute path to private key.
     *
     * @return Builder instance.
     */
    public static Builder builder(final Path crypt4ghBinAbsolutePath,
                                  final Path privateKeyAbsolutePath) {
        return new Builder(crypt4ghBinAbsolutePath, privateKeyAbsolutePath);
    }

    /**
     * Builds crypt4gh decryption command.
     *
     * @param destinationFileURI location of decrypted file to download at.
     *
     * @return complete executable command.
     */
    public String crypt4ghDecryptBashCmd(final URI destinationFileURI) {
        requireNonNull(destinationFileURI, "Decrypted download file URI cannot be null");
        return new StringJoiner(" ")
                .add(crypt4ghBinAbsolutePath.toString())
                .add("decrypt")
                .add("--sk")
                .add(privateKeyAbsolutePath.toString())
                .add(">")
                .add(destinationFileURI.getPath())
                .toString();
    }

    /**
     * Builder class.
     */
    public static class Builder {
        private final Path crypt4ghBinAbsolutePath;
        private final Path privateKeyAbsolutePath;

        private Builder(final Path crypt4ghBinAbsolutePath, final Path privateKeyAbsolutePath) {
            requireNonNull(crypt4ghBinAbsolutePath, "Crypt4gh bin absolute path cannot be null");
            requireNonNull(privateKeyAbsolutePath, "Private key absolute path cannot be null");
            this.crypt4ghBinAbsolutePath = crypt4ghBinAbsolutePath;
            this.privateKeyAbsolutePath = privateKeyAbsolutePath;
        }

        /**
         * @return Crypt4gh instance.
         */
        public Crypt4gh build() {
            return new Crypt4gh(this);
        }
    }
}

