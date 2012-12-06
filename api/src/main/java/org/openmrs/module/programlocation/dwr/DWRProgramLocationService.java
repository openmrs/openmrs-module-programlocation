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
package org.openmrs.module.programlocation.dwr;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

public class DWRProgramLocationService {

	protected final Log log = LogFactory.getLog(getClass());

	DateFormat ymdDf = new SimpleDateFormat("yyyy-MM-dd");

	public void updatePatientProgram(Integer patientProgramId,
			String enrollmentDateYmd, String completionDateYmd,
			Integer locationId) throws ParseException {
		org.openmrs.module.programlocation.PatientProgram pp = (org.openmrs.module.programlocation.PatientProgram) Context
				.getProgramWorkflowService()
				.getPatientProgram(patientProgramId);
		Location loc = null;
		if (locationId != null) {
			loc = Context.getLocationService().getLocation(locationId);
		}
		Date dateEnrolled = null;
		Date dateCompleted = null;
		Date ppDateEnrolled = null;
		Date ppDateCompleted = null;
		Location ppLocation = null;
		// If persisted date enrolled is not null then parse to ymdDf format.
		if (null != pp.getDateEnrolled()) {
			String enrolled = ymdDf.format(pp.getDateEnrolled());
			if (null != enrolled && enrolled.length() > 0)
				ppDateEnrolled = ymdDf.parse(enrolled);
		}
		if (null != pp.getLocation()) {
			ppLocation = pp.getLocation();
		}
		// If persisted date enrolled is not null then parse to ymdDf format.
		if (null != pp.getDateCompleted()) {
			String completed = ymdDf.format(pp.getDateCompleted());
			if (null != completed && completed.length() > 0)
				ppDateCompleted = ymdDf.parse(completed);
		}
		// Parse parameter dates to ymdDf format.
		if (enrollmentDateYmd != null && enrollmentDateYmd.length() > 0)
			dateEnrolled = ymdDf.parse(enrollmentDateYmd);
		if (completionDateYmd != null && completionDateYmd.length() > 0)
			dateCompleted = ymdDf.parse(completionDateYmd);
		// If either either parameter and persisted instances
		// of enrollment and completion dates are equal, then anyChange is true.
		boolean anyChange = OpenmrsUtil.nullSafeEquals(dateEnrolled,
				ppDateEnrolled);
		anyChange |= OpenmrsUtil.nullSafeEquals(dateCompleted, ppDateCompleted);
		anyChange |= OpenmrsUtil.nullSafeEquals(loc, ppLocation);

		// Do not update if the enrollment date is after the completion date.
		if (null != dateEnrolled && null != dateCompleted
				&& dateCompleted.before(dateEnrolled)) {
			anyChange = false;
		}
		if (anyChange) {
			pp.setDateEnrolled(dateEnrolled);
			pp.setDateCompleted(dateCompleted);
			pp.setLocation(loc);
			Context.getProgramWorkflowService().updatePatientProgram(pp);
		}
	}
}
