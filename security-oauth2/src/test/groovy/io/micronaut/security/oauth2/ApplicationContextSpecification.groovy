package io.micronaut.security.oauth2

import io.micronaut.context.ApplicationContext
import io.micronaut.core.io.socket.SocketUtils
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.oauth2.endpoint.endsession.request.AuthorizationServer
import mock.OpenIdConfigurationController
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

abstract class ApplicationContextSpecification extends Specification {
    @Shared
    int mockOpenIdHttpServerPort = SocketUtils.findAvailableTcpPort()

    @Shared
    String mockOpenIdHttpServerUrl = "http://localhost:${mockOpenIdHttpServerPort}"

    @Shared
    Map<String, Object> openIdMockServerConf = [
            "spec.name": "mockopenidprovider",
            'micronaut.security.enabled': true,
            'micronaut.security.token.jwt.enabled': true,
            'micronaut.server.port': mockOpenIdHttpServerPort,
            'mockserver.url': mockOpenIdHttpServerUrl,
    ]

    @AutoCleanup
    @Shared
    EmbeddedServer openIdMockEmbeddedServer = ApplicationContext.run(EmbeddedServer, openIdMockServerConf)

    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run(configuration)

    String getIssuer() {
        assert openIdMockEmbeddedServer.applicationContext.containsBean(OpenIdConfigurationController)
        assert mockOpenIdHttpServerUrl != null
        mockOpenIdHttpServerUrl
    }

    Map<String, Object> getConfiguration() {
            [
                    'micronaut.security.enabled': true,
                    'micronaut.security.token.jwt.enabled': true,
                    'micronaut.security.token.jwt.bearer.enabled': false,
                    'micronaut.security.token.jwt.cookie.enabled': true,
                    'micronaut.security.oauth2.enabled': true,
                    'micronaut.security.oauth2.clients.foo.client-id': 'XXXX',
                    'micronaut.security.oauth2.clients.foo.client-secret': 'YYYY',
                    'micronaut.security.oauth2.clients.foo.openid.issuer': getIssuer(),
                    'mockserver.authorizationserver': getAuthorizationServer(),
            ]
    }

    String getAuthorizationServer() {
        return AuthorizationServer.OKTA.name
    }
}
