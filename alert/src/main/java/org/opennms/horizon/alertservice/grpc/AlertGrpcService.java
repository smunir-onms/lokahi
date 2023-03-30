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

package org.opennms.horizon.alertservice.grpc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.opennms.horizon.alerts.proto.Alert;
import org.opennms.horizon.alerts.proto.AlertError;
import org.opennms.horizon.alerts.proto.AlertRequest;
import org.opennms.horizon.alerts.proto.AlertResponse;
import org.opennms.horizon.alerts.proto.AlertServiceGrpc;
import org.opennms.horizon.alerts.proto.DeleteAlertResponse;
import org.opennms.horizon.alerts.proto.ListAlertsRequest;
import org.opennms.horizon.alerts.proto.ListAlertsResponse;
import org.opennms.horizon.alertservice.api.AlertService;
import org.opennms.horizon.alertservice.db.repository.AlertRepository;
import org.opennms.horizon.alertservice.db.tenant.TenantLookup;
import org.opennms.horizon.alertservice.service.AlertMapper;
import org.opennms.horizon.model.common.proto.Severity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.google.protobuf.Timestamp;
import com.google.protobuf.UInt64Value;

import io.grpc.Context;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AlertGrpcService extends AlertServiceGrpc.AlertServiceImplBase {
    public static final int PAGE_SIZE_DEFAULT = 10;
    public static final String SORT_BY_DEFAULT = "alertId";
    public static final int DURATION = 24;
    private final AlertMapper alertMapper;
    private final AlertRepository alertRepository;
    private final AlertService alertService;
    private final TenantLookup tenantLookup;

    @Override
    public void listAlerts(ListAlertsRequest request, StreamObserver<ListAlertsResponse> responseObserver) {
        // Extract the page size, page and sort values from the request
        int pageSize = request.getPageSize() != 0 ? request.getPageSize() : PAGE_SIZE_DEFAULT;
        int page = request.getPage();
        String sortBy = !request.getSortBy().isEmpty() ? request.getSortBy() : SORT_BY_DEFAULT;
        boolean sortAscending = request.getSortAscending();

        // Create a PageRequest object based on the page size, next page token, filter, and sort parameters
        Sort.Direction sortDirection = sortAscending ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, pageSize, Sort.by(sortDirection, sortBy));

        // Get Filters
        List<Date> timeRange = new ArrayList<>();
        List<Severity> severities = new ArrayList<>();
        getFilter(request, timeRange, severities);

        Optional<String> lookupTenantId = tenantLookup.lookupTenantId(Context.current());
        var alertPage = lookupTenantId
            .map(tenantId -> alertRepository.findBySeverityInAndLastEventTimeBetweenAndTenantId(severities, timeRange.get(0), timeRange.get(1), pageRequest, tenantId))
            .orElseThrow();

        List<Alert> alerts = alertPage.getContent().stream()
            .map(alertMapper::toProto)
            .toList();

        ListAlertsResponse.Builder responseBuilder = ListAlertsResponse.newBuilder()
            .addAllAlerts(alerts);

        // If there is a next page, add the page number to the response's next_page_token field
        if (alertPage.hasNext()) {
            responseBuilder.setNextPageToken(alertPage.nextPageable().getPageNumber());
        }

        // Set last_page_token
        responseBuilder.setLastPageToken(alertPage.getTotalPages() - 1);

        // Set total alerts
        responseBuilder.setTotalAlerts(alertPage.getTotalElements());

        // Build the final ListAlertsResponse object and send it to the client using the responseObserver
        ListAlertsResponse response = responseBuilder.build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAlert(AlertRequest request, StreamObserver<DeleteAlertResponse> responseObserver) {
        var deleteAlertResponse = DeleteAlertResponse.newBuilder();
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        request.getAlertIdList().forEach(
            alertId -> {
                boolean success = alertService.deleteAlertByIdAndTenantId(alertId, tenantId);
                if (success) {
                    deleteAlertResponse.addAlertId(alertId).build();
                } else {
                    AlertError alertError = AlertError.newBuilder().setAlertId(alertId).setError("Couldn't delete alert").build();
                    deleteAlertResponse.addAlertError(alertError);
                }
            });

        responseObserver.onNext(deleteAlertResponse.build());
        responseObserver.onCompleted();
    }

    @Override
    public void acknowledgeAlert(AlertRequest request, StreamObserver<AlertResponse> responseObserver) {
        var alertResponse = AlertResponse.newBuilder();
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        request.getAlertIdList().forEach(
            alertId -> {
                Optional<Alert> alert = alertService.acknowledgeAlertByIdAndTenantId(alertId, tenantId);
                if(alert.isPresent()) {
                    alertResponse.addAlert(alert.get());
                }
                else {
                    AlertError alertError = AlertError.newBuilder().setAlertId(alertId).setError("Couldn't acknowledged alert").build();
                    alertResponse.addAlertError(alertError);
                }
            });

        responseObserver.onNext(alertResponse.build());
        responseObserver.onCompleted();
    }

    @Override
    public void unacknowledgeAlert(AlertRequest request, StreamObserver<AlertResponse> responseObserver) {
        var alertResponse = AlertResponse.newBuilder();
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        request.getAlertIdList().forEach(
            alertId -> {
                Optional<Alert> alert = alertService.unacknowledgeAlertByIdAndTenantId(alertId, tenantId);
                if(alert.isPresent()) {
                    alertResponse.addAlert(alert.get());
                }
                else {
                    AlertError alertError = AlertError.newBuilder().setAlertId(alertId).setError("Couldn't unacknowledged alert").build();
                    alertResponse.addAlertError(alertError);
                }
            });

        responseObserver.onNext(alertResponse.build());
        responseObserver.onCompleted();
    }

    @Override
    public void clearAlert(AlertRequest request, StreamObserver<AlertResponse> responseObserver) {
        var alertResponse = AlertResponse.newBuilder();
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        request.getAlertIdList().forEach(
            alertId -> {
                Optional<Alert> alert = alertService.clearAlertByIdAndTenantId(alertId, tenantId);
                if(alert.isPresent()) {
                    alertResponse.addAlert(alert.get());
                }
                else {
                    AlertError alertError = AlertError.newBuilder().setAlertId(alertId).setError("Couldn't clear alert").build();
                    alertResponse.addAlertError(alertError);
                }
            });

        responseObserver.onNext(alertResponse.build());
        responseObserver.onCompleted();
    }

    @Override
    public void escalateAlert(AlertRequest request, StreamObserver<AlertResponse> responseObserver) {
        var alertResponse = AlertResponse.newBuilder();
        String tenantId = tenantLookup.lookupTenantId(Context.current()).orElseThrow();
        request.getAlertIdList().forEach(
            alertId -> {
                Optional<Alert> alert = alertService.escalateAlertByIdAndTenantId(alertId, tenantId);
                if(alert.isPresent()) {
                    alertResponse.addAlert(alert.get());
                }
                else {
                    AlertError alertError = AlertError.newBuilder().setAlertId(alertId).setError("Couldn't escalate alert").build();
                    alertResponse.addAlertError(alertError);
                }
            });

        responseObserver.onNext(alertResponse.build());
        responseObserver.onCompleted();
    }

    @Override
    public void countAlerts(ListAlertsRequest request, StreamObserver<UInt64Value> responseObserver) {
        List<Date> timeRange = new ArrayList<>();
        List<Severity> severities = new ArrayList<>();
        getFilter(request, timeRange, severities);

        responseObserver.onNext(UInt64Value.of(tenantLookup.lookupTenantId(Context.current())
            .map(tenantId -> alertRepository.countAlertBySeverityInAndLastEventTimeBetweenAndTenantId(severities, timeRange.get(0), timeRange.get(1), tenantId))
            .orElseThrow()));
        responseObserver.onCompleted();
    }

    private static void getFilter(ListAlertsRequest request, List<Date> timeRange, List<Severity> severities) {
        request.getFiltersList().forEach(filter -> {
            if (filter.hasSeverity()) {
                severities.add(Severity.valueOf(filter.getSeverity().name()));
            }
            if (filter.hasTimeRange()) {
                timeRange.add(convertTimestampToDate(filter.getTimeRange().getStartTime()));
                timeRange.add(convertTimestampToDate(filter.getTimeRange().getEndTime()));
            }
        });

        if (timeRange.isEmpty()) {
            getDefaultTimeRange(timeRange);
        }

        if (severities.isEmpty()) {
            getAllSeverities(severities);
        }
    }

    private static void getAllSeverities(List<Severity> severities) {
        severities.addAll(Arrays.asList(Severity.values()));
    }

    private static void getDefaultTimeRange(List<Date> timeRange) {
        Calendar calendar = Calendar.getInstance();
        Date endTime = calendar.getTime();
        calendar.add(Calendar.HOUR_OF_DAY, -DURATION);
        Date startTime = calendar.getTime();
        timeRange.add(startTime);
        timeRange.add(endTime);
    }

    private static Date convertTimestampToDate(Timestamp timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
        return Date.from(instant);
    }
}
