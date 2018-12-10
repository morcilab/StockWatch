package com.google.gwt.sample.stockwatcher.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletContext;

import org.mapdb.DB;
import org.mapdb.DBMaker;

import com.google.gwt.sample.stockwatcher.client.StockPrice;
import com.google.gwt.sample.stockwatcher.client.StockPriceService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class StockPriceServiceImpl extends RemoteServiceServlet implements StockPriceService {
	private static final long serialVersionUID = 4192379456341403664L;
	private static final double MAX_PRICE = 100.0; // $100.00
	private static final double MAX_PRICE_CHANGE = 0.02; // +/- 2%

	@Override
	public StockPrice[] getPrices(String[] symbols) {
		Random rnd = new Random();
	    StockPrice[] prices = new StockPrice[symbols.length];
		for (int i=0; i<symbols.length; i++) {
			double price = rnd.nextDouble() * MAX_PRICE;
			double change = price * MAX_PRICE_CHANGE * (rnd.nextDouble() * 2f - 1f);

			prices[i] = new StockPrice(symbols[i], price, change);
		}

		return prices;
	}
	
	@Override
	public void saveSymbols(String[] symbols) {
		DB db = getDB();
		Map<Integer, String> map = db.getTreeMap("symbols");
		map.clear();
		int cnt = 0;
		for(String symbol : symbols) {
			map.put(cnt++, symbol);
		}
		db.commit(); //always remember to commit after modifications
	}

	@Override
	public String[] loadSymbols() {
		DB db = getDB();
		Map<Integer, String> map = db.getTreeMap("symbols");
		List<String> symbols = new ArrayList<String>();
		Set<Integer> keys = map.keySet();
		for(int key : keys) {
			symbols.add(map.get(key));
		}
		return symbols.toArray(new String[0]);
	}

	/*
	 * We store the DB in the servlet context
	 * to implement a poor man's singleton
	 */
	private DB getDB() {
		ServletContext context = this.getServletContext();
		synchronized (context) {
			DB db = (DB)context.getAttribute("DB");
			if(db == null) {
				db = DBMaker.newFileDB(new File("db")).closeOnJvmShutdown().make();
				context.setAttribute("DB", db);
			}
			return db;
		}
	}
}
