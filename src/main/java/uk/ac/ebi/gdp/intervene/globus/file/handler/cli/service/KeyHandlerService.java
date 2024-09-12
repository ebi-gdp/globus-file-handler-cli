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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import uk.ac.ebi.gdp.intervene.globus.file.handler.cli.dto.PrivateKeyDTO;

import static uk.ac.ebi.gdp.file.handler.core.exception.ExceptionHandler.throwClientError;
import static uk.ac.ebi.gdp.file.handler.core.exception.ExceptionHandler.throwServerError;

public class KeyHandlerService {
    private final WebClient webClient;
    private final String keyHandlerServiceURI;

    public KeyHandlerService(final WebClient webClient,
                             final String keyHandlerServiceURI) {
        this.webClient = webClient;
        this.keyHandlerServiceURI = keyHandlerServiceURI;
    }

    public PrivateKeyDTO downloadPrivateKey(final String secretId,
                                            final String secretIdVersion) {
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path(keyHandlerServiceURI/*"/key/{secretId}/version/{versionId}"*/)
                        .build(secretId, secretIdVersion))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, throwClientError())
                .onStatus(HttpStatus::is5xxServerError, throwServerError())
                .toEntity(PrivateKeyDTO.class)
                .block()
                .getBody();
    }
}
