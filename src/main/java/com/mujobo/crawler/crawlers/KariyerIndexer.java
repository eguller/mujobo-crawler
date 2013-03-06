package com.mujobo.crawler.crawlers;

import com.mujobo.crawler.model.JobData;
import com.mujobo.crawler.model.DbJob;
import com.mujobo.crawler.model.KariyerLink;
import com.mujobo.crawler.parser.KariyerParser;
import com.mujobo.crawler.parser.Parser;

import org.hibernate.exception.ConstraintViolationException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

import javax.persistence.EntityManager;

/**
 * Created with IntelliJ IDEA. User: mac Date: 1/22/13 Time: 12:30 PM To change
 * this template use File | Settings | File Templates.
 */
public class KariyerIndexer extends DbJob implements Runnable {
	Parser parser = new KariyerParser();

	public KariyerIndexer(EntityManager em) {
		super(em);
	}

	public void run() {
		while (true) {
			List<KariyerLink> kariyerLinkList = getUnIndexedLinks();
			for (KariyerLink kariyerLink : kariyerLinkList) {
				Document doc = null;
				try {
					doc = Jsoup.connect(kariyerLink.url).get();
					if (isExpired(doc)) {
						kariyerLink.crawled = true;
						try {
							save(kariyerLink);
						} catch (ConstraintViolationException cve) {
							// omit
						} catch (Exception e) {
							e.printStackTrace();
						}
						continue;
					}
					JobData jobData = parser.parse(doc);
					if (jobData != null) {
						// beginTx();
						kariyerLink.crawled = true;
						try {
							save(kariyerLink);
						} catch (ConstraintViolationException cve) {
							// omit
						} catch (Exception e) {
							e.printStackTrace();
						}

						// endTx();
						postJob(jobData);
					}
				} catch (ConstraintViolationException e) {
					// e.printStackTrace(); //To change body of catch statement
					// use File | Settings | File Templates.

				} catch (IOException e) {
					e.printStackTrace(); // To change body of catch statement
											// use File | Settings | File
											// Templates.
				}

			}
		}
	}

	private boolean isExpired(Document doc) {
		String text = doc.text();
		if (text.contains("ilan\u0131 yay\u0131nda de\u011fil")) {
			return true;
		}
		return false;
	}

	public static void postJob(JobData jobData) {
		try {
			Jsoup.connect("http://localhost:9000/crawler/api/job/add").timeout(20000)
					.data(jobData.toMap()).post();
			System.out.println("Posted");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<KariyerLink> getUnIndexedLinks() {
		// beginTx();
		List<KariyerLink> kariyerLinkList = em
				.createQuery(
						"select kl from KariyerLink kl where crawled=false")
				.setMaxResults(10).getResultList();
		// endTx();
		return kariyerLinkList;
	}
}
