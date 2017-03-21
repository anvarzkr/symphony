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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.inject.Inject;
import jodd.util.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.CharacterRepository;
import org.b3log.symphony.util.Results;
import org.b3log.symphony.util.Symphonys;
import org.b3log.symphony.util.Tesseracts;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Activity management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.5.9.6, Nov 1, 2016
 * @since 1.3.0
 */
@Service
public class ActivityMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ActivityMgmtService.class.getName());

    /**
     * Character repository.
     */
    @Inject
    private CharacterRepository characterRepository;

    /**
     * Activity query service.
     */
    @Inject
    private ActivityQueryService activityQueryService;

    /**
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    /**
     * Liveness management service.
     */
    @Inject
    private LivenessMgmtService livenessMgmtService;

    /**
     * Liveness query service.
     */
    @Inject
    private LivenessQueryService livenessQueryService;

    /**
     * Starts eating snake.
     *
     * @param userId the specified user id
     * @return result
     */
    public synchronized JSONObject startEatingSnake(final String userId) {
        final JSONObject ret = Results.falseResult();

        final boolean succ = true;

        ret.put(Keys.STATUS_CODE, succ);

        final String msg = succ ? "started" : langPropsService.get("activityStartEatingSnakeFailLabel");
        ret.put(Keys.MSG, msg);

        try {
            final JSONObject user = userQueryService.getUser(userId);
            final String userName = user.optString(User.USER_NAME);

            // Timeline
            final JSONObject timeline = new JSONObject();
            timeline.put(Common.USER_ID, userId);
            timeline.put(Common.TYPE, Common.ACTIVITY);
            String content = langPropsService.get("timelineActivityEatingSnakeLabel");
            content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                    + "/member/" + userName + "'>" + userName + "</a>").replace("${servePath}", Latkes.getServePath());
            timeline.put(Common.CONTENT, content);

            timelineMgmtService.addTimeline(timeline);

            // Liveness
            livenessMgmtService.incLiveness(userId, Liveness.LIVENESS_ACTIVITY);
        } catch (final ServiceException e) {
            LOGGER.log(Level.ERROR, "Timeline error", e);
        }

        return ret;
    }

    /**
     * Collects eating snake.
     *
     * @param userId the specified user id
     * @param score the specified score
     * @return result
     */
    public synchronized JSONObject collectEatingSnake(final String userId, final int score) {
        final JSONObject ret = Results.falseResult();

        if (score < 1) {
            ret.put(Keys.STATUS_CODE, true);

            return ret;
        }

        final boolean succ = true;

        ret.put(Keys.STATUS_CODE, succ);

        return ret;
    }

    /**
     * Submits the specified character to recognize.
     *
     * @param userId the specified user id
     * @param characterImg the specified character image encoded by Base64
     * @param character the specified character
     * @return recognition result
     */
    public synchronized JSONObject submitCharacter(final String userId, final String characterImg, final String character) {
        String recongnizeFailedMsg = langPropsService.get("activityCharacterRecognizeFailedLabel");

        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, false);
        ret.put(Keys.MSG, recongnizeFailedMsg);

        if (StringUtils.isBlank(characterImg) || StringUtils.isBlank(character)) {
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, recongnizeFailedMsg);

            return ret;
        }

        final byte[] data = Base64.decode(characterImg);
        OutputStream stream = null;
        final String tmpDir = System.getProperty("java.io.tmpdir");
        final String imagePath = tmpDir + "/" + userId + "-character.png";

        try {
            stream = new FileOutputStream(imagePath);
            stream.write(data);
            stream.flush();
            stream.close();
        } catch (final IOException e) {
            LOGGER.log(Level.ERROR, "Submits character failed", e);

            return ret;
        } finally {
            if (null != stream) {
                try {
                    stream.close();
                } catch (final IOException ex) {
                    LOGGER.log(Level.ERROR, "Closes stream failed", ex);
                }
            }
        }

        final String recognizedCharacter = Tesseracts.recognizeCharacter(imagePath);
        LOGGER.info("Character [" + character + "], recognized [" + recognizedCharacter + "], image path [" + imagePath
                + "]");
        if (StringUtils.equals(character, recognizedCharacter)) {
            final Query query = new Query();
            query.setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_USER_ID, FilterOperator.EQUAL, userId),
                    new PropertyFilter(org.b3log.symphony.model.Character.CHARACTER_CONTENT, FilterOperator.EQUAL, character)
            ));

            try {
                if (characterRepository.count(query) > 0) {
                    return ret;
                }
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Count characters failed [userId=" + userId + ", character=" + character + "]", e);

                return ret;
            }

            final JSONObject record = new JSONObject();
            record.put(org.b3log.symphony.model.Character.CHARACTER_CONTENT, character);
            record.put(org.b3log.symphony.model.Character.CHARACTER_IMG, characterImg);
            record.put(org.b3log.symphony.model.Character.CHARACTER_USER_ID, userId);

            String characterId = "";
            final Transaction transaction = characterRepository.beginTransaction();
            try {
                characterId = characterRepository.add(record);

                transaction.commit();
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Submits character failed", e);

                if (null != transaction) {
                    transaction.rollback();
                }

                return ret;
            }

            try {
                final JSONObject user = userQueryService.getUser(userId);
                final String userName = user.optString(User.USER_NAME);

                // Timeline
                final JSONObject timeline = new JSONObject();
                timeline.put(Common.USER_ID, userId);
                timeline.put(Common.TYPE, Common.ACTIVITY);
                String content = langPropsService.get("timelineActivityCharacterLabel");
                content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='" + Latkes.getServePath()
                        + "/member/" + userName + "'>" + userName + "</a>").replace("${servePath}", Latkes.getServePath());
                timeline.put(Common.CONTENT, content);

                timelineMgmtService.addTimeline(timeline);
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Submits character timeline failed", e);
            }

            ret.put(Keys.STATUS_CODE, true);
            ret.put(Keys.MSG, langPropsService.get("activityCharacterRecognizeSuccLabel"));
        } else {
            recongnizeFailedMsg = recongnizeFailedMsg.replace("{一}", recognizedCharacter);
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, recongnizeFailedMsg);
        }

        return ret;
    }

}
