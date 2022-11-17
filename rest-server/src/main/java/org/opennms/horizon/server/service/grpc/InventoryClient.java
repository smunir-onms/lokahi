/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.horizon.server.service.grpc;

import java.util.List;

import org.opennms.horizon.inventory.dto.DeviceCreateDTO;
import org.opennms.horizon.inventory.dto.DeviceServiceGrpc;
import org.opennms.horizon.inventory.dto.MonitoringLocationDTO;
import org.opennms.horizon.inventory.dto.MonitoringLocationServiceGrpc;
import org.opennms.horizon.inventory.dto.MonitoringSystemServiceGrpc;
import org.opennms.horizon.inventory.dto.NodeDTO;

import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;

import io.grpc.ManagedChannel;

public class InventoryClient {
    private final ManagedChannel channel;
    private final MonitoringLocationServiceGrpc.MonitoringLocationServiceBlockingStub locationStub;
    private final DeviceServiceGrpc.DeviceServiceBlockingStub deviceStub;
    private final MonitoringSystemServiceGrpc.MonitoringSystemServiceBlockingStub systemStub;

    public InventoryClient(ManagedChannel channel) {
        this.channel = channel;
        locationStub = MonitoringLocationServiceGrpc.newBlockingStub(channel);
        deviceStub = DeviceServiceGrpc.newBlockingStub(channel);
        systemStub = MonitoringSystemServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if(channel!=null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    public NodeDTO createNewDevice(DeviceCreateDTO device) {
        return deviceStub.createDevice(device);
    }

    public List<NodeDTO> listDevice() {
        return deviceStub.listDevices(Empty.newBuilder().build()).getDevicesList();
    }

    public NodeDTO getDeviceById(long id) {
        return deviceStub.getDeviceById(Int64Value.of(id));
    }

    public List<MonitoringLocationDTO> listLocations() {
        return locationStub.listLocations(Empty.newBuilder().build()).getLocationsList();
    }

    public MonitoringLocationDTO getLocationById(long id) {
        return locationStub.getLocationById(Int64Value.of(id));
    }
}
