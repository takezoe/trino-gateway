/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.gateway.ha.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.trino.gateway.ha.security.LbOAuthManager.OidcTokens;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestOidcToken
{
    private static final Logger log = LoggerFactory.getLogger(TestOidcToken.class);

    @Test
    public void testParseTokenParamsGracefully()
    {
        // This test is to make sure that we simulate the condition where
        // the OIDC providers send additional parameters in the token
        // 'to_be_ignored' parameter should not cause any parsing exception
        // All the other parameters should have the correct values
        OidcTokens oidcTokens = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            String jsonStr = "{\"id_token\" : \"ABC235234\", "
                    + "\"access_token\" : \"AcessABCD123\", "
                    + "\"refresh_token\" : \"RefreshTKN\", "
                    + "\"token_type\" : \"TOKENType\", "
                    + "\"expires_in\" : \"123456\", "
                    + "\"to_be_ignored\" : \"XYX123456\", "
                    + "\"scope\" : \"global\" "
                    + "}";

            oidcTokens = objectMapper.readValue(jsonStr, OidcTokens.class);
        }
        catch (JsonProcessingException ex) {
            log.error(ex.getMessage());
            fail();
        }

        assertEquals("ABC235234", oidcTokens.getIdToken());
        assertEquals("AcessABCD123", oidcTokens.getAccessToken());
        assertEquals("RefreshTKN", oidcTokens.getRefreshToken());
        assertEquals("123456", oidcTokens.getExpiresIn());
        assertEquals("global", oidcTokens.getScope());
    }
}
