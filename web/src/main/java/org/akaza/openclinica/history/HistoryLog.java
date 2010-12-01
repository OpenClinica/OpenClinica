package org.akaza.openclinica.history;

import java.io.IOException;
import org.akaza.openclinica.bean.login.UserAccountBean;
import org.akaza.openclinica.control.SpringServletAccess;
import org.akaza.openclinica.dao.hibernate.HistoryLogDao;
import org.akaza.openclinica.domain.history.HistoryLogBean;
import org.akaza.openclinica.view.Page;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class HistoryLog implements Filter{

	
	
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain filterChain) throws IOException, ServletException {

		String uri;
		String queryString;
		
		HttpServletRequest request = (HttpServletRequest)req;
		//System.out.println(request.getRequestURI());

		uri = request.getRequestURI();
		queryString = request.getQueryString();
	String url = uri+"?"+queryString;
		System.out.println("URL : to be recorded::::"+uri+queryString);
		System.out.println("Link Name "+request.getAttribute("titleName"));
		UserAccountBean userAccount  = (UserAccountBean)request.getSession().getAttribute("userBean");
		
		HistoryLogBean historyLogBean  = new HistoryLogBean();
		historyLogBean.setOwner(userAccount.getOwner());
		historyLogBean.setVisited_link(url);
		HistoryLogDao historyLogDao = getHistoryLogDao(request.getSession().getServletContext());
		historyLogDao.saveOrUpdate(historyLogBean);//TODO:it should only be a insert change after
	//	filterChain.doFilter(req, res);//Continue with the original filtering
		request.getSession().getServletContext().getRequestDispatcher(Page.VIEW_STUDY_SUBJECT.getFileName()).forward(req, res);
	}

	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

	public HistoryLogDao getHistoryLogDao(ServletContext context)
	{
		return (HistoryLogDao)SpringServletAccess.getApplicationContext(context).getBean(
        "historyLogDao");
	}
	
}
