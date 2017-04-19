package org.b3log.symphony.service;

import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Topic;
import org.b3log.symphony.repository.TopicRepository;
import org.json.JSONObject;

import javax.inject.Inject;
import java.util.List;

/**
 * Created by Nataly Shilonosova on 4/19/2017.
 */
@Service
public class TopicService {

    private static final Logger LOGGER = Logger.getLogger(TopicService.class.getName());

    @Inject
    private TopicRepository topicRepository;

    public List<JSONObject> getAllTopics() throws ServiceException {

        try{
            List<JSONObject> list = topicRepository.getAll();
            return list;
        }
        catch (RepositoryException e){

            LOGGER.log(Level.ERROR, "Getting all topics failed", e);
            throw new ServiceException(e.getMessage());
        }
    }

    public JSONObject get(final String id) throws ServiceException {

        try{
            JSONObject result = topicRepository.get(id);
            return result;
        }
        catch (RepositoryException e){

            LOGGER.log(Level.ERROR, "Getting '" + id + "' topic failed", e);
            throw new ServiceException(e.getMessage());
        }
    }

}
