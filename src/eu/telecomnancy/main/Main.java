package eu.telecomnancy.main;

import eu.telecomnancy.pubmed.abstracts.AbstractDownloader;
import eu.telecomnancy.pubmed.id.IdDownloader;
import eu.telecomnancy.pubmed.store.Store;

public class Main {
	
	public static void main(String[] args) throws Exception {
		
		if (args.length < 2) {
			System.out.println("Usage : ");
			System.out.println("\t--download-ids\t\t{year}");
			System.out.println("\t--download-abstracts\t{year}");
			System.out.println("\t--store\t\t\t{year}");
			System.exit(0);
		}
		
		int year = Integer.parseInt(args[1]);
		switch (args[0]) {
			case "--download-ids":
				IdDownloader idDownloader = new IdDownloader(year);
				idDownloader.download();
				break;
			case "--download-abstracts":
				AbstractDownloader abstractDownloader = new AbstractDownloader(year);
				abstractDownloader.download();
				break;
			case "--store":
				Store store = new Store(year);
				store.store();
				break;
		}
	}
}