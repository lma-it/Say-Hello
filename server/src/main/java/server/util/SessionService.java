package server.util;


import ch.qos.logback.classic.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class SessionService {

    public static synchronized SessionFactory getSessionFactory(Logger logger){
        Configuration configuration = new Configuration();
        configuration.configure();
        SessionFactory sessionFactory = configuration.buildSessionFactory();
        logger.info("Состояние sessionFactory: {}", sessionFactory);
        return sessionFactory;
    }

}
