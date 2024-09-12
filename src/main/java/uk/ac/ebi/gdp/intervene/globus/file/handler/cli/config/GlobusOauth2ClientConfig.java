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
package uk.ac.ebi.gdp.intervene.globus.file.handler.cli.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import uk.ac.ebi.gdp.file.handler.core.properties.WebClientProperties;

import java.util.List;

import static io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS;
import static org.springframework.security.oauth2.core.AuthorizationGrantType.CLIENT_CREDENTIALS;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;
import static uk.ac.ebi.gdp.file.handler.core.exception.ClientException.clientException;
import static uk.ac.ebi.gdp.file.handler.core.exception.ServerException.serverException;

@Configuration
public class GlobusOauth2ClientConfig {

    private static final String GLOBUS_RESOURCE_ID = "GLOBUS_RESOURCE_ID";

    @ConfigurationProperties(prefix = "webclient.connection")
    @Bean
    public WebClientProperties webClientProperties() {
        return new WebClientProperties();
    }

    @Bean
    public ReactiveClientRegistrationRepository getRegistration(
            @Value("${globus.aai.access-token.uri}") final String tokenUri,
            @Value("${globus.aai.client-id}") final String clientId,
            @Value("${globus.aai.client-secret}") final String clientSecret,
            @Value("${globus.aai.scopes}") final String scope) {
        return getRegistration(
                tokenUri,
                clientId,
                clientSecret,
                List.of(scope)
        );
    }

    @Primary
    @Bean
    public ReactiveOAuth2AuthorizedClientService reactiveOAuth2AuthorizedClientService(final ReactiveClientRegistrationRepository clientRegistrationRepository) {
        return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
    }

    @Bean("globusWebClient")
    public WebClient globusWebClient(final ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
                                     final WebClientProperties webClientProperties,
                                     @Value("${globus.guest-collection.domain}") final String baseURL) {
        return webClient(
                authorizedClientManager,
                webClientProperties,
                baseURL
        );
    }

    @Bean
    public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
            final ReactiveClientRegistrationRepository clientRegistrationRepository,
            final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        return reactiveO2ACMClientCredentials(
                clientRegistrationRepository,
                authorizedClientService
        );
    }

    private ReactiveClientRegistrationRepository getRegistration(final String tokenUri,
                                                                 final String clientId,
                                                                 final String clientSecret,
                                                                 final List<String> scopes) {
        final ClientRegistration registration = ClientRegistration
                .withRegistrationId(GLOBUS_RESOURCE_ID)
                .tokenUri(tokenUri)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .authorizationGrantType(CLIENT_CREDENTIALS)
                .scope(scopes)
                .build();
        return new InMemoryReactiveClientRegistrationRepository(registration);
    }

    private ReactiveOAuth2AuthorizedClientManager reactiveO2ACMClientCredentials(final ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                                                 final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        final ReactiveOAuth2AuthorizedClientProvider authorizedClientProvider =
                ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                        .clientCredentials()
                        .build();

        final AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager authorizedClientManager =
                reactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
        return authorizedClientManager;
    }

    private AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager reactiveOAuth2AuthorizedClientManager(final ReactiveClientRegistrationRepository clientRegistrationRepository,
                                                                                                               final ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        return new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
    }

    private WebClient webClient(final ReactiveOAuth2AuthorizedClientManager authorizedClientManager,
                                final WebClientProperties webClientProperties,
                                final String baseURL) {
        final HttpClient httpClient = HttpClient.create()
                .option(CONNECT_TIMEOUT_MILLIS, webClientProperties.getConnectionTimeout() * 1000)
                .doOnConnected(connection -> connection
                        .addHandlerFirst(new ReadTimeoutHandler(webClientProperties.getReadWriteTimeout()))
                        .addHandlerFirst(new WriteTimeoutHandler(webClientProperties.getReadWriteTimeout())));

        final ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);

        oauth.setDefaultClientRegistrationId(GLOBUS_RESOURCE_ID);
        return WebClient.builder()
                .filters(exchangeFilterFunctions -> {
                    exchangeFilterFunctions.add(oauth);
                    exchangeFilterFunctions.add(errorHandler());
                })
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(baseURL)
                .build();
    }

    private ExchangeFilterFunction errorHandler() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().is4xxClientError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> error(clientException(clientResponse.statusCode().value(), errorBody)));
            } else if (clientResponse.statusCode().is5xxServerError()) {
                return clientResponse.bodyToMono(String.class)
                        .flatMap(errorBody -> error(serverException(clientResponse.statusCode().value(), errorBody)));
            } else {
                return just(clientResponse);
            }
        });
    }
}
