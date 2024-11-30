package org.ih.shr.config;

import org.ih.shr.utils.IHConstant;
import org.springframework.stereotype.Component;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;

@Component
public class FhirConfig extends IHConstant {
	FhirContext fhirContext = FhirContext.forR4();

	public IGenericClient getOpenCRFhirContext() {
		IGenericClient openCr = fhirContext
				.newRestfulGenericClient(opencrOpenhimURL);
		BasicAuthInterceptor b = new BasicAuthInterceptor(
				opencrOpenhimAuthentication);
		openCr.registerInterceptor(b);
		return openCr;

	}

	public IGenericClient getGOFRFhirContext() {
		IGenericClient openMRSServer = fhirContext
				.newRestfulGenericClient(gofrOpenhimURL);
		BasicAuthInterceptor openmrsAuthentication = new BasicAuthInterceptor(
				gofrOpenhimAuthentication);
		openMRSServer.registerInterceptor(openmrsAuthentication);
		return openMRSServer;

	}

}
