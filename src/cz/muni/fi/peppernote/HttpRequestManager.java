package cz.muni.fi.peppernote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpRequestManager {
	
	private String server;
	private String token;
	
	
	public HttpRequestManager(){
		
	}
	
	public HttpRequestManager(String server) {
		this.server = server;
	}
	
	public String readInputStream(InputStream in) throws IOException{
		InputStream instream = in;
		BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		instream.close();
		return sb.toString();
	};
	
	public List<Notebook> getNotebooks() throws ClientProtocolException, IOException, JSONException{
		String URL = server+ "/notebooks.json?auth_token=" + token;
		List<Notebook> notebooks = new ArrayList<Notebook>();			
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL);		
		HttpResponse response= httpclient.execute(get);
			
		HttpEntity entity = response.getEntity();
		
		if(entity != null){					
			String result = readInputStream(entity.getContent());
			
			JSONArray a = new JSONArray(result);
			
			for(int i=0; i < a.length(); i++){
				JSONObject jnb = a.getJSONObject(i);
				Notebook nb = new Notebook();
				
				nb.set_server_id(jnb.getInt("id")); 
				nb.set_user_id(jnb.getInt("user_id")); 
				nb.set_version(jnb.getInt("version"));
				nb.set_name(jnb.getString("name"));
				notebooks.add(nb);
			}
		}
		return notebooks;
	};
	
	public List<Note> getNotes(Notebook nb) throws ClientProtocolException, IOException, JSONException{
		String URL = server+ "/notebooks/" + nb.get_server_id() + ".json?auth_token=" + token;
		List<Note> notes = new ArrayList<Note>();			
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL);
		HttpResponse response= httpclient.execute(get);
			
		HttpEntity entity = response.getEntity();
		
		if(entity != null){					
			String result = readInputStream(entity.getContent());
			
			JSONArray a = new JSONArray(result);
			
			for(int i=0; i < a.length(); i++){
				JSONObject json = a.getJSONObject(i);
				Note note = new Note();
				
				note.set_server_id(json.getInt("id"));
				note.set_version(json.getInt("version"));
				note.set_title(json.getString("title"));
				note.set_content(json.getString("content"));
				
				notes.add(note);
			}
		}
		return notes;
	};
	
	public Notebook createNotebook(Notebook notebook) throws JSONException, ClientProtocolException, IOException, BadRequestException{
		String URL = server + "/notebooks.json?auth_token=" + token;
		Notebook response_nb = new Notebook();
				
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post= new HttpPost(URL);
			JSONObject body = new JSONObject();
			
			body.put("name", notebook.get_name());
			
			post.setEntity(new StringEntity(body.toString()));
			post.addHeader("Content-Type", "application/json");
			
			HttpResponse response= httpclient.execute(post);
			HttpEntity entity = response.getEntity();			
			
			if(response.getStatusLine().getStatusCode() == 400){
				String result = readInputStream(entity.getContent());
				JSONObject json = new JSONObject(result);
				throw new BadRequestException(json.getString("name"));
			}			
			if(entity != null){
				String result = readInputStream(entity.getContent());
				JSONObject json = new JSONObject(result);
								
					response_nb.set_server_id(json.getInt("id")); 
					response_nb.set_user_id(json.getInt("user_id")); 
					response_nb.set_version(json.getInt("version"));
					response_nb.set_name(json.getString("name"));
					
			}		
		return response_nb;
	};
	
	public Notebook updateNotebook(Notebook notebook) throws JSONException, ClientProtocolException, IOException, NotFoundException, BadRequestException, ConflictException{
		String URL = server + "/notebooks/" + notebook.get_server_id() + ".json?" +
				"version=" + notebook.get_version() + "&auth_token=" + token;
		Notebook response_nb = new Notebook();					
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut put = new HttpPut(URL);
		JSONObject body = new JSONObject();
		
		body.put("name", notebook.get_name());
		
		put.setEntity(new StringEntity(body.toString()));
		put.addHeader("Content-Type", "application/json");
		
		HttpResponse response= httpclient.execute(put);
		HttpEntity entity = response.getEntity();
		String result;
		JSONObject json;
		
		
		switch(response.getStatusLine().getStatusCode()){
			case 400:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new BadRequestException(json.getString("name"));
			case 404:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new NotFoundException(json.getString("message"));
			case 409:
				result = readInputStream(entity.getContent());				
				throw new ConflictException(result);
		}					
		
		if(entity != null){
			result = readInputStream(entity.getContent());	
			json = new JSONObject(result);
			if(json.has("message")){
				throw new NotFoundException(json.getString("message"));
			}else if(!json.has("id")){
				throw new BadRequestException(json.getString("name"));
			}else {			
				response_nb.set_server_id(json.getInt("id")); 
				response_nb.set_user_id(json.getInt("user_id")); 
				response_nb.set_version(json.getInt("version"));
				response_nb.set_name(json.getString("name"));
			}
		}else {
			return notebook;
		}
		return response_nb;
	};
	
	public Notebook deleteNotebook(Notebook notebook) throws JSONException, ClientProtocolException, IOException, NotFoundException, ConflictException{
		String URL = server + "/notebooks/" + notebook.get_server_id() + ".json?" +
				"version=" + notebook.get_version() + "&auth_token=" + token;
		Notebook response_nb = new Notebook();
		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete del = new HttpDelete(URL);
		HttpResponse response= httpclient.execute(del);			
		HttpEntity entity = response.getEntity();			
		String result;
		JSONObject json;
		
		
		switch(response.getStatusLine().getStatusCode()){
			case 404:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new NotFoundException(json.getString("message"));
			case 409:
				result = readInputStream(entity.getContent());				
				throw new ConflictException(result);
		}
		
		
		if(entity != null){
			result = readInputStream(entity.getContent());
			json = new JSONObject(result);			
			response_nb.set_server_id(json.getInt("id")); 
			response_nb.set_user_id(json.getInt("user_id")); 
			response_nb.set_version(json.getInt("version"));
			response_nb.set_name(json.getString("name"));			
		}
		return response_nb;
	};
		
	public Note createNote(Note note, Notebook nb) throws JSONException, ClientProtocolException, IOException,BadRequestException, NotFoundException{
		String URL = server + "/notes.json?notebook_id=" + nb.get_server_id() + "&auth_token=" + token;
		Note response_note = new Note();
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost post= new HttpPost(URL);
		JSONObject body = new JSONObject();
		
		body.put("title", note.get_title());
		body.put("content", note.get_content());
		
		post.setEntity(new StringEntity(body.toString()));
		post.addHeader("Content-Type", "application/json");
		
		HttpResponse response= httpclient.execute(post);			
		HttpEntity entity = response.getEntity();			
		String result;
		JSONObject json;
				
		switch(response.getStatusLine().getStatusCode()){
			case 400:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new BadRequestException(json.getString("name"));
			case 404:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new NotFoundException(json.getString("message"));
		}
		
		if(entity != null){
			result = readInputStream(entity.getContent());
			json = new JSONObject(result);				
			response_note.set_server_id(json.getInt("id"));  
			response_note.set_version(json.getInt("version"));
			response_note.set_title(json.getString("title"));
			response_note.set_content(json.getString("content"));
			return response_note;
		} else {
			return note;
		}
		
	};
		
	public Note updateNote(Note note) throws JSONException, ClientProtocolException, IOException, NotFoundException, BadRequestException{
		String URL = server + "/notes/" + note.get_server_id() + ".json?" +
				"version=" + note.get_version() + "&auth_token=" + token;
		Note response_note = new Note();			
		HttpClient httpclient = new DefaultHttpClient();
		HttpPut put = new HttpPut(URL);		
		JSONObject body = new JSONObject();
		
		body.put("title", note.get_title());
		body.put("content", note.get_content());
		
		put.setEntity(new StringEntity(body.toString()));
		put.addHeader("Content-Type", "application/json");
		
		HttpResponse response= httpclient.execute(put);			
		HttpEntity entity = response.getEntity();			
		String result;
		JSONObject json;
				
		switch(response.getStatusLine().getStatusCode()){
			case 400:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new BadRequestException(json.getString("name"));
			case 404:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new NotFoundException(json.getString("message"));
		}
		
		if(entity != null){
			result = readInputStream(entity.getContent());	
			json = new JSONObject(result);
			response_note.set_server_id(json.getInt("id"));				
			response_note.set_version(json.getInt("version"));
			response_note.set_title(json.getString("title"));
			response_note.set_content(json.getString("content"));
			
		}
		return response_note;
	};
		
	public Note deleteNote(Note note) throws JSONException, ClientProtocolException, IOException, NotFoundException, ConflictException{
		String URL = server + "/notes/" + note.get_server_id() + ".json?" +
				"version=" + note.get_version() + "&auth_token=" + token;
		Note response_note = new Note();
		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete del = new HttpDelete(URL);	
		HttpResponse response= httpclient.execute(del);			
		HttpEntity entity = response.getEntity();			
		String result;
		JSONObject json;
				
		switch(response.getStatusLine().getStatusCode()){
			case 404:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new NotFoundException(json.getString("message"));
			case 409:
				result = readInputStream(entity.getContent());				
				throw new ConflictException(result);
		}
		
		if(entity != null){
			result = readInputStream(entity.getContent());
			json = new JSONObject(result);			
			response_note.set_server_id(json.getInt("id")); 
			response_note.set_notebook_id(json.getInt("notebook_id")); 
			response_note.set_version(json.getInt("version"));
			response_note.set_title(json.getString("title"));
			response_note.set_content(json.getString("content"));
			
		}
		return response_note;
	};
	
	public int signIn(String email, String password) throws JSONException, EntityException, UnauthorizedException, IOException, BadRequestException, NotFoundException{
		String URL = server + "/api/v1/tokens.json";		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost post = new HttpPost(URL);
		JSONObject body = new JSONObject();
		
		body.put("email", email);
		body.put("password", password);
		
		post.setEntity(new StringEntity(body.toString()));
		post.addHeader("Content-Type", "application/json");
		HttpResponse response= httpclient.execute(post);
		HttpEntity entity = response.getEntity();
		String result;
		JSONObject json;
				
		switch(response.getStatusLine().getStatusCode()){
			case 400:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new BadRequestException(json.getString("message"));
			case 404:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new NotFoundException(json.getString("message"));
			case 401:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new UnauthorizedException(json.getString("message"));
		}	
		
		if(entity != null){
			result = readInputStream(entity.getContent());
			json = new JSONObject(result);
			int user_id = json.getInt("id");
			token = json.getString("token");
			
			return user_id;
		} else {
			throw new EntityException("Response entity from server is null");
		}
	};

	public void signOut(int user_id) throws UnauthorizedException, IOException, JSONException, NotFoundException{
		String URL = server + "/api/v1/tokens/" + user_id + ".json?auth_token=" + token;		
		HttpClient httpclient = new DefaultHttpClient();
		HttpDelete del = new HttpDelete(URL);
		HttpResponse response= httpclient.execute(del);		
		HttpEntity entity = response.getEntity();
		String result;
		JSONObject json;
				
		switch(response.getStatusLine().getStatusCode()){
			case 404:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new NotFoundException(json.getString("message"));
			case 401:
				result = readInputStream(entity.getContent());
				json = new JSONObject(result);
				throw new UnauthorizedException(json.getString("message"));
		}
		
		if(response.getStatusLine().getStatusCode() == 401){
			throw new UnauthorizedException("Invalid token");
		} else if(response.getStatusLine().getStatusCode() == 404){
			throw new UnauthorizedException("User not found");
		}
		token = null;
	};
	
	/*
	public String notebookToJSON(){
		return
	};	
	*/
	
	public String getToken() {
		return token;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public void setToken(String _token) {
		this.token = _token;
	};	
	
}
