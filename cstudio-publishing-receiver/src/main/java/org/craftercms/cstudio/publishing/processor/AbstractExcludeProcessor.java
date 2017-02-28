package org.craftercms.cstudio.publishing.processor;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.cstudio.publishing.PublishedChangeSet;
import org.craftercms.cstudio.publishing.exception.PublishingException;
import org.craftercms.cstudio.publishing.target.PublishingTarget;
import org.springframework.beans.factory.annotation.Required;

import java.util.*;

/**
 * Base {@link PublishingProcessor} that excludes files from indexing.
 * @author joseross
 */
public abstract class AbstractExcludeProcessor extends AbstractPublishingProcessor {


    private static final Log logger = LogFactory.getLog(AbstractExcludeProcessor.class);

    /**
     * Actual processor to execute on the filtered files.
     */
    protected PublishingProcessor actualProcessor;

    @Required
    public void setActualProcessor(PublishingProcessor actualProcessor) {
        this.actualProcessor = actualProcessor;
    }

    /**
     * This method will define if a file is excluded or not.
     * @param file
     * @return
     */
    protected abstract boolean excludeFile(String file);

    protected void excludeFiles(List<String> files) {
        Iterator<String> iterator = files.iterator();
        while(iterator.hasNext()) {
            String file = iterator.next();
            if(excludeFile(file)) {
                iterator.remove();
            }
        }
    }

    @Override
    public void doProcess(PublishedChangeSet changeSet, Map<String, String> parameters, PublishingTarget target) throws PublishingException {
        List<String> createdFiles = copyFileList(changeSet.getCreatedFiles());
        List<String> updatedFiles = copyFileList(changeSet.getUpdatedFiles());
        List<String> deletedFiles = copyFileList(changeSet.getDeletedFiles());

        excludeFiles(createdFiles);
        excludeFiles(updatedFiles);
        excludeFiles(deletedFiles);

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

    protected List<String> copyFileList(List<String> files) {
        return CollectionUtils.isNotEmpty(files)? new ArrayList<>(files) : Collections.<String>emptyList();
    }
}
