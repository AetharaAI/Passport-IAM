package org.passport.models.cache.infinispan.entities;

import org.passport.common.util.Time;
import org.passport.models.cache.CachedObject;


/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AbstractRevisioned implements Revisioned, CachedObject {
    private final String id;
    private Long revision;
    private final long cacheTimestamp = Time.currentTimeMillis();

    public AbstractRevisioned(Long revision, String id) {
        this.revision = revision;
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Long getRevision() {
        return revision;
    }

    @Override
    public void setRevision(Long revision) {
        this.revision = revision;
    }

    /**
     * When was this cached
     *
     * @return
     */
    @Override
    public long getCacheTimestamp() {
        return cacheTimestamp;
    }
}
