package org.craftercms.cstudio.publishing.processor;

import org.craftercms.commons.lang.RegexUtils;

import org.springframework.beans.factory.annotation.Required;

/**
 * {@link PublishingProcessor} decorator that excludes files based on the path patterns.
 * @author joseross
 */
public class ExcludeByPathProcessor extends AbstractExcludeProcessor {

    /**
     * Array of patterns to exclude.
     */
    protected String[] excludedPaths;

    @Required
    public void setExcludedPaths(String[] excludedPaths) {
        this.excludedPaths = excludedPaths;
    }

    @Override
    protected boolean excludeFile(String file) {
        return RegexUtils.matchesAny(file, excludedPaths);
    }

    @Override
    public String getName() {
        return ExcludeByPathProcessor.class.getSimpleName();
    }

}
