package org.openmrs.module.programlocation.web.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.web.controller.PortletController;
import org.springframework.web.servlet.ModelAndView;

public class ProgramLocationController extends PortletController {

	protected Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public ModelAndView handleRequest(HttpServletRequest arg0,
			HttpServletResponse arg1) throws ServletException, IOException {
		// TODO Auto-generated method stub
		ModelAndView mav = super.handleRequest(arg0, arg1);
		
		mav.setViewName("/module/programlocation/portlets/patientPrograms");
		
		return mav;
	}



	@SuppressWarnings("unchecked")
	protected void populateModel(HttpServletRequest request, Map model) {
		
		if (!model.containsKey("programs")) {
			List<Program> programs = Context.getProgramWorkflowService().getAllPrograms();
			model.put("programs", programs);
		}
		
		// Add the locations
		List<Location> locations = Context.getLocationService().getAllLocations();
		model.put("locations", locations);		
	}
	
}
