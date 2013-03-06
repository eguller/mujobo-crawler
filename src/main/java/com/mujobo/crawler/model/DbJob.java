package com.mujobo.crawler.model;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

/**
 * Created with IntelliJ IDEA. User: mac Date: 1/31/13 Time: 1:25 PM To change
 * this template use File | Settings | File Templates.
 */
public abstract class DbJob {
	public EntityManager em;

	public DbJob(EntityManager em) {
		this.em = em;
	}

	public void beginTx() {
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
		}
	}

	public void endTx() {
		if (em.getTransaction().isActive()) {
			em.getTransaction().commit();
		}
	}

	public void rollback(){
		if(em.getTransaction().isActive()){
			em.getTransaction().rollback();
		}
	}
	
	public void save(Object obj) throws Exception{
		try {
			beginTx();
			em.persist(obj);
			endTx();
		} catch (Exception ex) {
			rollback();
			throw ex;
		}

	}
}
