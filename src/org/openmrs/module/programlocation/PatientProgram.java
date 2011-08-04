package org.openmrs.module.programlocation;

import java.util.Date;

import org.openmrs.Location;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.util.OpenmrsUtil;

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

	// brute-force overriding to make sure programs are automatically completed when transition to terminal states
	// copied from OpenMRS 1.7
	public void transitionToState(ProgramWorkflowState programWorkflowState, Date onDate) {
		PatientState lastState = getCurrentState(programWorkflowState.getProgramWorkflow());
		if (lastState != null && onDate == null) {
			throw new IllegalArgumentException("You can't change from a non-null state without giving a change date");
		}
		if (lastState != null && lastState.getEndDate() != null) {
			throw new IllegalArgumentException("You can't change out of a state that has an end date already");
		}
		if (lastState != null && lastState.getStartDate() != null
		        && OpenmrsUtil.compare(lastState.getStartDate(), onDate) > 0) {
			throw new IllegalArgumentException("You can't change out of a state before that state started");
		}
		if (lastState != null
		        && !programWorkflowState.getProgramWorkflow().isLegalTransition(lastState.getState(), programWorkflowState)) {
			throw new IllegalArgumentException("You can't change from state " + lastState.getState() + " to "
			        + programWorkflowState);
		}
		if (lastState != null) {
			lastState.setEndDate(onDate);
		}
		
		PatientState newState = new PatientState();
		newState.setPatientProgram(this);
		newState.setState(programWorkflowState);
		newState.setStartDate(onDate);
		
		getStates().add(newState);
		
		if (newState.getState().getTerminal()) {
			// complete patientprogram automatically as it is a terminal state
			newState.getPatientProgram().setDateCompleted(onDate);
		}
	}

}
