package eu.telecomnancy.pubmed.abstracts;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import eu.telecomnancy.model.Article;

public class AbstractDownloader {
	private int year;
	private File file;
	private String dir;
	private File afile;

	public AbstractDownloader(int year) throws Exception {
		this.year	= year;
		dir 		= "./output/" + this.year;
		file		= new File(dir + "/_ids");
		
		afile		= new File("./output/" + this.year + "/abstracts");
		
		if (afile.exists()) {
			afile.delete();
		}
		
		if (!file.exists()) {
			throw new Exception("Impossible to find id list");
		}
	}
	
	public void download() throws Exception
	{
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while (null != (line = reader.readLine())) {
			line = line.trim();
			if (!line.isEmpty()) {
				try {
				processDownload(line);
				} catch (Exception e) {System.out.println("Error while processing " + line);}
			}
		}
		reader.close();
	}
	
	private void processDownload(String ids) throws Exception {
		String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&rettype=xml&id=" + ids;
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpGet get = new HttpGet(url);
		
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

			@Override
			public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
				int status = response.getStatusLine().getStatusCode();
				if (status >= 200 && status < 300) {
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException("Unexpected response status: " + status);
				}
			}
		};
		
		String responseBody = client.execute(get, responseHandler);
		
		Document doc		= null;
		DOMParser parser	= new DOMParser();
		
		parser.parse(new InputSource(new StringReader(responseBody)));
		doc 				= parser.getDocument();
		
		
		NodeList bookArticles	= doc.getElementsByTagName("PubmedBookArticle");
		NodeList articles		= doc.getElementsByTagName("PubmedArticle");
		
		int i;
		Element article;
		NodeList abstrcts, titles;
		String json;
		BufferedWriter writer;
		
		Gson gson = new Gson();
		for (i=0; i < bookArticles.getLength(); i++) {
			
			article 	= (Element) bookArticles.item(i);
			
			titles		= article.getElementsByTagName("BookTitle");
			abstrcts	= article.getElementsByTagName("Abstract");
			
			if (abstrcts.getLength() > 0) {
				abstrcts	= ((Element) abstrcts.item(0)).getElementsByTagName("AbstractText");
				if (abstrcts.getLength() > 0 && titles.getLength() > 0) {
					Article a 	= new Article();
					a._id		= Integer.parseInt(article.getElementsByTagName("PMID").item(0).getTextContent().trim());
					a.title		= titles.item(0).getTextContent().trim();
					a.abstrct	= abstrcts.item(0).getTextContent().trim();
					a.year		= year;
					
					json = gson.toJson(a);
					
					writer = new BufferedWriter(new FileWriter(this.afile, true));
					writer.write(json);
					writer.close();
				}
			}
		}
		

		writer = new BufferedWriter(new FileWriter(this.afile, true));
		for (i=0; i < articles.getLength(); i++) {
			article 	= (Element) articles.item(i);
			
			titles		= article.getElementsByTagName("ArticleTitle");
			abstrcts	= article.getElementsByTagName("Abstract");
			
			
			if (abstrcts.getLength() > 0) {;
				abstrcts	= ((Element) abstrcts.item(0)).getElementsByTagName("AbstractText");
				if (abstrcts.getLength() > 0 && titles.getLength() > 0) {
					Article a 	= new Article();
					a._id		= Integer.parseInt(article.getElementsByTagName("PMID").item(0).getTextContent().trim());
					a.title		= titles.item(0).getTextContent().trim();
					a.abstrct	= abstrcts.item(0).getTextContent().trim();
					a.year		= year;
					
					json = gson.toJson(a);
					writer.write(json + "\n");
				}
			}
		}
		writer.close();
		
		System.out.println("Processed " + ids);
		client.close();
		Thread.sleep(334);
	}
}
