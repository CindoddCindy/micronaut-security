
package io.micronaut.security.token.jwt.endpoints

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class OauthControllerPathConfigurableSpec extends Specification {

    @Shared
    @AutoCleanup
    EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer, [
            'spec.name': 'refreshpathconfigurable',
            'micronaut.security.enabled': true,
            'micronaut.security.token.jwt.enabled': true,
            'micronaut.security.endpoints.oauth.enabled': true,
            'micronaut.security.endpoints.oauth.path': '/newtoken',
    ], Environment.TEST)

    @Shared
    @AutoCleanup
    RxHttpClient client = embeddedServer.applicationContext.createBean(RxHttpClient, embeddedServer.getURL())


    void "OauthController is not accessible at /oauth/access_token but at /newtoken"() {
        given:
        TokenRefreshRequest creds = new TokenRefreshRequest('foo', 'XXXXXXXXXX')

        expect:
        embeddedServer.applicationContext.getBean(OauthController.class)

        when:
        client.toBlocking().exchange(HttpRequest.POST('/oauth/access_token', creds))

        then:
        HttpClientResponseException e = thrown(HttpClientResponseException)
        e.status == HttpStatus.UNAUTHORIZED

        when:
        client.toBlocking().exchange(HttpRequest.POST('/newtoken', creds))

        then:
        e = thrown(HttpClientResponseException)
        e.status == HttpStatus.BAD_REQUEST
    }
}
