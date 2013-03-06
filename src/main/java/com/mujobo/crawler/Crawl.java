package com.mujobo.crawler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogManager;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.mujobo.crawler.crawlers.KariyerCrawler;
import com.mujobo.crawler.crawlers.KariyerIndexer;
import org.h2.tools.Server;
import org.reflections.Reflections;

public class Crawl {
	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		startDB();
		EntityManagerFactory entityManagerFactory = null;
        try {
            entityManagerFactory = 
            	     Persistence.createEntityManagerFactory("crawler");
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
        KariyerIndexer kariyerIndexer = new KariyerIndexer(entityManagerFactory.createEntityManager());
        KariyerCrawler kariyerCrawler = new KariyerCrawler(entityManagerFactory.createEntityManager());
        new Thread(kariyerIndexer).start();
        new Thread(kariyerCrawler).start();
	}
	
	public static void startDB(){
		try {
			Server server = Server.createWebServer();
			server.start();
			Class.forName("org.h2.Driver");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<Runnable> loadCrawlers(){
		List<Runnable> list = new ArrayList<Runnable>();
		Reflections reflections = new Reflections("com.mujobo.crawler.crawlers");
		Set<Class<? extends Runnable>> crawlerSet = reflections.getSubTypesOf(Runnable.class);
		for(Class<? extends Runnable> rClazz : crawlerSet){
			try {
				Runnable r = rClazz.newInstance();
				list.add(r);
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public static void startCrawlers(List<Runnable> crawlerList){
		for(Runnable r : crawlerList){
			new Thread(r).start();
		}
	}
}
