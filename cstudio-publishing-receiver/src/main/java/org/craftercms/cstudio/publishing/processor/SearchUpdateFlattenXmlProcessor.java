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

/**
 * @deprecated replaced by {@link SearchIndexingProcessor}
 */
@Deprecated
public class SearchUpdateFlattenXmlProcessor extends SearchUpdateProcessor {

    protected String includeElementXPathQuery;
    protected String disableFlatteningElement;

    private boolean disableNestedPageFlattening;

    public String getIncludeElementXPathQuery() {
        return includeElementXPathQuery;
    }

    public String getDisableFlatteningElement() {
        return disableFlatteningElement;
    }

    public void setIncludeElementXPathQuery(String includeElementXPathQuery) {
        this.includeElementXPathQuery = includeElementXPathQuery;
    }

    public void setDisableFlatteningElement(String disableFlatteningElement) {
        this.disableFlatteningElement = disableFlatteningElement;
    }

    public void setDisableNestedPageFlattening(final boolean disableNestedPageFlattening) {
        this.disableNestedPageFlattening = disableNestedPageFlattening;
    }

    @Override
    protected List<ItemProcessor> createDocumentProcessorChain(List<ItemProcessor> chain) {
        IncludeDescriptorsProcessor processor = new IncludeDescriptorsProcessor();

        if (StringUtils.isNotEmpty(includeElementXPathQuery)) {
            processor.setIncludeElementXPathQuery(includeElementXPathQuery);
        }
        if (StringUtils.isNotEmpty(disableFlatteningElement)) {
            processor.setDisabledIncludeNodeXPathQuery(disableFlatteningElement);
        }

        chain.add(processor);

        return super.createDocumentProcessorChain(chain);
    }

}
