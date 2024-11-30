package org.ih.shr.utils;

import org.springframework.beans.factory.annotation.Value;

public abstract class CommonProperties {

	@Value("${bris.url}")
	protected String BDRIS_URL;
	@Value("${bdris.username}")
	protected String BDRIS_USERNAME;
	@Value("${bdris.password}")
	protected String BDRIS_PASSWORD;

	@Value("${internal.server.url}")
	protected String INTERNALSAVEURL;
	@Value("${internal.server.use}")
	protected int USEINTERNALSERVER;
	@Value("${dhis.url}")
	public String DHIS2URL;
	@Value("${wrong.password}")
	public String worngPassowrd;

	@Value("${opensrp.server.url}")
	protected String opensrpServerUrl;
	@Value("${opensrp.server.username}")
	protected String opensrpUserName;
	@Value("${opensrp.server.password}")
	protected String opensrpPassword;

}
