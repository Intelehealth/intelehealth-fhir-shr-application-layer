package org.ih.shr.service;

import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.web.bind.annotation.RequestParam;

public interface BundleService {

	public String sendBundle(Bundle bundle);

	public Bundle convertToBundle(String bundleString);

	public String search(String resourecType,
			@RequestParam Map<String, String> reqParam);
}
