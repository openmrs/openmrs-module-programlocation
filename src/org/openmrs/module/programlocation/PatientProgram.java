package org.openmrs.module.programlocation;

import org.openmrs.Location;

public class PatientProgram extends org.openmrs.PatientProgram {

	private static final long serialVersionUID = 8329515304349020910L;
	private Location location;
	
	public PatientProgram() {
		// TODO Auto-generated constructor stub
		super();
	}

	public PatientProgram(Integer patientProgramId) {
		super(patientProgramId);
		// TODO Auto-generated constructor stub
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Location getLocation() {
		return location;
	}

}
