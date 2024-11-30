package org.ih.shr.service;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestParam;

public interface PatientSearch {

	public String searchPatient(@RequestParam Map<String, String> reqParam);
}
