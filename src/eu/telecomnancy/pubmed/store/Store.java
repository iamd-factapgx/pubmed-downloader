package eu.telecomnancy.pubmed.store;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.http.Consts;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class Store {
	private int year;
	private File file;

	public Store(int year) throws Exception {
		this.year	= year;
		file 		= new File("./output/" + this.year + "/abstracts");
		
		if (!file.exists()) {
			throw new Exception("Impossible to find abstracts list");
		}
	}
	
	public void store() throws Exception {
		int i=0;
		BufferedReader reader;
		
		String line;
		reader = new BufferedReader(new FileReader(file));
		while(null != (line = reader.readLine())) {
			try {
				StringEntity entity = new StringEntity(line.replaceAll("\"id\"", "\"_id\"").trim(), ContentType.create("application/json", Consts.UTF_8));
				entity.setChunked(true);
				
				System.out.println(
					Request
						.Post("http://pubmed.telecomnancy.univ-lorraine.fr/publications")
						.body(entity)
						.execute()
						.returnContent()
						.asString()
				);
				} catch (Exception e) {}
				
			i++;
			Thread.sleep(3000);
			if (i > 250) {
				break;
			}
		}
		
		reader.close();
	}

}
