package org.openmrs.module.programlocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.openmrs.Location;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
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

	// brute force override to show correct order of states even if 2 states have start date at same day
	// list of states for patient program in UI and voidLastState only looks into startdate, but doesn't take end date into account
	public List<PatientState> statesInWorkflow(ProgramWorkflow programWorkflow, boolean includeVoided) {
		List<PatientState> ret = new ArrayList<PatientState>();
		for (PatientState st : getStates()) {
			if (st.getState().getProgramWorkflow().equals(programWorkflow) && (includeVoided || !st.getVoided())) {
				ret.add(st);
			}
		}
		Collections.sort(ret, new Comparator<PatientState>() {
			
			public int compare(PatientState left, PatientState right) {
				// check if one of the states is active 
				if (left.getActive()) {
					return 1;
				}
				if (right.getActive()) {
					return -1;
				}
				return OpenmrsUtil.compareWithNullAsEarliest(left.getStartDate(), right.getStartDate());
			}
		});
		return ret;
	}

	// make sure that copy (for merge) also includes enrollment location
	public org.openmrs.PatientProgram copy() {
		return copyHelper(new PatientProgram());
	}

	protected org.openmrs.PatientProgram copyHelper(
			org.openmrs.PatientProgram target) {
		((PatientProgram) target).setLocation(getLocation());
		return super.copyHelper(target);
	}
}
