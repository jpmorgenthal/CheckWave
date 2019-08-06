package com.jpmorgenthal.checkwave;
/**
Copyright (c) 2009 jpmorgenthal.com

Permission is hereby granted, free of charge, to any person or organization 
obtaining a copy of this software and associated  documentation  files (the 
"Software"), to deal in the Software without restriction, including without 
limitation the rights to use, copy, modify, merge, publish, distribute, sub
license, and/or sell copies of the Software, and to permit persons  to whom 
the Software is furnished to do so, subject to the following conditions:

The above copyright notice and  this permission notice shall be included in 
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS",  WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING  BUT NOT  LIMITED  TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR  COPYRIGHT  HOLDERS  BE  LIABLE  FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY,  WHETHER  IN  AN  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
FROM,  OUT  OF  OR  IN  CONNECTION  WITH  THE  SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE
*/
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;


@SuppressWarnings("serial")
public class GoogleIDMapper extends HttpServlet 
{
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	UserService userService = UserServiceFactory.getUserService();
    	User user = userService.getCurrentUser();

		resp.setContentType("text/html");
		if (req.getParameter("googlewaveid")==null)
		{
			resp.getWriter().println("Google Wave ID must be filled in");
			return;
		}
    	if (user != null) {
    		resp.getWriter().println("Hello, " + user.getNickname()+" your email is "+user.getEmail());
    		resp.getWriter().println("<a href=\""+userService.createLogoutURL(req.getRequestURI())+"\">Logout</a>");
    	} else {
    		resp.sendRedirect(userService.createLoginURL(req.getRequestURI()));
    	}
    }
}
