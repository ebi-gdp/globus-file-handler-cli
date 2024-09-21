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

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.ebi.gdp.intervene.cryptography.aes.AESCryptography;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.dto.PrivateKeyDTO;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.dto.SecretDetailsDTO;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.service.KeyHandlerService;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.StringJoiner;

import static com.google.common.io.Files.write;
import static java.nio.file.Files.notExists;
import static java.util.Objects.requireNonNull;
import static uk.ac.ebi.gdp.file.handler.core.exception.ClientException.clientException;

/**
 * Crypt4gh builder class to provision necessary utilities to execute crypt4gh
 * commands e.g. encryption/decryption etc.
 */
public class Crypt4gh {
    public static final String SEC_KEY_EXT = ".sec";
    public static final String SECRET_DETAILS_FILE_SUFFIX = "secret-config.json";

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
        return new Builder(crypt4ghBinAbsolutePath,
                privateKeyAbsolutePath);
    }

    /**
     * Builder function to initialize Builder class with mandatory parameters.
     *
     * @param crypt4ghBinAbsolutePath absolute bin path to crypt4gh executable binary.
     * @param privateKeyAbsolutePath absolute path to private key.
     * @param keyHandlerService key handler service.
     * @param password to decrypt encrypted private key.
     *
     * @return Builder instance.
     */
    public static Builder builder(final Path crypt4ghBinAbsolutePath,
                                  final Path privateKeyAbsolutePath,
                                  final KeyHandlerService keyHandlerService,
                                  final char[] password) throws IOException {
        return new Builder(crypt4ghBinAbsolutePath,
                privateKeyAbsolutePath,
                keyHandlerService,
                password);
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
        private KeyHandlerService keyHandlerService;
        private char[] password;

        private Builder(final Path crypt4ghBinAbsolutePath,
                        final Path privateKeyAbsolutePath) {
            validateFiles(crypt4ghBinAbsolutePath, privateKeyAbsolutePath);
            this.crypt4ghBinAbsolutePath = crypt4ghBinAbsolutePath;
            this.privateKeyAbsolutePath = privateKeyAbsolutePath;
        }

        private Builder(final Path crypt4ghBinAbsolutePath,
                        final Path privateKeyAbsolutePath,
                        final KeyHandlerService keyHandlerService,
                        final char[] password) throws IOException {
            validateFiles(crypt4ghBinAbsolutePath, privateKeyAbsolutePath);
            this.crypt4ghBinAbsolutePath = crypt4ghBinAbsolutePath;
            this.keyHandlerService = keyHandlerService;
            this.password = password;
            this.privateKeyAbsolutePath = retrievePrivateKeyFromKeyHandlerService(privateKeyAbsolutePath);
        }

        private void validateFiles(final Path crypt4ghBinAbsolutePath,
                                   final Path privateKeyAbsolutePath) {
            requireNonNull(crypt4ghBinAbsolutePath, "Crypt4gh bin absolute path cannot be null");
            validatePrivateKey(privateKeyAbsolutePath);

            if (notExists(privateKeyAbsolutePath)) {
                throw clientException(404, "Private key file details %s not found!".formatted(privateKeyAbsolutePath.toString()));
            }
        }

        private void validatePrivateKey(final Path privateKeyAbsolutePath) {
            requireNonNull(privateKeyAbsolutePath, "Private key absolute path cannot be null");
            final String fileName = privateKeyAbsolutePath.getFileName().toString();
            if (!fileName.endsWith(SECRET_DETAILS_FILE_SUFFIX) && !fileName.endsWith(SEC_KEY_EXT)) {
                throw clientException(404, "File name should either end with %s OR %s".formatted(SEC_KEY_EXT, SECRET_DETAILS_FILE_SUFFIX));
            }
        }

        private Path retrievePrivateKeyFromKeyHandlerService(final Path privateKeySecretConfig) throws IOException {
            final ObjectMapper objectMapper = new ObjectMapper();
            final SecretDetailsDTO secretDetailsDTO = objectMapper
                    .readValue(privateKeySecretConfig.toFile(), SecretDetailsDTO.class);
            final PrivateKeyDTO privateKeyDTO = getSecretDetails(secretDetailsDTO);
            final AESCryptography aesCryptography = new AESCryptography.Builder().build();
            final byte[] privateKey = aesCryptography.decrypt(privateKeyDTO.privateKey(), password);
            return saveDecryptedPrivateKey(privateKey,
                    secretDetailsDTO.secretId(),
                    privateKeySecretConfig.getParent());
        }

        private PrivateKeyDTO getSecretDetails(final SecretDetailsDTO secretDetailsDTO) {
            return keyHandlerService.downloadPrivateKey(secretDetailsDTO.secretId(),
                    secretDetailsDTO.secretIdVersion());
        }

        private Path saveDecryptedPrivateKey(final byte[] privateKey,
                                             final String secretId,
                                             final Path privateKeySecretConfigParentPath) throws IOException {
            final Path privateKeyPath = privateKeySecretConfigParentPath
                    .resolve(secretId + SEC_KEY_EXT);
            write(privateKey, privateKeyPath.toFile());
            return privateKeyPath;
        }

        /**
         * @return Crypt4gh instance.
         */
        public Crypt4gh build() {
            return new Crypt4gh(this);
        }
    }
}

