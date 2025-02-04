/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.horizon.minion.flows.parser.state;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

public class ExporterState {
    public final String key;

    public final List<TemplateState> templates;
    public final List<OptionState> options;

    public ExporterState(Builder builder) {
        this.key = Objects.requireNonNull(builder.key);

        this.templates = Objects.requireNonNull(builder.templates.build());
        this.options = Objects.requireNonNull(builder.options.build());
    }

    public static Builder builder(final String sessionKey) {
        return new Builder(sessionKey);
    }

    public static class Builder {
        private final String key;

        private final ImmutableList.Builder<TemplateState> templates = ImmutableList.builder();
        private final ImmutableList.Builder<OptionState> options = ImmutableList.builder();

        private Builder(final String key) {
            this.key = Objects.requireNonNull(key);
        }

        public Builder withTemplate(final TemplateState state) {
            this.templates.add(state);
            return this;
        }

        public Builder withTemplate(final TemplateState.Builder state) {
            return this.withTemplate(state.build());
        }

        public Builder withOptions(final OptionState state) {
            this.options.add(state);
            return this;
        }

        public Builder withOptions(final OptionState.Builder state) {
            return this.withOptions(state.build());
        }

        public ExporterState build() {
            return new ExporterState(this);
        }
    }
}
