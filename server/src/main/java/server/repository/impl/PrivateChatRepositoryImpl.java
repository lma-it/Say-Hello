package server.repository.impl;

import ch.qos.logback.classic.Logger;
import lombok.extern.java.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.LoggerFactory;
import server.model.Client;
import server.model.PrivateChat;
import server.repository.PrivateChatRepository;


public class PrivateChatRepositoryImpl implements PrivateChatRepository {
    private final Logger logger = (Logger) LoggerFactory.getLogger(PrivateChatRepositoryImpl.class);

    private final Session session;

    public PrivateChatRepositoryImpl(Session session){
        this.session = session;
    }


    @Override
    public void saveEntity(PrivateChat entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.save(entity);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateEntity(PrivateChat entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteEntity(PrivateChat entity) {
        try{
            Transaction transaction = session.beginTransaction();
            PrivateChat privateChat = getEntityById(entity.getClient().getClientId());
            session.delete(privateChat);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public PrivateChat getEntityById(Long aLong) {
        try{
            Transaction transaction = session.beginTransaction();
            String hql = "FROM PrivateChat p WHERE p.privateId = :aLong";
            Query<PrivateChat> query = session.createQuery(hql, PrivateChat.class);
            query.setParameter("privateId", aLong);
            PrivateChat privateChat = query.getSingleResult();
            transaction.commit();
            return privateChat;
        }catch (HibernateException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public PrivateChat getChatByClient(Client client) {
        try{
            Transaction transaction = session.beginTransaction();
            String hql = "FROM PrivateChat p WHERE p.client = :client";
            Query<PrivateChat> query = session.createQuery(hql, PrivateChat.class);
            query.setParameter("client", client);
            PrivateChat privateChat = query.getSingleResult();
            transaction.commit();
            return privateChat;
        }catch (HibernateException e){
            catchErrors(e, "PrivateChatRepositoryImpl.getChatByClient");
            return null;
        }
    }


    private void catchErrors(Exception e, String method){
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()){
            sb.append(element).append("\n");
        }
        logger.error("ERROR: В методе {}. Причина: {}\nStackTrace: {}", method, e.getMessage(), sb);
    }
}
