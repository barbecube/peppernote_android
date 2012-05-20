package cz.muni.fi.peppernote;

public class ServerConflict {
	
	public static final int TAKEN_VALUE = 1;
	public static final int ID_NOT_FOUND = 2;
	public static final int NEW_VERSION = 3;
	
	
	private int type_of_entity;
	private int type_of_change;
	private int conflict;
	private Object entity;
	
	public ServerConflict(){
		
	}
	
	public ServerConflict(int type_of_entity,
			int type_of_change, int conflict, Object entity) {
		super();
		this.type_of_entity = type_of_entity;
		this.type_of_change = type_of_change;
		this.conflict = conflict;
		this.entity = entity;
	}
	
	
	public String toString(){
		String s;
		if(type_of_entity == PepperNoteManager.NOTEBOOK_ENTITY){
			Notebook n = (Notebook) entity;
			s = n.get_name() + " [notebook";
		} else {
			Note n = (Note) entity;
			s = n.get_title() + " [note";
		}
		switch (conflict){
			case TAKEN_VALUE: s = s + ":taken value]";
							  break;
			case ID_NOT_FOUND: s = s + ":id not found]";
							   break;
			case NEW_VERSION: s = s + ":new version]";
							  break;
		}
		return s;
	}


	public int getType_of_entity() {
		return type_of_entity;
	}


	public void setType_of_entity(int type_of_entity) {
		this.type_of_entity = type_of_entity;
	}


	public int getType_of_change() {
		return type_of_change;
	}


	public void setType_of_change(int type_of_change) {
		this.type_of_change = type_of_change;
	}


	public int getConflict() {
		return conflict;
	}


	public void setConflict(int conflict) {
		this.conflict = conflict;
	}


	public Object getEntity() {
		return entity;
	}


	public void setEntity(Object entity) {
		this.entity = entity;
	}
	
	
}
