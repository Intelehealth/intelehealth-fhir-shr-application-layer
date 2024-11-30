package org.ih.shr.service.impl;

import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Resource;
import org.ih.shr.config.FhirConfig;
import org.ih.shr.search.param.ReuestParam;
import org.ih.shr.service.BundleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.uhn.fhir.context.FhirContext;

@Service
public class BundleServiceImpl implements BundleService {

	@Autowired
	private FhirConfig firFhirConfig;
	FhirContext fhirContext = FhirContext.forR4();

	@Override
	public String sendBundle(Bundle originalTasksBundle) {
		Bundle transactionBundle = new Bundle();
		transactionBundle.setType(Bundle.BundleType.TRANSACTION);
		for (BundleEntryComponent bundleEntry : originalTasksBundle.getEntry()) {
			Resource resource = (Resource) bundleEntry.getResource();

			Bundle.BundleEntryComponent component = transactionBundle
					.addEntry();
			component.setResource(resource);
			component
					.getRequest()
					.setUrl(resource.fhirType() + "/"
							+ resource.getIdElement().getIdPart())
					.setMethod(Bundle.HTTPVerb.PUT);

		}

		System.err.println("DDD>>>>>>>>"
				+ fhirContext.newJsonParser().setPrettyPrint(true)
						.encodeResourceToString(transactionBundle));

		firFhirConfig.getOpenCRFhirContext().transaction()
				.withBundle(transactionBundle).execute();
		return fhirContext.newJsonParser().setPrettyPrint(true)
				.encodeResourceToString(transactionBundle);
	}

	@Override
	public Bundle convertToBundle(String bundleString) {
		Bundle theBundle = fhirContext.newJsonParser().parseResource(
				Bundle.class, bundleString);

		return theBundle;
	}

	@Override
	public String search(String resourecType, Map<String, String> reqParam) {
		Bundle results = firFhirConfig.getOpenCRFhirContext().search()
				.byUrl(resourecType + "?" + ReuestParam.toQueryParam(reqParam))
				.returnBundle(Bundle.class).execute();

		System.err.println("DDD>>>>>>>>"
				+ fhirContext.newJsonParser().setPrettyPrint(true)
						.encodeResourceToString(results));
		return fhirContext.newJsonParser().setPrettyPrint(true)
				.encodeResourceToString(results);
	}

}
