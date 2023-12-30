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
package io.trino.gateway.ha.router;

import com.google.common.collect.ImmutableList;
import io.trino.gateway.ha.config.ProxyBackendConfiguration;
import io.trino.gateway.ha.persistence.JdbcConnectionManager;
import io.trino.gateway.ha.persistence.dao.GatewayBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class HaGatewayManager
        implements GatewayBackendManager
{
    private static final Logger log = LoggerFactory.getLogger(HaGatewayManager.class);
    private final JdbcConnectionManager connectionManager;

    public HaGatewayManager(JdbcConnectionManager connectionManager)
    {
        this.connectionManager = connectionManager;
    }

    @Override
    public List<ProxyBackendConfiguration> getAllBackends()
    {
        try {
            connectionManager.open();
            List<GatewayBackend> proxyBackendList = GatewayBackend.findAll();
            return GatewayBackend.upcast(proxyBackendList);
        }
        finally {
            connectionManager.close();
        }
    }

    @Override
    public List<ProxyBackendConfiguration> getAllActiveBackends()
    {
        try {
            connectionManager.open();
            List<GatewayBackend> proxyBackendList = GatewayBackend.where("active = ?", true);
            return GatewayBackend.upcast(proxyBackendList);
        }
        finally {
            connectionManager.close();
        }
    }

    @Override
    public List<ProxyBackendConfiguration> getActiveAdhocBackends()
    {
        try {
            connectionManager.open();
            List<GatewayBackend> proxyBackendList =
                    GatewayBackend.where("active = ? and routing_group = ?", true, "adhoc");
            return GatewayBackend.upcast(proxyBackendList);
        }
        catch (Exception e) {
            log.info("Error fetching all backends", e.getLocalizedMessage());
        }
        finally {
            connectionManager.close();
        }
        return ImmutableList.of();
    }

    @Override
    public List<ProxyBackendConfiguration> getActiveBackends(String routingGroup)
    {
        try {
            connectionManager.open();
            List<GatewayBackend> proxyBackendList =
                    GatewayBackend.where("active = ? and routing_group = ?", true, routingGroup);
            return GatewayBackend.upcast(proxyBackendList);
        }
        finally {
            connectionManager.close();
        }
    }

    @Override
    public Optional<ProxyBackendConfiguration> getBackendByName(String name)
    {
        try {
            connectionManager.open();
            List<GatewayBackend> proxyBackendList =
                    GatewayBackend.where("name = ?", name);
            return GatewayBackend.upcast(proxyBackendList).stream().findAny();
        }
        finally {
            connectionManager.close();
        }
    }

    @Override
    public void deactivateBackend(String backendName)
    {
        try {
            connectionManager.open();
            GatewayBackend.findFirst("name = ?", backendName).set("active", false).saveIt();
        }
        finally {
            connectionManager.close();
        }
    }

    @Override
    public void activateBackend(String backendName)
    {
        try {
            connectionManager.open();
            GatewayBackend.findFirst("name = ?", backendName).set("active", true).saveIt();
        }
        finally {
            connectionManager.close();
        }
    }

    public ProxyBackendConfiguration addBackend(ProxyBackendConfiguration backend)
    {
        try {
            connectionManager.open();
            GatewayBackend.create(new GatewayBackend(), backend);
        }
        finally {
            connectionManager.close();
        }
        return backend;
    }

    public ProxyBackendConfiguration updateBackend(ProxyBackendConfiguration backend)
    {
        try {
            connectionManager.open();
            GatewayBackend model = GatewayBackend.findFirst("name = ?", backend.getName());
            if (model == null) {
                GatewayBackend.create(model, backend);
            }
            else {
                GatewayBackend.update(model, backend);
            }
        }
        finally {
            connectionManager.close();
        }
        return backend;
    }

    public void deleteBackend(String name)
    {
        try {
            connectionManager.open();
            GatewayBackend.delete("name = ?", name);
        }
        finally {
            connectionManager.close();
        }
    }
}
