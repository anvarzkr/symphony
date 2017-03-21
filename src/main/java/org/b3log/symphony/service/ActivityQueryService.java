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
package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Activity query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.1.5, Sep 23, 2016
 * @since 1.3.0
 */
@Service
public class ActivityQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActivityQueryService.class.getName());

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Does checkin today?
     *
     * @param userId the specified user id
     * @return {@code true} if checkin succeeded, returns {@code false} otherwise
     */
    public synchronized boolean isCheckedinToday(final String userId) {
        Stopwatchs.start("Checks checkin");
        try {
            final Calendar calendar = Calendar.getInstance();
            final int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour < Symphonys.getInt("activityDailyCheckinTimeMin")
                    || hour > Symphonys.getInt("activityDailyCheckinTimeMax")) {
                return true;
            }

            final Date now = new Date();

            final JSONObject user = userRepository.get(userId);

            final long time = user.optLong(UserExt.USER_CHECKIN_TIME);
            return DateUtils.isSameDay(now, new Date(time));
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Checks checkin failed", e);

            return true;
        } finally {
            Stopwatchs.end();
        }
    }


}
