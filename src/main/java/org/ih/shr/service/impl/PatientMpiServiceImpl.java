package org.ih.shr.service.impl;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleEntryResponseComponent;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.ih.shr.config.FhirConfig;
import org.ih.shr.service.BundleService;
import org.ih.shr.service.PatientMpiService;
import org.ih.shr.utils.HttpWebClient;
import org.ih.shr.utils.IHConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import ca.uhn.fhir.context.FhirContext;

@Service
public class PatientMpiServiceImpl extends IHConstant implements PatientMpiService {

	@Autowired
	BundleService bundleService;

	@Autowired
	private FhirConfig firFhirConfig;

	FhirContext fhirContext = FhirContext.forR4();

	@Override
	public String sendPatient(String bundleString) throws UnsupportedEncodingException {
		Bundle bundle = bundleService.convertToBundle(bundleString);

		// Case 1: If bundle already has MPI, just update
		if (hasMPI(bundle)) {
			String mpiId = getMPI(bundle);
			for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
				Patient patient = (Patient) bundleEntry.getResource();
				return updatePatientWithId(patient, mpiId);
			}
		}

		// Case 2 & 3: Process each entry
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();

			Bundle searchBundle = searchBundle(patient);
			String mpiId = null;

			// Extract MPI if available from search
			if (hasMPI(searchBundle)) {
				mpiId = getMPI(searchBundle);
			} else {
				mpiId = extractResourceId(searchBundle);
			}

			// Case 2: MPI already exists from search
			if (mpiId != null) {
				patient.setId(mpiId);
				patient = mergeIdentifier(patient, searchBundle);
				if (!hasMPI(searchBundle)) {
					patient.getIdentifier().add(getNewMPIIdentifierWithMpiID(bundle, mpiId));
				}
				return updatePatientWithId(patient, mpiId);
			}

			// Case 3: No MPI, need to create and then update with assigned ID
			Bundle createTransaction = new Bundle();
			createTransaction.setType(Bundle.BundleType.TRANSACTION);
			createTransaction.addEntry().setResource(patient).getRequest().setMethod(Bundle.HTTPVerb.POST)
					.setUrl("Patient");

			Bundle createResponse = firFhirConfig.getOpenCRFhirContext().transaction().withBundle(createTransaction)
					.execute();

			mpiId = extractResponseId(createResponse);
			if (mpiId == null)
				throw new RuntimeException("Failed to create patient and retrieve ID");

			// Update Patient with MPI identifier
			patient.setId(mpiId);
			patient.getIdentifier().add(getNewMPIIdentifierWithMpiID(bundle, mpiId));

			return updatePatientWithId(patient, mpiId);
		}

		return null;
	}

	// Extracted utility method for updating patient
	private String updatePatientWithId(Resource resource, String mpiId) {
		Bundle updateTransaction = new Bundle();
		updateTransaction.setType(Bundle.BundleType.TRANSACTION);

		updateTransaction.addEntry().setResource(resource).getRequest().setMethod(Bundle.HTTPVerb.PUT)
				.setUrl(resource.fhirType() + "/" + mpiId);

		Bundle updateResponse = firFhirConfig.getOpenCRFhirContext().transaction().withBundle(updateTransaction)
				.execute();

		return fhirContext.newJsonParser().setPrettyPrint(true).encodeResourceToString(updateTransaction);
	}

	private Identifier copyMPIIdentifier(Bundle searchBundle) {
		for (BundleEntryComponent bundleEntry : searchBundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();
			for (Identifier identifier : patient.getIdentifier()) {
				if (identifier.getType().getText().equals("MPI")) {
					return identifier;
				}
			}
		}
		return null;
	}

	public boolean hasMPI(Bundle bundle) {
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();
			for (Identifier identifier : patient.getIdentifier()) {
				if (identifier.getType().getText().equals("MPI")) {
					return true;
				}
			}
		}
		return false;
	}

	public String getMPI(Bundle bundle) {
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();
			for (Identifier identifier : patient.getIdentifier()) {
				if (identifier.getType().getText().equals("MPI")) {
					return identifier.getValue();
				}
			}
		}
		return null;
	}

	public Identifier getNewMPIIdentifierWithMpiID(Bundle bundle, String mpiId) {
		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			if (bundleEntry.getResource() instanceof Patient) { // Ensure the resource is a Patient
				Patient patient = (Patient) bundleEntry.getResource();
				for (Identifier identifier : patient.getIdentifier()) {
					Identifier mpiIdentifier = identifier.copy();
					mpiIdentifier.setId(UUID.randomUUID().toString());
					mpiIdentifier.setValue(mpiId);

					// Ensure the coding list is initialized and has at least one element
					if (mpiIdentifier.getType().getCoding().isEmpty()) {
						mpiIdentifier.getType().addCoding(new Coding());
					}

					mpiIdentifier.getType().setText("MPI");
					mpiIdentifier.setSystem(CENTRAL_FHIR + "/StructureDefinition/MPI");
					mpiIdentifier.getType().getCoding().get(0).setSystem(CENTRAL_FHIR + "/CodeSystem/MPI");
					mpiIdentifier.getType().getCoding().get(0).setCode("MPI");
					return mpiIdentifier;
				}
			}
		}
		return null;
	}

	private List<Identifier> getIdentifier(Bundle bundle) {
		if (bundle.getEntry().size() == 0)
			return new ArrayList<>();

		Patient patient = (Patient) bundle.getEntryFirstRep().getResource();

		return patient.getIdentifier();
	}

	private String extractResourceId(Bundle bundle) {
		if (bundle.getEntry().size() == 0)
			return null;
		Resource resource = bundle.getEntryFirstRep().getResource();
		return resource.getIdElement().getIdPart();
	}

	private String extractResponseId(Bundle bundle) {
		if (bundle.getEntry().size() == 0)
			return null;
		BundleEntryResponseComponent response = bundle.getEntryFirstRep().getResponse();
		return response.getLocation().split("/")[1];
	}
	
	private String makeQueryParam(Patient patient) {
	    UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
	    
	    if (patient.getBirthDate() != null) {
	    	String dob = new SimpleDateFormat("yyyy-MM-dd").format(patient.getBirthDate()).toString();
	        builder.queryParam("birthdate", dob);
	    }
	    
	    if (patient.getNameFirstRep().getFamily() != null) {
	        builder.queryParam("family", patient.getNameFirstRep().getFamily());
	    }
	    
	    if (patient.getNameFirstRep().getGivenAsSingleString() != null) {
	        builder.queryParam("given", patient.getNameFirstRep().getGivenAsSingleString());
	    }
	    
	    if (patient.getGender() != null) {
	        builder.queryParam("gender", patient.getGender().toCode());
	    }
	    
//	    if (!patient.getTelecom().isEmpty()) {
//	        String phoneNumber = patient.getTelecom().get(0).getValue();
//	        builder.queryParam("telecom",phoneNumber);
//	    }
	    
	    String query =  builder.build().getQuery();
	    return query;
	}

	private Bundle searchBundle(Patient patient) throws UnsupportedEncodingException {
		String queryParam = makeQueryParam(patient);

		String response = HttpWebClient.get(opencrOpenhimURL + "/Patient", "?" + queryParam,
				getOpencrOpenhimCredentials()[0], getOpencrOpenhimCredentials()[1]);

		System.err.println("Search bundle response: >>>>> " + response);

		return fhirContext.newJsonParser().parseResource(Bundle.class, response);
	}

	private List<Identifier> getIdentifiers(Bundle bundle) {
		if (bundle.getEntry().size() < 1) {
			return new ArrayList<>();
		}

		for (BundleEntryComponent bundleEntry : bundle.getEntry()) {
			Patient patient = (Patient) bundleEntry.getResource();
			return patient.getIdentifier();

		}
		return new ArrayList<>();
	}

	private Patient mergeIdentifier(Patient patient, Bundle bundle) {
		List<Identifier> identifiers = getIdentifiers(bundle);
		for (Identifier identifier : identifiers) {
			if (matchWithLocalIdentifier(patient, identifier))
				continue;
			patient.getIdentifier().add(identifier);
		}
		return patient;
	}

	private boolean matchWithLocalIdentifier(Patient localPatient, Identifier identifier) {
		for (Identifier localIdentifier : localPatient.getIdentifier()) {
			if (localIdentifier.getValue().equals(identifier.getValue()))
				return true;
		}
		return false;
	}
}
