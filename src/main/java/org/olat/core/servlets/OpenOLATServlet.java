/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.admin.sysinfo.manager.SessionStatsManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.webdav.WebDAVDispatcher;
import org.olat.core.configuration.PreWarm;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.dispatcher.mapper.GlobalMapperRegistry;
import org.olat.core.dispatcher.mapper.MapperDispatcher;
import org.olat.core.extensions.ExtManager;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.event.FrameworkStartupEventChannel;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.threadlog.RequestBasedLogLevelManager;
import org.olat.core.util.threadlog.UserBasedLogLevelManager;

public class OpenOLATServlet extends HttpServlet {

	private static final long serialVersionUID = -2777749229549683775L;
	private static final OLog log = Tracing.createLoggerFor(OpenOLATServlet.class);
	
    private static final String METHOD_PROPFIND = "PROPFIND";
    private static final String METHOD_PROPPATCH = "PROPPATCH";
    private static final String METHOD_MKCOL = "MKCOL";
    private static final String METHOD_COPY = "COPY";
    private static final String METHOD_MOVE = "MOVE";
    private static final String METHOD_LOCK = "LOCK";
    private static final String METHOD_UNLOCK = "UNLOCK";
    
	private String legacyContext;
	
	private DispatcherModule dispatcherModule;
	private SessionStatsManager sessionStatsManager;
	private RequestBasedLogLevelManager requestBasedLogLevelManager;
	
	private WebDAVDispatcher webDAVDispatcher;
	private Map<String, Dispatcher> dispatchers;
	
	/**
	 * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
	 */
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		
		requestBasedLogLevelManager = RequestBasedLogLevelManager.getInstance();
		if (requestBasedLogLevelManager==null) {
			log.info("init: RequestBasedLogLevelManager is not configured on this system.");
		} else {
			log.info("init: RequestBasedLogLevelManager is configured and will be used.");
		}
		if (UserBasedLogLevelManager.getInstance()==null) {
			log.info("init: UserBasedLogLevelManager is not configured on this system.");
		} else {
			log.info("init: UserBasedLogLevelManager is configured and will be used.");
		}
		
		//the servlet.init method gets called after the spring stuff and all the stuff in web.xml is done
		log.info("Framework has started, sending event to listeners of FrameworkStartupEventChannel");
		FrameworkStartupEventChannel.fireEvent();
		log.info("FrameworkStartupEvent processed by alle listeners. Webapp has started.");
		sessionStatsManager = CoreSpringFactory.getImpl(SessionStatsManager.class);
		dispatcherModule = CoreSpringFactory.getImpl(DispatcherModule.class);
		
		dispatchers = new HashMap<String, Dispatcher>(dispatcherModule.getDispatchers());
		dispatchers.put(DispatcherModule.PATH_MAPPED, new MapperDispatcher());
		dispatchers.put(DispatcherModule.PATH_GLOBAL_MAPPED,  GlobalMapperRegistry.getInstance());
		
		webDAVDispatcher = CoreSpringFactory.getImpl(WebDAVDispatcher.class);
		dispatchers.put(DispatcherModule.WEBDAV_PATH, webDAVDispatcher);
		
		Settings settings = CoreSpringFactory.getImpl(Settings.class);
		if(StringHelper.containsNonWhitespace(settings.getLegacyContext())) {
			legacyContext = settings.getLegacyContext();
			// same pattern as dispatcher: /olat/
			if(!legacyContext.startsWith("/")) {
				legacyContext = "/" + legacyContext;
			}
			if(!legacyContext.endsWith("/")) {
				legacyContext += "/";
			}
		}
		
		//preload extensions
		ExtManager.getInstance().getExtensions();
		preWarm();
	}
	
	private void preWarm() {
		TaskExecutorManager executor = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		Map<String,PreWarm> preWarms = CoreSpringFactory.getBeansOfType(PreWarm.class);
		for(PreWarm preWarm:preWarms.values()) {
			executor.execute(preWarm);
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		//log.info(req.getMethod() + " :: " + req.getPathInfo());

		Tracing.setUreq(req);
		ThreadLocalUserActivityLoggerInstaller.initUserActivityLogger(req);
		WorkThreadInformations.set("Serve request: " + req.getRequestURI());
		if(sessionStatsManager != null) {
			sessionStatsManager.incrementRequest();
			sessionStatsManager.incrementConcurrentCounter();
		}
		try{
			
			final String method = req.getMethod();
			if (method.equals(METHOD_PROPFIND)) {
				webDAVDispatcher.execute(req, resp);
	        } else if (method.equals(METHOD_PROPPATCH)) {
	        	webDAVDispatcher.execute(req, resp);
	        } else if (method.equals(METHOD_MKCOL)) {
	        	webDAVDispatcher.execute(req, resp);
	        } else if (method.equals(METHOD_COPY)) {
	        	webDAVDispatcher.execute(req, resp);
	        } else if (method.equals(METHOD_MOVE)) {
	        	webDAVDispatcher.execute(req, resp);
	        } else if (method.equals(METHOD_LOCK)) {
	        	webDAVDispatcher.execute(req, resp);
	        } else if (method.equals(METHOD_UNLOCK)) {
	        	webDAVDispatcher.execute(req, resp);
	        } else {
	            super.service(req, resp);
	        }
			
		} finally {
			if (requestBasedLogLevelManager != null) {
				requestBasedLogLevelManager.deactivateRequestBasedLogLevel();
			}
			if(sessionStatsManager != null) {
				sessionStatsManager.decrementConcurrentCounter();
			}
			WorkThreadInformations.unset();
			ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
			I18nManager.remove18nInfoFromThread();
			Tracing.setUreq(null);
			//let it at the end
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		String subContext = DispatcherModule.getFirstPath(req);
		if("/".equals(subContext)) {
			webDAVDispatcher.doRootOptions(req, resp);
		} else if("/webdav".equals(subContext) || "/webdav/".equals(subContext)) {
			webDAVDispatcher.doWebdavOptions(req, resp);
		} else {
			super.doOptions(req, resp);
		}
	}

	/**
	 * Called when the HTTP request method is GET. This method just calls the
	 * doPost() method.
	 * 
	 * @param request The HTTP request
	 * @param response The HTTP response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException {
		executeUserRequest(request, response);
	}

	/**
	 * Called when the HTTP request method is POST. This method provides the main
	 * control logic.
	 * 
	 * @param request The HTTP request
	 * @param response The HTTP response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		executeUserRequest(request, response);
	}
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		webDAVDispatcher.execute(req, resp);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		webDAVDispatcher.execute(req, resp);
	}

	@Override
	protected void doHead(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		String subContext = DispatcherModule.getFirstPath(req);
		if("/".equals(subContext)) {
			webDAVDispatcher.execute(req, resp);
		} else if("/webdav".equals(subContext) || "/webdav/".equals(subContext)) {
			webDAVDispatcher.execute(req, resp);
		} else {
			executeUserRequest(req, resp);
		}
	}

	/**
	 * Initialize tracing with request, this allows debugging information as IP, User-Agent.
	 * @param request
	 * @param response
	 */
	private void executeUserRequest(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		if (requestBasedLogLevelManager != null) {
			requestBasedLogLevelManager.activateRequestBasedLogLevel(request);
		}
		
		final String dispatcherName = DispatcherModule.getFirstPath(request);
		if(dispatcherName != null && !dispatcherName.startsWith("/webdav")) {
			String userAgent = request.getHeader("User-Agent");
			if(userAgent != null && userAgent.indexOf("BitKinex") >= 0) {
				//BitKinex isn't allow to see this context
				response.sendError(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}
		
		if(legacyContext != null && legacyContext.equals(dispatcherName)) {
			String uri = request.getRequestURI();
			String redirectUri = uri.substring(legacyContext.length() - 1, uri.length());
			RequestDispatcher dispatcher = request.getRequestDispatcher(redirectUri);
			dispatcher.forward(request, response);
		} else if(dispatchers.containsKey(dispatcherName)) {
			I18nManager.attachI18nInfoToThread(request);
			Dispatcher dispatcher = dispatchers.get(dispatcherName);
			dispatcher.execute(request, response);
		} else {
			//root -> redirect to dmz
			if("/".equals(dispatcherName)) {
				redirectToDmz(response);
			} else if("/dmz".equals(dispatcherName)) {
				redirectToDmz(response);
			} else {
				String uri = request.getRequestURI();
				if(uri != null && uri.contains("/raw/_noversion_/")) {
					//cut and redirect
					int index = uri.indexOf("/raw/_noversion_/");
					if(index > 0) {
						String redirectUri = Settings.getServerContextPathURI() + uri.substring(index, uri.length());
						response.sendRedirect(redirectUri);
					} else {
						response.sendError(HttpServletResponse.SC_NOT_FOUND);
					}
				} else {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				}
			}
		}
	}
	
	private void redirectToDmz(HttpServletResponse response)
	throws IOException {
		String dmzUri = WebappHelper.getServletContextPath() + DispatcherModule.getPathDefault();
		response.sendRedirect(dmzUri);
	}
}
