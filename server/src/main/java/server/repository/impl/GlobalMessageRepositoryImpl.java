package server.repository.impl;

import ch.qos.logback.classic.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.LoggerFactory;
import server.model.GlobalChat;
import server.model.GlobalMessage;
import server.repository.GlobalMessageRepository;

public class GlobalMessageRepositoryImpl implements GlobalMessageRepository {
    private final Session session;
    private Transaction transaction;
    private final Logger logger = (Logger) LoggerFactory.getLogger(GlobalMessageRepositoryImpl.class);

    public GlobalMessageRepositoryImpl(Session session){
        this.session = session;
    }

    @Override
    public void saveEntity(GlobalMessage entity) {
        try{
            transaction = session.beginTransaction();
            session.save(entity);
            transaction.commit();
        }catch (HibernateException e){
            catchErrors(e, "GlobalMessageRepositoryImpl.saveEntity");
            transaction.rollback();

        }

    }

    @Override
    public void updateEntity(GlobalMessage entity) {
        try{
            transaction = session.beginTransaction();
            logger.info("Обновляем globalMessages: {}", entity);
            session.update(entity);
            transaction.commit();
        }catch (HibernateException e){
            catchErrors(e, "GlobalMessageRepositoryImpl.updateEntity");
            transaction.rollback();
        }

    }

    @Override
    public void deleteEntity(GlobalMessage entity) {
        try{
            transaction = session.beginTransaction();
            session.delete(entity);
            transaction.commit();
        }catch (HibernateException e){
            catchErrors(e, "GlobalMessageRepositoryImpl.deleteEntity");
            transaction.rollback();
        }
    }

    @Override
    public GlobalMessage getEntityById(GlobalChat globalChat) {
        try{
            transaction = session.beginTransaction();
            String hql = "FROM GlobalMessage g WHERE g.globalChat = :globalChat";
            Query<GlobalMessage> query = session.createQuery(hql, GlobalMessage.class);
            query.setParameter("globalChat", globalChat);
            GlobalMessage globalMessage = query.getSingleResult();
            transaction.commit();
            return globalMessage;
        }catch (HibernateException e){
            catchErrors(e, "GlobalMessageRepositoryImpl.getEntityById");
            transaction.rollback();
            return null;
        }
    }


    private void catchErrors(HibernateException e, String method){
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()){
            sb.append(element).append("\n");
        }
        logger.info("ERROR: возникла в методе {}. Причина: {}\nStackTrace: {}", method, e.getMessage(), sb);
    }
}
