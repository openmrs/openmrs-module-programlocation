/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package org.openmrs.module.programlocation.advisor;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.Set;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

public class ProgramWorkflowServiceAroundAdvisor extends
		StaticMethodMatcherPointcutAdvisor implements Advisor {

	private static final long serialVersionUID = -1088281919324035946L;

	public boolean matches(Method method, Class targetClass) {
		if (method.getName().equals("triggerStateConversion")
				&& method.getParameterTypes().length == 3) {
			return true;
		}
		return false;
	}

	@Override
	public Advice getAdvice() {
		return new ExitFromCareAdvice();
	}

	public class ExitFromCareAdvice implements MethodInterceptor {

		public Object invoke(MethodInvocation invocation) throws Throwable {
			final Object[] arguments = invocation.getArguments();
			final Patient patient = (Patient) arguments[0];
			final Concept concept = (Concept) arguments[1];
			final Date dateConverted = (Date) arguments[2];

			triggerStateConversion(patient, concept, dateConverted);
			
			return null;
		}
		
		// hack to prevent 'Exit from care' from setting every single patientprogram to the concept, even the already closed ones
		// e.g. exit from care with patient died will result in mutliple deaths, even if a patient was already discharged from the program.
		// this needs to be fixed
		// mostly copied from core ProgramWorkflowService 
		private void triggerStateConversion(Patient patient, Concept trigger, Date dateConverted) {
			
			// Check input parameters
			if (patient == null)
				throw new APIException("Attempting to convert state of an invalid patient");
			if (trigger == null)
				throw new APIException("Attempting to convert state for a patient without a valid trigger concept");
			if (dateConverted == null)
				throw new APIException("Invalid date for converting patient state");
			
			for (PatientProgram patientProgram : Context.getProgramWorkflowService().getPatientPrograms(patient, null, null, null, null, null, false)) {
				if (patientProgram.getActive(dateConverted)) { // my glorious addition
					Set<ProgramWorkflow> workflows = patientProgram.getProgram().getWorkflows();
					for (ProgramWorkflow workflow : workflows) {
						// (getWorkflows() is only returning over nonretired workflows)
						PatientState patientState = patientProgram.getCurrentState(workflow);
						
						// #1080 cannot exit patient from care  
						// Should allow a transition from a null state to a terminal state
						// Or we should require a user to ALWAYS add an initial workflow/state when a patient is added to a program
						ProgramWorkflowState currentState = (patientState != null) ? patientState.getState() : null;
						ProgramWorkflowState transitionState = workflow.getState(trigger);
						
						if (transitionState != null && workflow.isLegalTransition(currentState, transitionState)) {
							patientProgram.transitionToState(transitionState, dateConverted);
						}
					}
					
					// #1068 - Exiting a patient from care causes "not-null property references
					// a null or transient value: org.openmrs.PatientState.dateCreated". Explicitly
					// calling the savePatientProgram() method will populate the metadata properties.
					// 
					// #1067 - We should explicitly save the patient program rather than let 
					// Hibernate do so when it flushes the session.
					Context.getProgramWorkflowService().savePatientProgram(patientProgram);
				}
			}
		}
	}
}
