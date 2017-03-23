package org.craftercms.cstudio.publishing.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.craftercms.core.service.Context;
import org.craftercms.core.store.impl.filesystem.FileSystemContentStoreAdapter;
import org.craftercms.cstudio.publishing.servlet.FileUploadServlet;
import org.craftercms.cstudio.publishing.target.PublishingTarget;

/**
 * Created by alfonso on 3/20/17.
 */
public class ContextUtils {

    private static Log LOGGER = LogFactory.getLog(ContextUtils.class);

    public ContextUtils() {
    }

    public static Context createContext(PublishingTarget target, String siteName) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Context created for site " + siteName);
        }

        String resolvedUrl = StringUtils.replace(target.getTargetFolder(), FileUploadServlet.PARAM_SITE, siteName);

        return target.getContentStoreService().createContext(
            FileSystemContentStoreAdapter.STORE_TYPE, null, null, null, resolvedUrl, false, 0,
            Context.DEFAULT_IGNORE_HIDDEN_FILES);
    }

    public static void destroyContext(PublishingTarget target, Context context) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Context destroyed");
        }

        target.getContentStoreService().destroyContext(context);
    }

}
