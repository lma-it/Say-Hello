package server.util;


import ch.qos.logback.classic.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class SessionService {

    public static synchronized SessionFactory getSessionFactory(Logger logger, String clazz){
        Configuration configuration = new Configuration();
        configuration.configure();
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        logger.info("Состояние sessionFactory: {} из класса: {}", sessionFactory, clazz);
        return sessionFactory;
    }

}
