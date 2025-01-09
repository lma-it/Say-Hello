package server.repository.impl;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import server.model.PrivateChat;
import server.repository.PrivateChatRepository;


public class PrivateChatRepositoryImpl implements PrivateChatRepository {

    private SessionFactory sessionFactory;
    private final Session session;

    public PrivateChatRepositoryImpl(Session session){
        this.session = session;
    }


    @Override
    public void saveEntity(PrivateChat entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.update(entity);
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
            PrivateChat privateChat = getEntityById(entity.getClientId());
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
            String hql = "FROM PrivateChat p WHERE p.id = :aLong";
            Query<PrivateChat> query = session.createQuery(hql, PrivateChat.class);
            query.setParameter("id", aLong);
            PrivateChat privateChat = query.getSingleResult();
            transaction.commit();
            return privateChat;
        }catch (HibernateException e){
            System.out.println(e.getMessage());
            return null;
        }
    }

}
