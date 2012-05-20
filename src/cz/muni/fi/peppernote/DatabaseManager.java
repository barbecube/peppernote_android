package cz.muni.fi.peppernote;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseManager extends SQLiteOpenHelper  {
	
	// Database Version
    private static final int DATABASE_VERSION = 1;
 
    // Database Name
    private static final String DATABASE_NAME = "PepperNoteDatabase";
 
    // table names
    private static final String TABLE_NOTEBOOKS = "notebooks";
    private static final String TABLE_NOTES = "notes";
    private static final String TABLE_CHANGES = "changes";
    private static final String TABLE_SERVERS = "servers";
    
    // Table Columns names - Note
    private static final String NOTE_ID = "id";
    private static final String NOTE_SERVER_ID = "server_id";
    private static final String NOTE_NOTEBOOK_ID = "notebook_id";
    private static final String NOTE_VERSION = "version";
    private static final String NOTE_TITLE = "title";
    private static final String NOTE_CONTENT = "content";
    
    // -Notebooks
    private static final String NOTEBOOK_ID = "id";
    private static final String NOTEBOOK_SERVER_ID = "server_id";
    private static final String NOTEBOOK_USER_ID = "user_id";
    private static final String NOTEBOOK_VERSION = "version";
    private static final String NOTEBOOK_NAME = "name";
    private static final String NOTEBOOK_SERVER = "server";
    
    // -Changes
    private static final String CHANGE_ID ="id";
    private static final String CHANGE_ENTITY_ID ="entity_id";
    private static final String CHANGE_VERSION = "version";
    private static final String CHANGE_USER_ID ="user_id";
    private static final String CHANGE_TYPE_OF_CHANGE ="type_of_change";
    private static final String CHANGE_TYPE_OF_ENTITY ="type_of_entity";
    private static final String CHANGE_SERVER = "server";
    
    // -PepperNote Servers
    private static final String SERVER_ID ="id";
    private static final String SERVER_ADDRESS = "address";
	
    
    //
    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		String CREATE_SERVERS_TABLE = "CREATE TABLE " + TABLE_SERVERS + "("
                + SERVER_ID + " INTEGER PRIMARY KEY," + SERVER_ADDRESS + " TEXT" + ")";
        db.execSQL(CREATE_SERVERS_TABLE);
		
		String CREATE_NOTEBOOKS_TABLE = "CREATE TABLE " + TABLE_NOTEBOOKS + "("
                + NOTEBOOK_ID + " INTEGER PRIMARY KEY," + NOTEBOOK_NAME + " TEXT,"
                + NOTEBOOK_SERVER_ID + " INTEGER, " + NOTEBOOK_USER_ID + " INTEGER, " 
                + NOTEBOOK_VERSION + " INTEGER, " + NOTEBOOK_SERVER + " INTEGER," 
                + "FOREIGN KEY(" + NOTEBOOK_SERVER +") REFERENCES " + TABLE_SERVERS 
                + "( " + SERVER_ID + ")" + " ON DELETE CASCADE " + ")";
        db.execSQL(CREATE_NOTEBOOKS_TABLE);
        
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "("
                + NOTE_ID + " INTEGER PRIMARY KEY," + NOTE_SERVER_ID + " INTEGER, " 
                + NOTE_NOTEBOOK_ID + " INTEGER, " + NOTE_VERSION + " INTEGER, " 
                + NOTE_TITLE + " TEXT, " + NOTE_CONTENT + " TEXT, " 
                + "FOREIGN KEY(" + NOTE_NOTEBOOK_ID +") REFERENCES " + TABLE_NOTEBOOKS 
                + "( " + NOTEBOOK_ID + ")" + " ON DELETE CASCADE " + ")";
        db.execSQL(CREATE_NOTES_TABLE);
        
        String CREATE_CHANGES_TABLE = "CREATE TABLE " + TABLE_CHANGES + "("
                + CHANGE_ID + " INTEGER PRIMARY KEY," + CHANGE_ENTITY_ID + " INTEGER," + CHANGE_VERSION + " INTEGER,"
        		+ CHANGE_USER_ID + " INTEGER," + CHANGE_TYPE_OF_CHANGE + " INTEGER," 
                + CHANGE_TYPE_OF_ENTITY + " INTEGER," + CHANGE_SERVER + " INTEGER," 
                + "FOREIGN KEY(" + CHANGE_SERVER +") REFERENCES " + TABLE_SERVERS 
                + "( " + SERVER_ID + ")" + " ON DELETE CASCADE " +")";
        db.execSQL(CREATE_CHANGES_TABLE);
        
        
	}
	
	@Override
	public void onOpen(SQLiteDatabase db) {
	    super.onOpen(db);
	    if (!db.isReadOnly()) {
	        // Enable foreign key constraints
	        db.execSQL("PRAGMA foreign_keys=ON;");
	    }
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVesion) {
		// Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTEBOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHANGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVERS);
        
        // Create tables again
        onCreate(db);
		
	}

		
	//--------------------------------------------------------------------------------------
	
	public int addNotebook(Notebook notebook) {
		SQLiteDatabase db = this.getWritableDatabase();
		
	    ContentValues values = new ContentValues();
	    values.put(NOTEBOOK_NAME, notebook.get_name());
	    values.put(NOTEBOOK_SERVER_ID, notebook.get_server_id());
	    values.put(NOTEBOOK_USER_ID, notebook.get_user_id());
	    values.put(NOTEBOOK_VERSION, notebook.get_version()); 
	    values.put(NOTEBOOK_SERVER, notebook.get_server());
	 
	    // Inserting Row
	    int id = (int)db.insert(TABLE_NOTEBOOKS, null, values);
	    db.close(); // Closing database connection
	    return id;
	}
	 
	//
	public Notebook getNotebook(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		 
	    Cursor cursor = db.query(TABLE_NOTEBOOKS, new String[] { NOTEBOOK_ID,
	            NOTEBOOK_SERVER_ID, NOTEBOOK_USER_ID, NOTEBOOK_VERSION, NOTEBOOK_NAME, NOTEBOOK_SERVER }, NOTEBOOK_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	    Notebook notebook = new Notebook(Integer.parseInt(cursor.getString(0)),
	    		Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)),
	    		Integer.parseInt(cursor.getString(3)),cursor.getString(4),
	    		Integer.parseInt(cursor.getString(5)));
	    cursor.close();
	    db.close();
	    return notebook;
	}
	 
	//
	public List<Notebook> getAllNotebooks() {
		List<Notebook> notebookList = new ArrayList<Notebook>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_NOTEBOOKS;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Notebook notebook = new Notebook();
	            notebook.set_id(Integer.parseInt(cursor.getString(0)));
	            notebook.set_name(cursor.getString(1));
	            notebook.set_server_id(Integer.parseInt(cursor.getString(2)));
	            notebook.set_user_id(Integer.parseInt(cursor.getString(3)));
	            notebook.set_version(Integer.parseInt(cursor.getString(4)));
	            notebook.set_server(Integer.parseInt(cursor.getString(5)));
	            
	            
	            notebookList.add(notebook);
	        } while (cursor.moveToNext());
	    }
	    cursor.close();
	    db.close();
	    //
	    return notebookList;
	}
	 
	
	public List<Notebook> getAllNotebooksByUserIdAndServer(int user_id, PepperNoteServer server) {
		
		List<Notebook> notebookList = new ArrayList<Notebook>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_NOTEBOOKS 
	    		+ " WHERE " + NOTEBOOK_USER_ID + " = " + user_id 
	    		+ " AND " + NOTEBOOK_SERVER + " = " + server.get_id();
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);
	 
	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Notebook notebook = new Notebook();
	            notebook.set_id(Integer.parseInt(cursor.getString(0)));
	            notebook.set_name(cursor.getString(1));
	            notebook.set_server_id(Integer.parseInt(cursor.getString(2)));
	            notebook.set_user_id(Integer.parseInt(cursor.getString(3)));
	            notebook.set_version(Integer.parseInt(cursor.getString(4)));
	            notebook.set_server(Integer.parseInt(cursor.getString(5)));
	            
	            
	            notebookList.add(notebook);
	        } while (cursor.moveToNext());
	    }
	    cursor.close();
	    db.close();
	    //
	    return notebookList;
	}
	
	
	
	//
	public int updateNotebook(Notebook notebook) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		 
	    ContentValues values = new ContentValues();
	    values.put(NOTEBOOK_NAME, notebook.get_name());
	    values.put(NOTEBOOK_SERVER_ID, notebook.get_server_id());
	    values.put(NOTEBOOK_USER_ID, notebook.get_user_id());
	    values.put(NOTEBOOK_VERSION, notebook.get_version());
	    values.put(NOTEBOOK_SERVER, notebook.get_server());
	    
	 
	    // updating row
	    int id = db.update(TABLE_NOTEBOOKS, values, NOTEBOOK_ID + " = ?",
	            new String[] { String.valueOf(notebook.get_id()) });
	    db.close();
	    return id;
	}
	 
	//
	public int deleteNotebook(Notebook notebook) {
				
	    SQLiteDatabase db = this.getWritableDatabase();
	    int rows_num = db.delete(TABLE_NOTEBOOKS, NOTEBOOK_ID + " = ?",
	            new String[] { String.valueOf(notebook.get_id()) });
	    deleteAllNotesInNotebook(notebook);
	    db.close();
	    return rows_num;
	}
	
	
	//----------------------------------------------------------------------------------------
	
	
	public int addNote(Note note) {
		SQLiteDatabase db = this.getWritableDatabase();
		
	    ContentValues values = new ContentValues();
	    values.put(NOTE_CONTENT, note.get_content());
	    values.put(NOTE_NOTEBOOK_ID, note.get_notebook_id());
	    values.put(NOTE_SERVER_ID, note.get_server_id());
	    values.put(NOTE_TITLE, note.get_title()); 
	    values.put(NOTE_VERSION, note.get_version()); 
	    
	    // Inserting Row
	    int id = (int)db.insert(TABLE_NOTES, null, values);
	    db.close(); // Closing database connection
	    return id;
	}
	 
	//
	public Note getNote(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		 
	    Cursor cursor = db.query(TABLE_NOTES, new String[] { NOTE_ID,
	            NOTE_SERVER_ID, NOTE_NOTEBOOK_ID, NOTE_VERSION, NOTE_TITLE, NOTE_CONTENT }, NOTE_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	    Note note = new Note(Integer.parseInt(cursor.getString(0)),
	    		Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)),
	    		Integer.parseInt(cursor.getString(3)), cursor.getString(4), cursor.getString(5) );
	    cursor.close();
	    db.close();
	    return note;
	}
	 
	//
	public List<Note> getAllNotes() {
		List<Note> noteList = new ArrayList<Note>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_NOTES;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Note note = new Note();
	            note.set_id(Integer.parseInt(cursor.getString(0)));
	            note.set_server_id(Integer.parseInt(cursor.getString(1)));
	            note.set_notebook_id(Integer.parseInt(cursor.getString(2)));
	            note.set_version(Integer.parseInt(cursor.getString(3)));
	            note.set_title(cursor.getString(4));
	            note.set_content(cursor.getString(5));
	            
	            
	            noteList.add(note);
	        } while (cursor.moveToNext());
	    }
	    cursor.close();
	    db.close();
	    //
	    return noteList;
	}
	 
	//
	public List<Note> getAllNotesByNotebookId(Notebook notebook) {
		
		List<Note> noteList = new ArrayList<Note>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_NOTES + " WHERE " + NOTE_NOTEBOOK_ID 
	    		+ " = " + notebook.get_id();
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Note note = new Note();
	            note.set_id(Integer.parseInt(cursor.getString(0)));
	            note.set_server_id(Integer.parseInt(cursor.getString(1)));
	            note.set_notebook_id(Integer.parseInt(cursor.getString(2)));
	            note.set_version(Integer.parseInt(cursor.getString(3)));
	            note.set_title(cursor.getString(4));
	            note.set_content(cursor.getString(5));
	            
	            
	            noteList.add(note);
	        } while (cursor.moveToNext());
	    }
	    cursor.close();
	    db.close();
	    //
	    return noteList;
	}
	
	//
	public int updateNote(Note note) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		 
	    ContentValues values = new ContentValues();
	    values.put(NOTE_CONTENT, note.get_content());
	    values.put(NOTE_NOTEBOOK_ID, note.get_notebook_id());
	    values.put(NOTE_SERVER_ID, note.get_server_id());
	    values.put(NOTE_TITLE, note.get_title());
	    values.put(NOTE_VERSION, note.get_version());
	    
	 
	    // updating row
	    int id = db.update(TABLE_NOTES, values, NOTE_ID + " = ?",
	            new String[] { String.valueOf(note.get_id()) });
	    db.close();
	    return id;
	}
	 
	//
	public int deleteNote(Note note) {
				
	    SQLiteDatabase db = this.getWritableDatabase();
	    int rows_num = db.delete(TABLE_NOTES, NOTE_ID + " = ?",
	            new String[] { String.valueOf(note.get_id()) });
	    db.close();
	    return rows_num;
	}
	
	public void deleteAllNotesInNotebook(Notebook notebook){
		List<Note> notes = getAllNotesByNotebookId(notebook);
		for(int i=0;i < notes.size();i++){
			deleteNote(notes.get(i));
		}
	}
	
	//---------------------------------------------------------------------------------------

	
	public int addChange(Change change) {
		SQLiteDatabase db = this.getWritableDatabase();
		
	    ContentValues values = new ContentValues();
	    //added ID 
	    values.put(CHANGE_ENTITY_ID,change.get_entity_id());
	    values.put(CHANGE_VERSION, change.get_version());
	    values.put(CHANGE_USER_ID, change.get_user_id());
	    values.put(CHANGE_TYPE_OF_CHANGE, change.get_type_of_change());
	    values.put(CHANGE_TYPE_OF_ENTITY, change.get_type_of_entity());
	    values.put(CHANGE_SERVER, change.get_server());
	    
	    
	    // Inserting Row
	    int id = (int)db.insert(TABLE_CHANGES, null, values);
	    db.close(); // Closing database connection
	    return id;
	}
	 
	//
	public Change getChange(int id) { 
		
		SQLiteDatabase db = this.getReadableDatabase();
		 
	    Cursor cursor = db.query(TABLE_CHANGES, new String[] { CHANGE_ID, CHANGE_ENTITY_ID, CHANGE_VERSION,CHANGE_USER_ID,
	            CHANGE_TYPE_OF_CHANGE, CHANGE_TYPE_OF_ENTITY, CHANGE_SERVER }, CHANGE_ID + "=?",
	            new String[] { String.valueOf(id) }, null, null, null, null);
	    if (cursor != null)
	        cursor.moveToFirst();
	 
	    Change change = new Change( Integer.parseInt(cursor.getString(0)),
	    		Integer.parseInt(cursor.getString(1)), Integer.parseInt(cursor.getString(2)),
	    		Integer.parseInt(cursor.getString(3)), Integer.parseInt(cursor.getString(4)),
	    		Integer.parseInt(cursor.getString(5)),Integer.parseInt(cursor.getString(6)));
	    cursor.close();
	    db.close();
	    return change;
	}
	 
	//
	public List<Change> getAllChanges() {
		List<Change> changeList = new ArrayList<Change>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_CHANGES;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            Change change = new Change();
	            change.set_id(Integer.parseInt(cursor.getString(0)));
	            change.set_entity_id(Integer.parseInt(cursor.getString(1)));
	            change.set_version(Integer.parseInt(cursor.getString(2)));
	            change.set_user_id(Integer.parseInt(cursor.getString(3)));
	            change.set_type_of_change(Integer.parseInt(cursor.getString(4)));
	            change.set_type_of_entity(Integer.parseInt(cursor.getString(5)));
	            change.set_server(Integer.parseInt(cursor.getString(6)));
	            
	            
	            changeList.add(change);
	        } while (cursor.moveToNext());
	    }
	    cursor.close();
	    db.close();
	    //
	    return changeList;
	}
	 
	//
	public List<Change> getAllChangesByUserIdAndServer(int user_id, PepperNoteServer server) {
		
		List<Change> changeList = new ArrayList<Change>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_CHANGES 
	    		+ " WHERE " + CHANGE_USER_ID + " = " + user_id 
	    		+ " AND " + CHANGE_SERVER + " = " + server.get_id();
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	        	Change change = new Change();
	            change.set_id(Integer.parseInt(cursor.getString(0)));
	            change.set_entity_id(Integer.parseInt(cursor.getString(1)));
	            change.set_version(Integer.parseInt(cursor.getString(2)));
	            change.set_user_id(Integer.parseInt(cursor.getString(3)));
	            change.set_type_of_change(Integer.parseInt(cursor.getString(4)));
	            change.set_type_of_entity(Integer.parseInt(cursor.getString(5)));
	            change.set_server(Integer.parseInt(cursor.getString(6)));
	            
	            
	            changeList.add(change);
	        } while (cursor.moveToNext());
	    }
	    cursor.close();
	    db.close();
	    //
	    return changeList;
	}
	
	//
	public int updateChange(Change change) {
		
		SQLiteDatabase db = this.getWritableDatabase();
		 
	    ContentValues values = new ContentValues();
	    values.put(CHANGE_ENTITY_ID, change.get_entity_id());
	    values.put(CHANGE_VERSION, change.get_version());
	    values.put(CHANGE_USER_ID, change.get_user_id());
	    values.put(CHANGE_TYPE_OF_CHANGE, change.get_type_of_change());
	    values.put(CHANGE_TYPE_OF_ENTITY, change.get_type_of_entity());
	    values.put(CHANGE_SERVER, change.get_server());
	    
	 
	    // updating row
	    int id = db.update(TABLE_CHANGES, values, CHANGE_ID + " = ?",
	            new String[] { String.valueOf(change.get_id()) });
	    db.close();
	    return id;
	}
	 
	//
	public int deleteChange(Change change) {
				
	    SQLiteDatabase db = this.getWritableDatabase();
	    int row_num = db.delete(TABLE_CHANGES, CHANGE_ID + " = ?",
	            new String[] { String.valueOf(change.get_id()) });
	    db.close();
	    return row_num;
	}
	
	//--------------------------------------------------------------------------------------
	
	public int addServer(PepperNoteServer server){
		SQLiteDatabase db = this.getWritableDatabase();
		
	    ContentValues values = new ContentValues(); 
	    values.put(SERVER_ADDRESS,server.getAddress());	    
	    
	    // Inserting Row
	    int id = (int)db.insert(TABLE_SERVERS, null, values);
	    db.close(); // Closing database connection
	    return id;
	};	
	public int updateServer(PepperNoteServer server){
		SQLiteDatabase db = this.getWritableDatabase();
		
	    ContentValues values = new ContentValues(); 
	    values.put(SERVER_ADDRESS,server.getAddress());	    
	    
	    // Inserting Row
	    return db.update(TABLE_SERVERS, values, SERVER_ID + " = ?",
	            new String[] { String.valueOf(server.get_id()) });
	};	
	public int deleteServer(PepperNoteServer server){
		SQLiteDatabase db = this.getWritableDatabase();
	    int row_num = db.delete(TABLE_SERVERS, SERVER_ID + " = ?",
	            new String[] { String.valueOf(server.get_id()) });
	    db.close();
	    return row_num;
	};
	public List<PepperNoteServer> getAllServers(){
		List<PepperNoteServer> serversList = new ArrayList<PepperNoteServer>();
	    // Select All Query
	    String selectQuery = "SELECT  * FROM " + TABLE_SERVERS;
	 
	    SQLiteDatabase db = this.getWritableDatabase();
	    Cursor cursor = db.rawQuery(selectQuery, null);

	    // looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	            PepperNoteServer server = new PepperNoteServer();
	            server.set_id(Integer.parseInt(cursor.getString(0)));
	            server.setAddress(cursor.getString(1));
	            
	            serversList.add(server);
	        } while (cursor.moveToNext());
	    }
	    cursor.close();
	    db.close();
	    //
	    return serversList;
	};
	
}
