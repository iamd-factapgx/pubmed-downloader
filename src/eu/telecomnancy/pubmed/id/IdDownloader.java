package eu.telecomnancy.pubmed.id;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class IdDownloader {

	private int year;
	private File file;

	public IdDownloader(int year) {
		this.year	= year;
		this.file	= new File("./output/" + this.year + "/_ids");
		
		if (!file.exists()) {
			File dir = new File(file.getParent());
			if (!dir.exists()) {
				dir.mkdirs();
			}
		} else {
			file.delete();
		}
	}
	
	public void download() throws Exception
	{
		int count=0, retstart=-200, retmax=200;
		do {
			try {
				retstart	= retstart + retmax;
				count		= processDownload(retstart, retmax);
			} catch (Exception e) {}
			Thread.sleep(334);
		} while (count > retstart);
	}
	
	private int processDownload(int retstart, int retmax) throws Exception {
		String url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term="+year+"[pdat]&retmode=xml&retmax=" + retmax + "&retstart=" + retstart;
		
		HttpClient client = HttpClients.createDefault();
		
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
		doc = parser.getDocument();
		
		int count		= Integer.parseInt(doc.getElementsByTagName("Count").item(0).getTextContent());
		NodeList ids	= doc.getElementsByTagName("IdList").item(0).getChildNodes();
		
		String str = "";
		for (int i=0; i<ids.getLength(); i++) {
			Node id = ids.item(i);
			if (id.getNodeName().equals("Id")) {
				str += id.getTextContent() + ",";
			}
		}
		
		BufferedWriter writer	= new BufferedWriter(new FileWriter(file, true));
		writer.write(str.substring(0, str.length()-2) + "\n");
		writer.close();
		
		System.out.println("Processed " + (retstart + retmax) + "/" + count);
		return count;
	}
}
