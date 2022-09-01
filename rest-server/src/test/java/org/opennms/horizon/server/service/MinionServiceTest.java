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

package org.opennms.horizon.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Arrays;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opennms.horizon.shared.dto.minion.MinionCollectionDTO;
import org.opennms.horizon.shared.dto.minion.MinionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import io.leangen.graphql.execution.ResolutionEnvironment;
import reactor.core.publisher.Mono;

@SpringBootTest
public class MinionServiceTest {
    @MockBean
    private PlatformGateway mockGateway;
    @MockBean
    private ResolutionEnvironment mockEnv;
    @Autowired
    private MinionService minionService;
    @Autowired
    private CacheManager cacheManager;
    private MinionCollectionDTO collectionDTO;
    private MinionDTO dto1;
    private MinionDTO dto2;
    private String authHeader = "Authorization: abdcd";

    @BeforeEach
    public void setup() {
        dto1 = new MinionDTO();
        dto1.setId("test-minion-1");
        dto2 = new MinionDTO();
        dto2.setId("test-minion-2");
        collectionDTO = new MinionCollectionDTO(Arrays.asList(dto1, dto2));
        doReturn(authHeader).when(mockGateway).getAuthHeader(mockEnv);
        cacheManager.getCache("minions").clear();
    }

   @Test
    public void testListMinions() {
        doReturn(Mono.just(collectionDTO)).when(mockGateway).get(PlatformGateway.URL_PATH_MINIONS, authHeader, MinionCollectionDTO.class);
        minionService.listMinions(mockEnv).subscribe(result -> assertEquals(collectionDTO, result));
        minionService.listMinions(mockEnv).subscribe(result -> assertEquals(collectionDTO, result));
        verify(mockGateway, times(2)).getAuthHeader(mockEnv);
        //now cache for list minions
        verify(mockGateway, times(2)).get(PlatformGateway.URL_PATH_MINIONS, authHeader, MinionCollectionDTO.class);
        verifyNoMoreInteractions(mockGateway);
    }

    @Test
    public void testGetByIDAfterList() {
        doReturn(Mono.just(collectionDTO)).when(mockGateway).get(PlatformGateway.URL_PATH_MINIONS, authHeader, MinionCollectionDTO.class);
        minionService.listMinions(mockEnv).doOnSuccess(result -> {
            assertEquals(collectionDTO, result);
            minionService.getMinionById(dto1.getId(), mockEnv).subscribe(m -> assertEquals(dto1, m)); // from the cache
            minionService.getMinionById(dto2.getId(), mockEnv).subscribe(m -> assertEquals(dto2, m));
        });
        verify(mockGateway).getAuthHeader(mockEnv);
        verify(mockGateway).get(PlatformGateway.URL_PATH_MINIONS, authHeader, MinionCollectionDTO.class);
        verifyNoMoreInteractions(mockGateway);
    }

    @Test
    public void testGetByID() {
        doReturn(Mono.just(dto1)).when(mockGateway).get(String.format(PlatformGateway.URL_PATH_MINIONS_ID, dto1.getId()), authHeader, MinionDTO.class);
        minionService.getMinionById(dto1.getId(), mockEnv).subscribe(result -> assertEquals(dto1, result));
        minionService.getMinionById(dto1.getId(), mockEnv).subscribe(result -> assertEquals(dto1, result)); //got dto from the cache
        verify(mockGateway).getAuthHeader(mockEnv);
        verify(mockGateway).get(String.format(PlatformGateway.URL_PATH_MINIONS_ID, dto1.getId()), authHeader, MinionDTO.class);
        verifyNoMoreInteractions(mockGateway);
    }
}
