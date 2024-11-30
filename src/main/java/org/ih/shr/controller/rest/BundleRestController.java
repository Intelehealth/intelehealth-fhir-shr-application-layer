package org.ih.shr.controller.rest;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Map;

import org.hl7.fhir.r4.model.Bundle;
import org.ih.shr.service.BundleService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shr/rest/v1/bundle")
public class BundleRestController {

	@Autowired
	private BundleService bundleService;

	@PostMapping("/save")
	public ResponseEntity<?> save(@RequestBody String bundleString)
			throws ParseException {
		Bundle theBundle = bundleService.convertToBundle(bundleString);

		return new ResponseEntity<>(bundleService.sendBundle(theBundle),
				HttpStatus.OK);
	}

	@GetMapping("/{resourceType}")
	public ResponseEntity<?> searchPatient(
			@PathVariable("resourceType") String resourceType,
			@RequestParam Map<String, String> reqParam)
			throws UnsupportedEncodingException, ParseException, JSONException {

		return new ResponseEntity<>(
				bundleService.search(resourceType, reqParam), HttpStatus.OK);
	}

}
