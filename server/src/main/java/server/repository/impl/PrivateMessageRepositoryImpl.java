package server.repository.impl;


import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import server.model.PrivateChat;
import server.model.PrivateMessage;
import server.repository.PrivateMessageRepository;

public class PrivateMessageRepositoryImpl implements PrivateMessageRepository {
    private final Session session;


    public PrivateMessageRepositoryImpl(Session session){
        this.session = session;
    }



    @Override
    public void saveEntity(PrivateMessage entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.save(entity);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void updateEntity(PrivateMessage entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void deleteEntity(PrivateMessage entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.delete(entity);
            transaction.commit();
        }catch (HibernateException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public PrivateMessage getEntityById(PrivateChat privateChat) {
        try{
            Transaction transaction = session.beginTransaction();
            String hql = "FROM PrivateMessage p WHERE i.privateChat = :privateChat";
            Query<PrivateMessage> query = session.createQuery(hql, PrivateMessage.class);
            query.setParameter("privateChat", privateChat);
            PrivateMessage privateMessage = query.getSingleResult();
            transaction.commit();
            return privateMessage;
        }catch (HibernateException e){
            System.out.println(e.getMessage());
            return null;
        }
    }
}
