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

package org.opennms.horizon.flows.grpc.client;


import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.opennms.dataplatform.flows.ingester.v1.IngesterGrpc;
import org.opennms.dataplatform.flows.ingester.v1.StoreFlowDocumentsRequest;
import org.opennms.dataplatform.flows.ingester.v1.StoreFlowDocumentsResponse;
import org.opennms.horizon.shared.constants.GrpcConstants;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IngestorClient {

    private final ManagedChannel channel;
    private final long deadline;
    private IngesterGrpc.IngesterBlockingStub ingesterBlockingStub;

    protected void initStubs() {
        ingesterBlockingStub = IngesterGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        if (Objects.nonNull(channel) && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    private Metadata getMetadata(boolean bypassAuthorization, String tenantId) {
        var metadata = new Metadata();
        metadata.put(GrpcConstants.AUTHORIZATION_BYPASS_KEY, String.valueOf(bypassAuthorization));
        metadata.put(GrpcConstants.TENANT_ID_BYPASS_KEY, tenantId);
        return metadata;
    }

    public StoreFlowDocumentsResponse sendData(StoreFlowDocumentsRequest storeFlowDocumentsRequest) {
        // TODO: use spring retry for retry mechanismus, in case of failure from ingestor
        // TODO:set  tenantID
        String tenantId = "";
        Metadata metadata = getMetadata(true, tenantId);

        return ingesterBlockingStub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata))
            .withDeadlineAfter(deadline, TimeUnit.MILLISECONDS)
            .storeFlowDocuments(storeFlowDocumentsRequest);
    }

}
