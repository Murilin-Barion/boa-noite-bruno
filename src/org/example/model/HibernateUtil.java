package org.example.model;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.Session;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static void buildSessionFactory() {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Erro ao inicializar a SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static Session openSession() {
        if (sessionFactory == null) {
            buildSessionFactory();
        }
        return sessionFactory.openSession();
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}