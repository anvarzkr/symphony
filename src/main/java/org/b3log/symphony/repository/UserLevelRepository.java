package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.cache.UserLevelCache;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserLevel;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Nataly Shilonosova on 3/28/2017.
 */
@Repository
public class UserLevelRepository extends AbstractRepository {

    @Inject
    private UserLevelCache userLevelCache;

    public UserLevelRepository() {
        super(User.USER);
    }


    public JSONObject get(final String userId) throws RepositoryException {

        JSONObject userLevel = userLevelCache.getUserLevel(userId);
        if (userLevel != null) {
            return userLevel;
        }



        final List<JSONObject> result = select("SELECT CAST(SUM(res) AS signed) as exp " +
                "FROM (" +
                "SELECT CASE WHEN 200 + 5*(articleGoodCnt-articleBadCnt) + " +
                "10*articleCommentCount + " +
                "20*articleWatchCnt + 20 * articleCollectCnt < 0 " +
                "THEN 0 " +
                "ELSE 200 + 5*(articleGoodCnt-articleBadCnt) + " +
                "10*articleCommentCount + 20*articleWatchCnt + 20 * articleCollectCnt " +
                "END res " +
                "FROM b3log_symphony.symphony_article WHERE articleAuthorId='"+ userId +"' " +
                "UNION " +
                "(SELECT CASE " +
                "WHEN 10 + 10*(commentGoodCnt - commentBadCnt) < 0 THEN 0 " +
                "ELSE 10 + 10*(commentGoodCnt - commentBadCnt) " +
                "END res " +
                "FROM b3log_symphony.symphony_comment " +
                "WHERE commentAuthorId='"+ userId +"')" +
                ") AS t\n");
        if (!result.isEmpty()) {
            int exp = result.get(0).optInt("exp", 0);
            int level = 1 + exp/1000;
            userLevel = new JSONObject();
            userLevel.put(UserLevel.USER_ID, userId);
            userLevel.put(UserLevel.EXPERIENCE, exp);
            userLevel.put(UserLevel.LEVEL, level);
            userLevel.put(UserLevel.NEXT_LEVEL_EXPERIENCE, level*1000);
        }

        if (null == userLevel) {
            return null;
        }
        userLevelCache.putUserLevel(userLevel);
        return userLevel;
    }

    public void update(final String userId, final JSONObject userLevel) throws RepositoryException {

        userLevel.put(Keys.OBJECT_ID, userId);
        userLevelCache.putUserLevel(userLevel);
    }
}