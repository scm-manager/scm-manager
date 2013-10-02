/**
 * Copyright (c) 2013, Clemens Rabe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package sonia.scm.web.filter;

import java.io.IOException;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.user.User;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This filter calls all AutoLoginModule objects to try an auto-login. It can be
 * used on its own, usually at the global context ('/*') or as a base class like
 * BasicAuthenticationFilter.
 * 
 * @author Clemens Rabe
 */
@Singleton
public class AutoLoginFilter extends HttpFilter {

	/** the logger for AutoLoginFilter */
	private static final Logger logger = LoggerFactory
			.getLogger(AutoLoginFilter.class);

	@Deprecated
	public AutoLoginFilter() {
	}

	/**
	 * Constructor.
	 * 
	 * @param autoLoginModules
	 *            - The auto-login modules.
	 */
	@Inject
	public AutoLoginFilter(Set<AutoLoginModule> autoLoginModules) {
		this.autoLoginModules = autoLoginModules;
	}

	@Override
	protected void doFilter(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		User user = getAuthenticatedUser(request, response);

		if (user == null)
			chain.doFilter(request, response);
		else
			chain.doFilter(
					new SecurityHttpServletRequestWrapper(request, user),
					response);
	}

	/**
	 * Check all known AutoLoginModule objects to authenticate the user using
	 * the current request.
	 * 
	 * @param request
	 *            - The servlet request.
	 * @param response
	 *            - The servlet response.
	 * @return The user or null if no user was found.
	 */
	protected User getAuthenticatedUser(HttpServletRequest request,
			HttpServletResponse response) {
		Subject subject = SecurityUtils.getSubject();
		User user = null;

		if (subject.isAuthenticated() || subject.isRemembered()) {
			if (logger.isTraceEnabled()) {
				logger.trace("user is allready authenticated");
			}

			user = subject.getPrincipals().oneByType(User.class);
		} else {
			// Try the known filters first
			for (AutoLoginModule filter : autoLoginModules) {
				user = filter.authenticate(request, response, subject);

				if (user != null) {
					if (logger.isTraceEnabled()) {
						logger.trace(
								"user {} successfully authenticated by authentication filter",
								user.getName());
					}
					break;
				}
			}
		}

		return user;
	}

	/**
	 * Set of AutoLoginModule objects.
	 */
	private Set<AutoLoginModule> autoLoginModules;

}
