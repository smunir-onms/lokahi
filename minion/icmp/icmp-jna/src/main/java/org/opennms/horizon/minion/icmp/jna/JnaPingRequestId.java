/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.icmp.jna;

import java.net.InetAddress;
import org.opennms.horizon.shared.utils.InetAddressComparator;

/**
 * <p>JnaPingRequestId class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JnaPingRequestId {
    private InetAddress m_addr;
    private int m_identifier;
    private int m_sequenceNumber;
    private long m_threadId;

    public JnaPingRequestId(InetAddress addr, int identifier, int sequenceNumber, long threadId) {
        m_addr = addr;
        m_identifier = identifier;
        m_sequenceNumber = sequenceNumber;
        m_threadId = threadId;
    }

    public JnaPingRequestId(JnaPingReply reply) {
        this(reply.getAddress(), reply.getIdentifier(), reply.getSequenceNumber(), reply.getThreadId());
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_addr;
    }

    /**
     * <p>getTid</p>
     *
     * @return a long.
     */
    public int getIdentifier() {
        return m_identifier;
    }

    /**
     * <p>getSequenceId</p>
     *
     * @return a short.
     */
    public int getSequenceNumber() {
        return m_sequenceNumber;
    }
    
    public long getThreadId() {
        return m_threadId;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JnaPingRequestId) {
            JnaPingRequestId id = (JnaPingRequestId)obj;
            return (new InetAddressComparator().compare(getAddress(), id.getAddress()) == 0)
                && getIdentifier() == id.getIdentifier() 
                && getSequenceNumber() == id.getSequenceNumber()
                && getThreadId() == id.getThreadId();
        }
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + m_addr.hashCode();
        hash = hash * 31 + m_identifier;
        hash = hash * 31 + m_sequenceNumber;
        hash = hash * 31 + (int)(m_threadId >>> 32);
        hash = hash * 31 + (int)(m_threadId);
        return hash;
    }


    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(getClass().getSimpleName());
        buf.append('[');
        buf.append("addr = ").append(m_addr);
        buf.append(", ");
        buf.append("ident = ").append(m_identifier);
        buf.append(", ");
        buf.append("seqNum = ").append(m_sequenceNumber);
        buf.append(", ");
        buf.append("tId = ").append(m_threadId);
        buf.append(']');
        return buf.toString();
    }
    

}
