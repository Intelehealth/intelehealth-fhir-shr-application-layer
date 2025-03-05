package org.ih.shr.utils;

import org.springframework.beans.factory.annotation.Value;

public abstract class IHConstant {

	@Value("${opencr.openhim.url}")
	protected String opencrOpenhimURL;
	@Value("${opencr.openhim.clientid.password.basic.auth}")
	protected String opencrOpenhimAuthentication;

	@Value("${gofr.openhim.url}")
	protected String gofrOpenhimURL;
	@Value("${gofr.openhim.clientid.password.basic.auth}")
	protected String gofrOpenhimAuthentication;
	
	public String[] getOpencrOpenhimCredentials() {
		return opencrOpenhimAuthentication.split(":");
	}
	@Value("${MPI.CODING}")
	protected String MPI_CODING;
	
	@Value("${CENTRAL.FHIR}")
	protected String CENTRAL_FHIR;

}
