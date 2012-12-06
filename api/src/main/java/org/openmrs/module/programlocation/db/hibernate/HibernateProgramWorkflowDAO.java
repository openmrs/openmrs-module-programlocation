package org.openmrs.module.programlocation.db.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.hibernate.SessionFactory;
import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.db.DAOException;

public class HibernateProgramWorkflowDAO extends
		org.openmrs.api.db.hibernate.HibernateProgramWorkflowDAO implements org.openmrs.module.programlocation.db.ProgramWorkflowDAO{

	private SessionFactory sessionFactory;
	
	@Override
	public PatientProgram getPatientProgram(Integer patientProgramId)
			throws DAOException {
		return (org.openmrs.module.programlocation.PatientProgram)sessionFactory.getCurrentSession().get(org.openmrs.module.programlocation.PatientProgram.class, patientProgramId);
	}

	@Override
	public PatientProgram getPatientProgramByUuid(String uuid) {
		return (org.openmrs.module.programlocation.PatientProgram) sessionFactory.getCurrentSession().createQuery(
	    "from PatientProgram pp where pp.uuid = :uuid").setString("uuid", uuid).uniqueResult();
	}

	@Override
	public List<PatientProgram> getPatientPrograms(Cohort cohort,
			Collection<Program> programs) {
		// TODO Auto-generated method stub
		return super.getPatientPrograms(cohort, programs);
	}

	@Override
	public List<PatientProgram> getPatientPrograms(Patient patient,
			Program program, Date minEnrollmentDate, Date maxEnrollmentDate,
			Date minCompletionDate, Date maxCompletionDate,
			boolean includeVoided) throws DAOException {
		// TODO Auto-generated method stub
		List<PatientProgram> patientPrograms = super.getPatientPrograms(patient, program, minEnrollmentDate,
				maxEnrollmentDate, minCompletionDate, maxCompletionDate, includeVoided);
	
		return this.convertToModulePatientProgram(patientPrograms);
	}

	@Override
	public PatientProgram savePatientProgram(PatientProgram patientProgram) {
		if (patientProgram.getPatientProgramId() == null) {
			sessionFactory.getCurrentSession().save(patientProgram);
		} else {
			sessionFactory.getCurrentSession().merge(patientProgram);
		}
		return this.getPatientProgram(patientProgram.getId());
	}	

	public void setSessionFactory(SessionFactory sessionFactory)
    {
        this.sessionFactory = sessionFactory;
        super.setSessionFactory(this.sessionFactory);
    }		
	
	private List<org.openmrs.PatientProgram> convertToModulePatientProgram(
            List<org.openmrs.PatientProgram> openMRSPatientPrograms)
    {
        List<org.openmrs.PatientProgram> modulePatientPrograms = new ArrayList<org.openmrs.PatientProgram>();
        org.openmrs.PatientProgram currOpenMRSPatientProgram = null;

        Iterator<org.openmrs.PatientProgram> iter = openMRSPatientPrograms.iterator();

        while (iter.hasNext())
        {
        	currOpenMRSPatientProgram = (org.openmrs.PatientProgram) iter.next();
        	modulePatientPrograms.add(this.getPatientProgram(currOpenMRSPatientProgram.getId()));
        }
        return modulePatientPrograms;
    }

	
}
