/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.cache;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.util.JSONs;
import org.json.JSONObject;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Nataly Shilonosova on 4/19/2017.
 */
@Named
@Singleton
public class TopicCache {

    private static final Logger LOGGER = Logger.getLogger(TopicCache.class.getName());

    private static final Map<String, JSONObject> CACHE = new ConcurrentHashMap<>();

    public JSONObject getTopic(final String id) {
        final JSONObject topic = CACHE.get(id);
        if (null == topic) {
            return null;
        }

        final JSONObject copy = JSONs.clone(topic);

        return copy;
    }

    public void putTopic(final JSONObject topic) {
        CACHE.put(topic.optString(Keys.OBJECT_ID), JSONs.clone(topic));

    }

}
