/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2017 The OpenNMS Group, Inc.
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

package org.opennms.horizon.events.conf.xml;


import org.opennms.horizon.events.util.ConfigUtils;
import org.opennms.horizon.events.util.ValidateUsing;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Objects;

/**
 * Optional event parameter
 * 
 * @author <a href="mailto:agaue@opennms.org>Alejandro Galue</a>
 */
@XmlRootElement(name="parameter")
@XmlAccessorType(XmlAccessType.NONE)
@ValidateUsing("eventconf.xsd")
public class Parameter implements Serializable {

    private static final long serialVersionUID = 2L;

    @XmlAttribute(name="name", required=true)
    private String m_name;

    @XmlAttribute(name="value", required=true)
    private String m_value;

    @XmlAttribute(name="expand", required=false)
    private Boolean m_expand;

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getValue() {
        return m_value;
    }

    public void setValue(final String value) {
        m_value = ConfigUtils.assertNotEmpty(value, "value");
    }

    public Boolean getExpand() {
        return m_expand == null ? Boolean.FALSE : m_expand;
    }

    public void setExpand(final Boolean expand) {
        m_expand = expand;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, m_value, m_expand);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Parameter) {
            final Parameter that = (Parameter) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_value, that.m_value) &&
                    Objects.equals(this.m_expand, that.m_expand);
        }
        return false;
    }

}
