package server.repository.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import server.model.GlobalChat;
import server.repository.GlobalChatRepository;


public class GlobalChatRepositoryImpl implements GlobalChatRepository {

    private SessionFactory sessionFactory;
    private final Session session;

    public GlobalChatRepositoryImpl(Session session){
        this.session = session;
    }



    @Override
    public void saveEntity(GlobalChat entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.update(entity);
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
            GlobalChat globalChat = getEntityById(entity.getClientId());
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
            String hql = "FROM GlobalChat c WHERE c.id = :aLong";
            Query<GlobalChat> query = session.createQuery(hql, GlobalChat.class);
            query.setParameter("id", aLong);
            GlobalChat globalChat = query.getSingleResult();
            transaction.commit();
            return globalChat;
        }catch (HibernateException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

}
