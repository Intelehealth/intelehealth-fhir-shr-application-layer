package org.ih.shr.controller.rest;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Map;

import org.ih.shr.service.PatientMpiService;
import org.ih.shr.service.PatientSearch;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shr/rest/v1/patient")
public class PatientRestController {

	@Autowired
	private PatientSearch patientSearch;

	@Autowired
	private PatientMpiService patientMPIService;

	@GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> searchPatient(@RequestParam Map<String, String> reqParam)
			throws UnsupportedEncodingException, ParseException, JSONException {
		return new ResponseEntity<>(patientSearch.searchPatient(reqParam), HttpStatus.OK);
	}

	@PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> savePatientAndGetMPI(@RequestBody String bundleString) throws UnsupportedEncodingException {
		System.out.println("/rest/v1/patient/save");
		return new ResponseEntity<>(patientMPIService.sendPatient(bundleString), HttpStatus.OK);
	}

}
