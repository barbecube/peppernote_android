package cz.muni.fi.peppernote;

import android.app.Application;

public class PepperNote extends Application{
	private PepperNoteManager manager;
	
	public PepperNote(){
		manager = new PepperNoteManager();
	}
	
	public PepperNoteManager manager(){
		return manager;
	}
	
	public PepperNoteManager getGlobalVariable() {
        return manager;
	}
}
