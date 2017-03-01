package org.craftercms.cstudio.publishing.processor;

import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.service.Item;
import org.craftercms.core.store.impl.filesystem.FileSystemContentStoreAdapter;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.PostConstruct;

/**
 * {@link PublishingProcessor} decorator that excludes files based on the value of a field.
 * @author joseross
 */
public class ExcludeByFieldValueProcessor extends AbstractExcludeProcessor {

    /**
     * Field to check.
     */
    protected String fieldName;

    /**
     * Values that should cause a file to be excluded.
     */
    protected String[] excludedValues;

    protected String targetFolderUrl;
    protected ContentStoreService contentStoreService;
    protected Context context;

    @Required
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    @Required
    public void setExcludedValues(String[] excludedValues) {
        this.excludedValues = excludedValues;
    }

    @Required
    public void setTargetFolderUrl(String targetFolderUrl) {
        this.targetFolderUrl = targetFolderUrl;
    }

    @Required
    public void setContentStoreService(ContentStoreService contentStoreService) {
        this.contentStoreService = contentStoreService;
    }

    @PostConstruct
    public void init() {
        context = contentStoreService.createContext(
            FileSystemContentStoreAdapter.STORE_TYPE, null, null, null, targetFolderUrl, false, 0,
            Context.DEFAULT_IGNORE_HIDDEN_FILES);
    }

    @PostConstruct
    public void destroy() {
        contentStoreService.destroyContext(context);
    }

    @Override
    protected boolean excludeFile(String file) {
        Item item = contentStoreService.getItem(context, file);
        if(item != null) {
            String fieldValue = item.queryDescriptorValue(fieldName);
            if(StringUtils.isNotEmpty(fieldValue)) {
                return RegexUtils.matchesAny(fieldValue, excludedValues);
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return ExcludeByFieldValueProcessor.class.getSimpleName();
    }
}
