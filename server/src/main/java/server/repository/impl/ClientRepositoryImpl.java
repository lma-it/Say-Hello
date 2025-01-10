package server.repository.impl;

import ch.qos.logback.classic.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.LoggerFactory;
import server.model.Client;
import server.repository.ClientRepository;

import org.hibernate.query.Query;
import java.util.List;

public class ClientRepositoryImpl implements ClientRepository {
    private final Logger logger = (Logger) LoggerFactory.getLogger(ClientRepositoryImpl.class);
    private final Session session;

    public ClientRepositoryImpl(Session session){
        this.session = session;
    }




    @Override
    public void saveEntity(Client entity) {
        try{
            Transaction transaction = session.beginTransaction();
            logger.info("Сохранение сущности: {}", entity);
            session.save(entity);
            transaction.commit();
        }catch (HibernateException e){
            catchErrors(e, "saveEntity");
        }
    }


    public List<Client> getAllEntities() {
        try{
            Transaction transaction = session.beginTransaction();
            String hql = "FROM Client";
            logger.info("Выполняется запрос: {}", hql);
            List<Client> clients = session.createQuery(hql, Client.class).getResultList();
            logger.info("Получен список клиентов: {}", clients);
            transaction.commit();
            return clients;
        }catch (HibernateException e){
            catchErrors(e, "getAllEntities");
            return null;
        }
    }

    @Override
    public void updateEntity(Client entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
        }catch (HibernateException e){
            catchErrors(e, "updateEntity");
        }
    }

    @Override
    public void deleteEntity(Client entity) {
        try{
            Transaction transaction = session.beginTransaction();
            session.delete(entity);
            transaction.commit();
        }catch (HibernateException e){
            catchErrors(e, "deleteEntity");
        }
    }

    @Override
    public Client getEntityById(Long aLong) {
        try{
            Transaction transaction = session.beginTransaction();
            String hql = "FROM Client c WHERE c.id = " + aLong.toString();
            Client client = session.createQuery(hql, Client.class).getSingleResult();
            transaction.commit();
            return client;
        }catch (HibernateException e){
            catchErrors(e, "getEntityById");
            return null;
        }
    }

    @Override
    public Client getEntityByLoginAndPassword(String login, String password){
        try{
            Transaction transaction = session.beginTransaction();
            String hql = "FROM Client c WHERE c.login = :login AND c.password = :password";
            Query<Client> query = session.createQuery(hql, Client.class);
            query.setParameter("login", login);
            query.setParameter("password", password);
            logger.info("Данные о логине: {}, пароль: {}", login, password);
            Client client = query.getSingleResult();
            transaction.commit();
            return client;
        }catch (HibernateException e){
            catchErrors(e, "getEntityByLoginAndPassword");
            return null;
        }
    }

    private void catchErrors(HibernateException e, String method){
        StringBuilder sb = new StringBuilder();
        for(StackTraceElement element : e.getStackTrace()){
            sb.append(element).append("\n");
        }
        logger.error("ERROR: возникла в методе {}. Причина: {}\nStackTrace: {}", method, e.getMessage(), sb);
    }
}
