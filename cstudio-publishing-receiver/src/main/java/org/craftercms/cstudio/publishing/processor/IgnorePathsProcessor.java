package org.craftercms.cstudio.publishing.processor;

import org.craftercms.cstudio.publishing.PublishedChangeSet;
import org.craftercms.cstudio.publishing.exception.PublishingException;
import org.craftercms.cstudio.publishing.target.PublishingTarget;
import org.springframework.beans.factory.annotation.Required;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by joseross on 2/27/17.
 */
public class IgnorePathsProcessor extends AbstractPublishingProcessor {

    protected String[] ignoredPaths;
    protected PublishingProcessor actualProcessor;

    @Required
    public void setIgnoredPaths(String[] ignoredPaths) {
        this.ignoredPaths = ignoredPaths;
    }

    @Required
    public void setActualProcessor(PublishingProcessor actualProcessor) {
        this.actualProcessor = actualProcessor;
    }

    @Override
    public void doProcess(PublishedChangeSet changeSet, Map<String, String> parameters, PublishingTarget target) throws PublishingException {
        List<String> createdFiles = new LinkedList<>(changeSet.getCreatedFiles());
        List<String> updatedFiles = new LinkedList<>(changeSet.getUpdatedFiles());
        List<String> deletedFiles = new LinkedList<>(changeSet.getDeletedFiles());

        Stream.of(ignoredPaths).forEach(path -> {
            createdFiles.stream().filter(path::matches);
        });

    }
}
