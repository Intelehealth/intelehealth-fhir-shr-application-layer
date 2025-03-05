package org.ih.shr.service;

import java.io.UnsupportedEncodingException;

public interface PatientMpiService {
	public String sendPatient(String bundleString) throws UnsupportedEncodingException;

}
