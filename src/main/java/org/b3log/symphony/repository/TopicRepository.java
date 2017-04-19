package org.b3log.symphony.repository;

import org.b3log.latke.Keys;
import org.b3log.latke.repository.AbstractRepository;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.annotation.Repository;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.cache.TopicCache;
import org.b3log.symphony.model.Topic;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Nataly Shilonosova on 4/19/2017.
 */
@Repository
public class TopicRepository extends AbstractRepository {

    @Inject
    private TopicCache topicCache;

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

    /*@Override
    public String add(final JSONObject topic) throws RepositoryException {
        final String ret = super.add(topic);

        topicCache.putTopic(topic);

        return ret;
    }*/

}
