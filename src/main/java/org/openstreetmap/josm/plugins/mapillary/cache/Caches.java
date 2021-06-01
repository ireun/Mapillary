// License: GPL. For details, see LICENSE file.
package org.openstreetmap.josm.plugins.mapillary.cache;

import java.io.File;
import java.io.Serializable;

import javax.swing.ImageIcon;

import org.apache.commons.jcs3.access.CacheAccess;
import org.apache.commons.jcs3.engine.behavior.IElementAttributes;

import org.openstreetmap.josm.data.cache.BufferedImageCacheEntry;
import org.openstreetmap.josm.data.cache.JCSCacheManager;
import org.openstreetmap.josm.plugins.mapillary.model.UserProfile;
import org.openstreetmap.josm.spi.preferences.Config;

public final class Caches {

  private Caches() {
    // Private constructor to avoid instantiation
  }

  public static File getCacheDirectory() {
    final File f = new File(Config.getDirs().getCacheDirectory(true) + "/Mapillary");
    if (!f.exists()) {
      f.mkdirs();
    }
    return f;
  }

  public abstract static class CacheProxy<K, V extends Serializable> {
    private final CacheAccess<K, V> cache;

    protected CacheProxy() {
      cache = createNewCache();
    }

    protected abstract CacheAccess<K, V> createNewCache();

    public V get(final K key) {
      return cache == null || key == null ? null : cache.get(key);
    }

    public void put(final K key, final V value) {
      if (cache != null) {
        cache.put(key, value);
      }
    }
  }

  /**
   * Caches for images
   */
  public static class ImageCache {
    private static final short MAX_DISK_IMAGES_SIZE = 10_000; // kb, ~50 full size images (average ~200 kb/image)
    private static final byte MAX_MEMORY_OBJECTS = 2;
    private static final CacheAccess<String, BufferedImageCacheEntry> THUMBNAIL_IMAGE_CACHE = JCSCacheManager.getCache(
      "mapillary:image:thumbnailImage", MAX_MEMORY_OBJECTS * 10, MAX_DISK_IMAGES_SIZE, getCacheDirectory().getPath());
    private static final CacheAccess<String, BufferedImageCacheEntry> FULL_IMAGE_CACHE = JCSCacheManager
      .getCache("mapillary:image:fullImage", MAX_MEMORY_OBJECTS, MAX_DISK_IMAGES_SIZE, getCacheDirectory().getPath());

    /**
     * Get the cache for the image type
     *
     * @param type The image type
     * @return The cache
     */
    public static CacheAccess<String, BufferedImageCacheEntry> getCache(MapillaryCache.Type type) {
      if (MapillaryCache.Type.THUMBNAIL.equals(type)) {
        return THUMBNAIL_IMAGE_CACHE;
      }
      return FULL_IMAGE_CACHE;
    }

    private ImageCache() {
      // No-op
    }
  }

  public static class MapObjectIconCache extends CacheProxy<String, ImageIcon> {
    private static CacheProxy<String, ImageIcon> instance;

    public static CacheProxy<String, ImageIcon> getInstance() {
      synchronized (MapObjectIconCache.class) {
        if (instance == null) {
          instance = new MapObjectIconCache();
        }
        return instance;
      }
    }

    @Override
    protected CacheAccess<String, ImageIcon> createNewCache() {
      return JCSCacheManager.getCache("mapillaryObjectIcons", 100, 1000, getCacheDirectory().getPath());
    }
  }

  public static class UserProfileCache extends CacheProxy<String, UserProfile> {
    private static CacheProxy<String, UserProfile> instance;

    public static CacheProxy<String, UserProfile> getInstance() {
      synchronized (UserProfileCache.class) {
        if (instance == null) {
          instance = new UserProfileCache();
        }
        return instance;
      }
    }

    @Override
    protected CacheAccess<String, UserProfile> createNewCache() {
      CacheAccess<String, UserProfile> cache = JCSCacheManager.getCache("userProfile", 100, 1000,
        getCacheDirectory().getPath());
      IElementAttributes atts = cache.getDefaultElementAttributes();
      atts.setMaxLife(604_800_000); // Sets lifetime to 7 days (604800000=1000*60*60*24*7)
      cache.setDefaultElementAttributes(atts);
      return cache;
    }
  }
}
