package org.b3log.symphony.cache;

import org.b3log.latke.Keys;
import org.b3log.latke.cache.Cache;
import org.b3log.latke.cache.CacheFactory;
import org.b3log.symphony.model.UserLevel;
import org.b3log.symphony.util.JSONs;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by Nataly Shilonosova on 3/28/2017.
 */
@Named
@Singleton
public class UserLevelCache {

    private static final Cache USER_LEVEL_CACHE = CacheFactory.getCache(UserLevel.USER_LEVELS);
    static {
        USER_LEVEL_CACHE.setMaxCount(Symphonys.getInt("cache.userLevelCnt"));
    }

    public JSONObject getUserLevel(final String userId){

        final JSONObject userLevel = (JSONObject) USER_LEVEL_CACHE.get(userId);
        if (userLevel == null){
            return null;
        }
        return JSONs.clone(userLevel);
    }

    public void putUserLevel(final JSONObject userLevel){

        final String userId = userLevel.optString(Keys.OBJECT_ID);
        USER_LEVEL_CACHE.put(userId, JSONs.clone(userLevel));

    }

}