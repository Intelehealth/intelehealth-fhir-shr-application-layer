package org.ih.shr.controller.rest;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Map;

import org.ih.shr.service.PatientSearch;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/v1/patient")
public class PatientRestController {

	@Autowired
	private PatientSearch patientSearch;

	@GetMapping("/search")
	public ResponseEntity<?> searchPatient(
			@RequestParam Map<String, String> reqParam)
			throws UnsupportedEncodingException, ParseException, JSONException {

		return new ResponseEntity<>(patientSearch.searchPatient(reqParam),
				HttpStatus.OK);
	}

}
