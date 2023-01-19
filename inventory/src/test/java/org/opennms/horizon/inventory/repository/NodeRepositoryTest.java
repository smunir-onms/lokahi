/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.horizon.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.inventory.SpringContextTestInitializer;
import org.opennms.horizon.inventory.model.MonitoringLocation;
import org.opennms.horizon.inventory.model.Node;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {SpringContextTestInitializer.class})
class NodeRepositoryTest {
    @Autowired
    private NodeRepository nodeRepo;
    @Autowired
    private MonitoringLocationRepository locationRepo;
    private final String tenantId1 = "test-tenant-1", tenantId2 = "test-tenant-2";
    private MonitoringLocation location1, location2;
    private Node node1, node2, node3;

    @BeforeEach
    void setUp() {
        location1 = new MonitoringLocation();
        location1.setLocation("location1");
        location1.setTenantId(tenantId1);
        locationRepo.save(location1);

        location2 = new MonitoringLocation();
        location2.setLocation("location2");
        location2.setTenantId(tenantId2);
        locationRepo.save(location2);

        node1 = new Node();
        node1.setNodeLabel("node1");
        node1.setTenantId(tenantId1);
        node1.setCreateTime(LocalDateTime.now());
        node1.setMonitoringLocation(location1);
        nodeRepo.save(node1);

        node2 = new Node();
        node2.setNodeLabel("node2");
        node2.setTenantId(tenantId1);
        node2.setCreateTime(LocalDateTime.now());
        node2.setMonitoringLocation(location1);
        nodeRepo.save(node2);

        node3 = new Node();
        node3.setNodeLabel("node3");
        node3.setTenantId(tenantId2);
        node3.setCreateTime(LocalDateTime.now());
        node3.setMonitoringLocation(location2);
        nodeRepo.save(node3);
    }

    @AfterEach
    void cleanUp() {
        nodeRepo.deleteAll();
        locationRepo.deleteAll();
    }

    @Test
    void testListByLocationIdAndTenant() {
        List<Node> list = nodeRepo.findByMonitoringLocationIdAndTenantId(location1.getId(), tenantId1);
        assertThat(list)
            .hasSize(2)
            .extracting(Node::getNodeLabel)
            .containsExactly(node1.getNodeLabel(), node2.getNodeLabel());

        List<Node> list2 = nodeRepo.findByMonitoringLocationIdAndTenantId(location2.getId(), tenantId2);
        assertThat(list2)
            .hasSize(1)
            .extracting(Node::getNodeLabel)
            .containsExactly(node3.getNodeLabel());
    }

    @Test
    void testListByWrongIdOrTenant() {
        List<Node> list = nodeRepo.findByMonitoringLocationIdAndTenantId(location1.getId(), tenantId2);
        assertThat(list).isEmpty();
        List<Node> list2 = nodeRepo.findByMonitoringLocationIdAndTenantId(location2.getId(), tenantId1);
        assertThat(list2).isEmpty();
    }
}
