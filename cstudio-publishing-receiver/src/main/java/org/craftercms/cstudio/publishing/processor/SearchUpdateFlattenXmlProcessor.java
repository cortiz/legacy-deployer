/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.cstudio.publishing.processor;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.processors.impl.IncludeDescriptorsProcessor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @deprecated replaced by {@link SearchIndexingProcessor}
 */
@Deprecated
public class SearchUpdateFlattenXmlProcessor extends SearchUpdateProcessor {

    private IncludeDescriptorsProcessor includeDescriptorsProcessor;

    public void setIncludeElementXPathQuery(String includeElementXPathQuery) {
        // Ignore, we're using the injected IncludeDescriptorsProcessor
    }

    public void setDisableFlatteningElement(String disableFlatteningElement) {
        // Ignore, we're using the injected IncludeDescriptorsProcessor
    }

    public void setDisableNestedPageFlattening(boolean disableNestedPageFlattening) {
        // Ignore, we're using the injected IncludeDescriptorsProcessor
    }

    @Autowired
    public void setIncludeDescriptorsProcessor(IncludeDescriptorsProcessor includeDescriptorsProcessor) {
        this.includeDescriptorsProcessor = includeDescriptorsProcessor;
    }

    @Override
    protected List<ItemProcessor> createDocumentProcessorChain(List<ItemProcessor> chain) {
        chain.add(includeDescriptorsProcessor);

        return super.createDocumentProcessorChain(chain);
    }

}
