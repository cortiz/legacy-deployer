package org.craftercms.cstudio.publishing.target;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.filesystem.FileSystemContentStoreAdapter;
import org.craftercms.cstudio.publishing.servlet.FileUploadServlet;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 * Utility class to hold a shared {@link ContentStoreService} & {@link Context}
 * @author joseross
 */
public class TargetContext {

    private static final Log logger = LogFactory.getLog(TargetContext.class);

    public static final String CONTEXT_ID_PARAM = "contextId";
    public static final String SITE_PLACEHOLDER = "{siteId}";

    protected String targetFolderUrl;

    protected ContentStoreService contentStoreService;

    @Required
    public void setTargetFolderUrl(String targetFolderUrl) {
        this.targetFolderUrl = targetFolderUrl;
    }

    @Required
    public void setContentStoreService(ContentStoreService contentStoreService) {
        this.contentStoreService = contentStoreService;
    }

    public ContentStoreService getContentStoreService() {
        return contentStoreService;
    }

    public Context getContext(Map<String, String> params) {
        if(logger.isDebugEnabled()) {
            logger.debug("Context requested");
        }
        if(params.containsKey(CONTEXT_ID_PARAM)) {
            return contentStoreService.getContext(params.get(CONTEXT_ID_PARAM));
        } else {
            Context context = createContext(params.get(FileUploadServlet.PARAM_SITE));
            params.put(CONTEXT_ID_PARAM, context.getId());
            return context;
        }
    }

    protected Context createContext(String siteName) {
        if(logger.isDebugEnabled()) {
            logger.debug("Context created for site " + siteName);
        }
        String resolvedUrl = StringUtils.replace(targetFolderUrl, SITE_PLACEHOLDER, siteName);
        return contentStoreService.createContext(
            FileSystemContentStoreAdapter.STORE_TYPE, null, null, null, resolvedUrl, false, 0,
            Context.DEFAULT_IGNORE_HIDDEN_FILES);
    }

    public void destroyContext(Map<String, String> params) {
        if(logger.isDebugEnabled()) {
            logger.debug("Context destroyed");
        }
        if(params.containsKey(CONTEXT_ID_PARAM)) {
            Context context = contentStoreService.getContext(params.get(CONTEXT_ID_PARAM));
            contentStoreService.destroyContext(context);
            params.remove(CONTEXT_ID_PARAM);
        }
    }

}
