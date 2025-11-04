package ro.gs1.btmock.beans;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named("environmentBean")
@ApplicationScoped
public class EnvironmentBean {

	private static final Logger LOG = Logger.getLogger(EnvironmentBean.class);

	/**
	 * Returns the application base URL. 1️⃣ First tries to read from environment
	 * variable (e.g., APP_BASE_URI) 2️⃣ If missing, falls back to the Faces
	 * ExternalContext request context path
	 */
	public String getBaseUri() {
		String envUri = System.getenv("APP_BASE_URI");
		if (envUri != null && !envUri.isBlank()) {
			LOG.debugf("getBaseUri()- using env %s", envUri);
			return envUri;
		}

		FacesContext facesContext = FacesContext.getCurrentInstance();
		if (facesContext != null) {
			LOG.debug("getBaseUri()- found current instance");
			ExternalContext externalContext = facesContext.getExternalContext();
			if (externalContext != null) {
				LOG.debug("getBaseUri()- found external context");
				String requestContextPath = externalContext.getRequestContextPath();
				String scheme = externalContext.getRequestScheme();
				String serverName = externalContext.getRequestServerName();
				int port = externalContext.getRequestServerPort();

				  LOG.debug("getBaseUri()- using external context");
				return String.format("%s://%s:%d%s", scheme, serverName, port, requestContextPath);
			}
		}

		  LOG.debug("getBaseUri()- using fallback");
		return "http://localhost:8080";
	}
}
