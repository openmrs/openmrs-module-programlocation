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
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.programlocation.web.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.programlocation.PatientProgram;
import org.openmrs.module.programlocation.ProgramWorkflowService;
import org.openmrs.web.WebConstants;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.servlet.view.RedirectView;

public class PatientProgramFormController implements Controller {
	
	protected final Log log = LogFactory.getLog(getClass());
	
	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// can't do anything without a method
		return null;
	}
	
	public ModelAndView enroll(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	                                                                                    IOException {
		
		String returnPage = request.getParameter("returnPage");
		if (returnPage == null) {
			throw new IllegalArgumentException("must specify a returnPage parameter in a call to enroll()");
		}
		
		String patientIdStr = request.getParameter("patientId");
		String programIdStr = request.getParameter("programId");
		String enrollmentDateStr = request.getParameter("dateEnrolled");
		String locationIdStr = request.getParameter("locationId");
		String completionDateStr = request.getParameter("dateCompleted");
		
		log.debug("enroll " + patientIdStr + " in " + programIdStr + " on " + enrollmentDateStr);
		
		ProgramWorkflowService pws = Context.getService(ProgramWorkflowService.class);
		
		// make sure we parse dates the same was as if we were using the initBinder + property editor method 
		CustomDateEditor cde = new CustomDateEditor(Context.getDateFormat(), true, 10);
		cde.setAsText(enrollmentDateStr);
		Date enrollmentDate = (Date) cde.getValue();
		cde.setAsText(completionDateStr);
		Date completionDate = (Date) cde.getValue();
		Patient patient = Context.getPatientService().getPatient(Integer.valueOf(patientIdStr));
		
		Location location;
		try {
			location = Context.getLocationService().getLocation(Integer.valueOf(locationIdStr));
		} catch (Exception e) {
			location = null;
		}
			
		Program program = pws.getProgram(Integer.valueOf(programIdStr));
		
		List<org.openmrs.PatientProgram> pps = pws.getPatientPrograms(patient, program, null, completionDate, enrollmentDate, null, false);
		
		if (!pps.isEmpty())
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Program.error.already");
		else {
			PatientProgram pp = new PatientProgram();
			pp.setPatient(patient);
			pp.setLocation(location);
			pp.setProgram(program);
			pp.setDateEnrolled(enrollmentDate);
			pp.setDateCompleted(completionDate);
			Context.getProgramWorkflowService().savePatientProgram(pp);
		}
		return new ModelAndView(new RedirectView(returnPage));
	}
	
	public ModelAndView complete(HttpServletRequest request, HttpServletResponse response) throws ServletException,
	                                                                                      IOException {
	
		
		String returnPage = request.getParameter("returnPage");
		if (returnPage == null) {
			throw new IllegalArgumentException("must specify a returnPage parameter in a call to enroll()");
		}
		
		String patientProgramIdStr = request.getParameter("patientProgramId");
		String dateCompletedStr = request.getParameter("dateCompleted");
		
		// make sure we parse dates the same was as if we were using the initBinder + property editor method 
		CustomDateEditor cde = new CustomDateEditor(Context.getDateFormat(), true, 10);
		cde.setAsText(dateCompletedStr);
		Date dateCompleted = (Date) cde.getValue();
		
		org.openmrs.PatientProgram p = Context.getProgramWorkflowService().getPatientProgram(Integer.valueOf(patientProgramIdStr));
		p.setDateCompleted(dateCompleted);
		Context.getProgramWorkflowService().savePatientProgram(p);
		
		return new ModelAndView(new RedirectView(returnPage));
	}
	
}
