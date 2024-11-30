package org.ih.shr.service.impl;

import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.ih.shr.config.FhirConfig;
import org.ih.shr.search.param.ReuestParam;
import org.ih.shr.service.PatientSearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import ca.uhn.fhir.context.FhirContext;

@Service
public class PatientSearchImpl implements PatientSearch {
	@Autowired
	private FhirConfig firFhirConfig;
	FhirContext fhirContext = FhirContext.forR4();

	@Override
	public String searchPatient(@RequestParam Map<String, String> theSearchParam) {

		Bundle results = firFhirConfig.getOpenCRFhirContext().search()
				.byUrl("Patient?" + ReuestParam.toQueryParam(theSearchParam))
				.returnBundle(Bundle.class).execute();

		System.err.println("DDD>>>>>>>>"
				+ fhirContext.newJsonParser().setPrettyPrint(true)
						.encodeResourceToString(results));
		return fhirContext.newJsonParser().setPrettyPrint(true)
				.encodeResourceToString(results);
	}

}
