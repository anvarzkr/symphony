package org.b3log.symphony.repository;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.cache.TopicCache;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.Topic;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.ServerException;
import java.util.List;

/**
 * Created by Nataly Shilonosova on 4/19/2017.
 */
@Repository
public class TopicRepository extends AbstractRepository {

    @Inject
    private TopicCache topicCache;
    private static final Logger LOGGER = Logger.getLogger(TagCache.class.getName());

    public TopicRepository() {
        super(Topic.TOPIC);
    }

    public List<JSONObject> getAll() throws RepositoryException {

        final Query query = new Query();

        final JSONObject result = get(query);
        final JSONArray array = result.optJSONArray(Keys.RESULTS);

        return CollectionUtils.jsonArrayToList(array);
    }

    @Override
    public JSONObject get(final String id) throws RepositoryException {

        JSONObject result = topicCache.getTopic(id);
        if (result != null){
            return result;
        }

        result = super.get(id);
        if (result!=null){

            topicCache.putTopic(result);
        }

        return result;

    }

    @Override
    public String add(final JSONObject topic) throws RepositoryException {

        final Transaction transaction = beginTransaction();
        try {

            final String ret = super.add(topic);

            topicCache.putTopic(topic);

            transaction.commit();
            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Migrates tag data failed", e);
            throw new RepositoryException(e);
        }

    }

}
