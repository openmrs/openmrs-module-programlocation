package org.openmrs.module.programlocation.impl;

public class ProgramWorkflowServiceImpl extends
		org.openmrs.api.impl.ProgramWorkflowServiceImpl implements
		org.openmrs.module.programlocation.ProgramWorkflowService {

	public ProgramWorkflowServiceImpl() {
		
	}
	
	public void setProgramWorkflowDAO(org.openmrs.module.programlocation.db.hibernate.HibernateProgramWorkflowDAO dao)
    {
        this.dao = dao;
        super.setProgramWorkflowDAO(this.dao);
    }
}
