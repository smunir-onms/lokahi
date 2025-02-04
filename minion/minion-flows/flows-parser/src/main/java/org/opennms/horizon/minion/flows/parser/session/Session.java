/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.flows.parser.session;

import java.net.InetAddress;
import java.util.Collection;
import java.util.List;

import org.opennms.horizon.minion.flows.parser.MissingTemplateException;
import org.opennms.horizon.minion.flows.parser.ie.Value;

public interface Session {

    interface Resolver {
        Template lookupTemplate(final int templateId) throws MissingTemplateException;
        List<Value<?>> lookupOptions(final List<Value<?>> values);
    }

    void addTemplate(final long observationDomainId, final Template template);

    void removeTemplate(final long observationDomainId, final int templateId);

    void removeAllTemplate(final long observationDomainId, final Template.Type type);

    void addOptions(final long observationDomainId,
                    final int templateId,
                    final Collection<Value<?>> scopes,
                    final List<Value<?>> values);

    Resolver getResolver(final long observationDomainId);

    InetAddress getRemoteAddress();

    boolean verifySequenceNumber(final long observationDomainId,
                                 final long sequenceNumber);
}
