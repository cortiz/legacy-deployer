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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.processors.ItemProcessor;
import org.craftercms.core.processors.impl.FieldRenamingProcessor;
import org.craftercms.core.processors.impl.ItemProcessorPipeline;
import org.craftercms.core.service.Content;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.cstudio.publishing.PublishedChangeSet;
import org.craftercms.cstudio.publishing.exception.PublishingException;
import org.craftercms.cstudio.publishing.servlet.FileUploadServlet;
import org.craftercms.cstudio.publishing.target.PublishingTarget;
import org.craftercms.search.service.SearchService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Processor to update the Crafter Search engine index.
 *
 * @author Alfonso Vásquez
 * @deprecated replaced by {@link SearchIndexingProcessor}
 */
@Deprecated
public class SearchUpdateProcessor extends AbstractPublishingProcessor {

    private static final Log logger = LogFactory.getLog(SearchUpdateProcessor.class);

    protected SearchService searchService;
    protected String siteName;
    protected Map<String, String> fieldMappings;
    private String charEncoding = CharEncoding.UTF_8;
    protected ItemProcessor documentProcessor;

    @Required
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * set a sitename to override in index
     *
     * @param siteName an override siteName in index
     */
    public void setSiteName(String siteName) {
        if (!StringUtils.isEmpty(siteName)) {
            // check if it is preview for backward compatibility
            if (!SITE_NAME_PREVIEW.equalsIgnoreCase(siteName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Overriding site name in index with " + siteName);
                }
                this.siteName = siteName;
            }
        }
    }

    public void setFieldMappings(Map<String, String> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    public void setCharEncoding(String charEncoding) {
        this.charEncoding = charEncoding;
    }

    public void setTokenizeAttribute(String tokenizeAttribute) {
        // Ignore, now this functionality is done by the server
    }

    public void setTokenizeSubstitutionMap(Map<String, String> tokenizeSubstitutionMap) {
        // Ignore, now this functionality is done by the server
    }

    public void setDocumentProcessor(ItemProcessor documentProcessor) {
        this.documentProcessor = documentProcessor;
    }

    @PostConstruct
    public void init() {
        if (documentProcessor == null) {
            List<ItemProcessor> chain = createDocumentProcessorChain(new ArrayList<ItemProcessor>());

            documentProcessor = new ItemProcessorPipeline(chain);
        }
    }

    @Override
    public void doProcess(PublishedChangeSet changeSet, Map<String, String> parameters,
                          Context context, PublishingTarget target) throws PublishingException {
        String siteId = (!StringUtils.isEmpty(siteName))? siteName: parameters.get(FileUploadServlet.PARAM_SITE);
        ContentStoreService contentStoreService = target.getContentStoreService();

        List<String> createdFiles = changeSet.getCreatedFiles();
        List<String> updatedFiles = changeSet.getUpdatedFiles();
        List<String> deletedFiles = changeSet.getDeletedFiles();

        if (CollectionUtils.isNotEmpty(createdFiles)) {
            update(siteId, createdFiles, context, contentStoreService, false);
        }
        if (CollectionUtils.isNotEmpty(updatedFiles)) {
            update(siteId, updatedFiles, context, contentStoreService, false);
        }
        if (CollectionUtils.isNotEmpty(deletedFiles)) {
            update(siteId, deletedFiles, context, contentStoreService, true);
        }

        searchService.commit();
    }

    protected List<ItemProcessor> createDocumentProcessorChain(List<ItemProcessor> chain) {
        FieldRenamingProcessor processor = new FieldRenamingProcessor();
        if (MapUtils.isNotEmpty(fieldMappings)) {
            processor.setFieldMappings(fieldMappings);
        }

        chain.add(processor);

        return chain;
    }

    protected void update(String siteId, List<String> fileNames, Context context,
                          ContentStoreService contentStoreService, boolean delete) throws PublishingException {
        for (String fileName : fileNames) {
            if (fileName.endsWith(".xml")) {
                try {
                    if (delete) {
                        searchService.delete(siteId, fileName);

                        if (logger.isDebugEnabled()) {
                            logger.debug(siteId + ":" + fileName + " deleted from search index");
                        }
                    } else {
                        try {
                            String xml = processXml(fileName, context, contentStoreService);

                            searchService.update(siteId, fileName, xml, true);

                            if (logger.isDebugEnabled()) {
                                logger.debug(siteId + ":" + fileName + " added to search index");
                            }
                        } catch (DocumentException e) {
                            logger.warn("Cannot process XML file " + siteId + ":" + fileName + ". Continuing index " +
                                        "update...", e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Failed to send update of file " + siteId + ":" + fileName, e);
                }
            }
        }
    }

    protected String processXml(String fileName, Context context, ContentStoreService contentStoreService) throws DocumentException {
        String xml;

        Item item = contentStoreService.getItem(context, fileName);
        Document document = processDocument(item, context);
        xml = document.asXML();

        if (logger.isDebugEnabled()) {
            logger.debug("Processed XML:");
            logger.debug(xml);
        }

        return xml;
    }

    protected Document processDocument(Item item, Context context) throws DocumentException {
        return documentProcessor.process(context, null, item).getDescriptorDom();
    }


}

