package org.craftercms.cstudio.publishing.processor;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.cstudio.publishing.PublishedChangeSet;
import org.craftercms.cstudio.publishing.exception.PublishingException;
import org.craftercms.cstudio.publishing.target.PublishingTarget;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

/**
 * Scans the updated files to detect level descriptors and adds the possible affected children to be indexed.
 * @author joseross
 */
public class ReIndexPagesOnLevelDescriptorUpdateProcessor extends AbstractPublishingProcessor {

    private static final Log logger = LogFactory.getLog(ReIndexPagesOnLevelDescriptorUpdateProcessor.class);

    public static final String DEFAULT_FILE_NAME = "crafter-level-descriptor.level.xml";

    protected String fileName = DEFAULT_FILE_NAME;

    protected PublishingProcessor actualProcessor;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Required
    public void setActualProcessor(PublishingProcessor actualProcessor) {
        this.actualProcessor = actualProcessor;
    }

    protected List<String> findChangedLevelDescriptors(List<String> files) {
        if(files == null) {
            return Collections.emptyList();
        }

        List<String> updatedFiles = new ArrayList<>();
        for(String file : files) {
            if(file.endsWith(fileName)) {
                updatedFiles.add(file);
            }
        }
        return updatedFiles;
    }

    protected void addChangedChildPages(ContentStoreService contentStoreService, Context context, Item item,
                                        List<String> updatedFiles) {
        if(item.isFolder()) {
            List<Item> children = contentStoreService.getChildren(context, item.getUrl());
            for(Item child : children) {
                addChangedChildPages(contentStoreService, context, child, updatedFiles);
            }
        } else if(!updatedFiles.contains(item.getUrl())) {
            if(logger.isDebugEnabled()) {
                logger.debug("Page " + item + " affected by update on level descriptor, will be reindexed.");
            }

            updatedFiles.add(item.getUrl());
        }
    }

    protected void addChangedChildPages(List<String> levelDescriptors, List<String> updatedFiles,
                                        ContentStoreService contentStoreService, Context context) {
        for (String levelDescriptor : levelDescriptors) {
            String parentPath = FilenameUtils.getFullPath(levelDescriptor);
            Item item = contentStoreService.getItem(context, parentPath);
            addChangedChildPages(contentStoreService, context, item, updatedFiles);
        }
    }

    @Override
    public void doProcess(PublishedChangeSet changeSet, Map<String, String> parameters, Context context,
                          PublishingTarget target) throws PublishingException {
        ContentStoreService contentStoreService = target.getContentStoreService();

        List<String> createdFiles = changeSet.getCreatedFiles();
        List<String> updatedFiles = changeSet.getUpdatedFiles();
        List<String> deletedFiles = changeSet.getDeletedFiles();

        List<String> newUpdatedFiles = new LinkedList<>();
        if(updatedFiles != null) {
            newUpdatedFiles.addAll(updatedFiles);
        }

        List<String> createdLevelDescriptors = findChangedLevelDescriptors(createdFiles);
        List<String> updatedLevelDescriptors = findChangedLevelDescriptors(updatedFiles);
        List<String> deletedLevelDescriptors = findChangedLevelDescriptors(deletedFiles);

        if(!createdLevelDescriptors.isEmpty() ||
            !updatedLevelDescriptors.isEmpty() ||
            !deletedLevelDescriptors.isEmpty()) {
            addChangedChildPages(createdLevelDescriptors, newUpdatedFiles, contentStoreService, context);
            addChangedChildPages(updatedLevelDescriptors, newUpdatedFiles, contentStoreService, context);
            addChangedChildPages(deletedLevelDescriptors, newUpdatedFiles, contentStoreService, context);
        }

        actualProcessor.doProcess(new PublishedChangeSet(createdFiles, newUpdatedFiles, deletedFiles), parameters, context, target);
    }

    @Override
    public String getName() {
        return ReIndexPagesOnLevelDescriptorUpdateProcessor.class.getSimpleName();
    }
}