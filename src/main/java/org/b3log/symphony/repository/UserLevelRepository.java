package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.symphony.cache.UserLevelCache;
import org.b3log.symphony.model.Common;
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

        //TODO: SQL query
        final List<JSONObject> result = select("SELECT\n"
                + "	AVG(sum) AS point\n"
                + "FROM\n"
                + "	`" + getName() + "`\n"
                + "WHERE\n"
                + "	type = 27\n"
                + "AND toId = ?\n"
                + "", userId);
        if (!result.isEmpty()) {
            userLevel = result.get(0);
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