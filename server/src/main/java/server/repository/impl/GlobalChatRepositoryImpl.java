package server.repository.impl;

import ch.qos.logback.classic.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.LoggerFactory;
import server.model.Client;
import server.model.GlobalChat;
import server.repository.GlobalChatRepository;


public class GlobalChatRepositoryImpl implements GlobalChatRepository {
    private final Logger logger = (Logger) LoggerFactory.getLogger(GlobalChatRepositoryImpl.class);

    private final Session session;

    public GlobalChatRepositoryImpl(Session session){
        this.session = session;
    }



    @Override
    public void saveEntity(GlobalChat entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.save(entity);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void updateEntity(GlobalChat entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void deleteEntity(GlobalChat entity) {
        try{
            Transaction transaction = session.beginTransaction();
            GlobalChat globalChat = getEntityById(entity.getClient().getClientId());
            session.delete(globalChat);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public GlobalChat getEntityById(Long aLong) {
        try{
            Transaction transaction = session.beginTransaction();
            String hql = "FROM GlobalChat g WHERE g.globalId = :aLong";
            Query<GlobalChat> query = session.createQuery(hql, GlobalChat.class);
            query.setParameter("globalId", aLong);
            GlobalChat globalChat = query.getSingleResult();
            transaction.commit();
            return globalChat;
        }catch (HibernateException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public GlobalChat getChatByClient(Client client) {
        try{
            Transaction transaction = session.beginTransaction();
            String hql = "FROM GlobalChat g WHERE g.client = :client";
            Query<GlobalChat> query = session.createQuery(hql, GlobalChat.class);
            query.setParameter("client", client);
            GlobalChat globalChat = query.getSingleResult();
            logger.info("Информация о globalChat: {}", globalChat.toString());
            transaction.commit();
            return globalChat;
        }catch (HibernateException e){
            catchErrors(e, "GlobalChatRepositoryImpl.getChatByClient");
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
