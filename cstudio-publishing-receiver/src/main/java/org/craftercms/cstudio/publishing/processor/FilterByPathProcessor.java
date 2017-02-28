package org.craftercms.cstudio.publishing.processor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.cstudio.publishing.PublishedChangeSet;
import org.craftercms.cstudio.publishing.exception.PublishingException;
import org.craftercms.cstudio.publishing.target.PublishingTarget;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

/**
 * {@link PublishingProcessor} decorator that skips files based on the path patterns.
 * @author joseross
 */
public class FilterByPathProcessor extends AbstractPublishingProcessor {

    private static final Log logger = LogFactory.getLog(FilterByPathProcessor.class);

    /**
     * Array of patterns to skip.
     */
    protected String[] filteredPaths;

    /**
     * Actual processor to execute on the filtered files.
     */
    protected PublishingProcessor actualProcessor;

    @Required
    public void setFilteredPaths(String[] filteredPaths) {
        this.filteredPaths = filteredPaths;
    }

    @Required
    public void setActualProcessor(PublishingProcessor actualProcessor) {
        this.actualProcessor = actualProcessor;
    }

    protected void filterFiles(List<String> files) {
        Iterator<String> iterator = files.iterator();
        while(iterator.hasNext()) {
            String file = iterator.next();
            if(RegexUtils.matchesAny(file, filteredPaths)) {
                iterator.remove();
            }
        }
    }

    @Override
    public void doProcess(PublishedChangeSet changeSet, Map<String, String> parameters, PublishingTarget target) throws PublishingException {
        List<String> createdFiles = copyFileList(changeSet.getCreatedFiles());
        List<String> updatedFiles = copyFileList(changeSet.getUpdatedFiles());
        List<String> deletedFiles = copyFileList(changeSet.getDeletedFiles());

        filterFiles(createdFiles);
        filterFiles(updatedFiles);
        filterFiles(deletedFiles);

        if(CollectionUtils.isNotEmpty(createdFiles) ||
            CollectionUtils.isNotEmpty(updatedFiles) ||
            CollectionUtils.isNotEmpty(deletedFiles)) {

            PublishedChangeSet newChangeSet = new PublishedChangeSet();
            newChangeSet.setCreatedFiles(createdFiles);
            newChangeSet.setUpdatedFiles(updatedFiles);
            newChangeSet.setDeletedFiles(deletedFiles);

            if (logger.isDebugEnabled()) {
                logger.debug("Executing publishing processor " + actualProcessor.getName() + " for " + newChangeSet);
            }

            actualProcessor.doProcess(newChangeSet, parameters, target);

        }

    }

    @Override
    public String getName() {
        return FilterByPathProcessor.class.getSimpleName();
    }

    protected List<String> copyFileList(List<String> files) {
        return CollectionUtils.isNotEmpty(files)? new ArrayList<>(files) : Collections.<String>emptyList();
    }

}
