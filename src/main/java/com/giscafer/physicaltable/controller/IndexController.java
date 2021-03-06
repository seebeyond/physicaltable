package com.giscafer.physicaltable.controller;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.giscafer.physicaltable.Constant.ConfigConstant;
import com.giscafer.physicaltable.interceptor.LoginValidator;
import com.giscafer.physicaltable.interceptor.TimeInterceptor;
import com.giscafer.physicaltable.model.Reportcard;
import com.giscafer.physicaltable.service.IReportcardService;
import com.giscafer.physicaltable.service.impl.ReportcardServiceImpl;
import com.giscafer.physicaltable.util.BeanMapUtil;
import com.giscafer.physicaltable.util.POIExcelUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.ehcache.CacheInterceptor;

/**
 * index控制器
 * 
 * @author netbuffer
 * 
 */
@Before({ TimeInterceptor.class })
public class IndexController extends Controller {
	private static Logger logIndex = LoggerFactory
			.getLogger(IndexController.class);
	private static IReportcardService reportService = Enhancer
			.enhance(ReportcardServiceImpl.class);

	/*
	 * jfinal默认访问不到的路径都会访问这个index方法来处理，如果想让找不到的路径引发404，可以加NoUrlPara这个拦截器
	 */
	public void index() {
		Object login = getSession().getAttribute(ConfigConstant.ISLOGIN);
		if ((null != login && Boolean.parseBoolean(login.toString()))) {// 已经登陆
			redirect("/reportcard");
		}else{
			render("/login.html");
		}
	}

	@Before({ LoginValidator.class })
	public void login() {
		Prop p = PropKit.use("config.txt");
		String cu = p.get("musername");
		String cp = p.get("mpassword");
		logIndex.debug("配置:" + ToStringBuilder.reflectionToString(p));
		String username = getPara("username");
		String password = getPara("password");
		if (cu.equals(username) && cp.equals(password)) {
			getSession().setAttribute(ConfigConstant.ISLOGIN, true);
			getSession().setAttribute(ConfigConstant.USERNAME, cu);
			logIndex.info("登陆信息:" + ToStringBuilder.reflectionToString(this));
			setCookie(ConfigConstant.USERNAME, username, 86400);// 1天免登陆
			redirect("/reportcard");
			//
		} else {
			logIndex.info("登陆失败!");
			redirect("/login.html");
		}
	}

	public void exit() {
		getSession().setAttribute(ConfigConstant.ISLOGIN, false);
		getSession().removeAttribute(ConfigConstant.ISLOGIN);
		getSession().removeAttribute(ConfigConstant.USERNAME);
		clearCookies();
		render("/login.html");
	}
	/**
     * 清除所有cookie
     * @param req
     * @param res
     */
    public void clearCookies() {
        Cookie[] cookies = getRequest().getCookies();
        for(int i = 0,len = cookies.length; i < len; i++) {
            Cookie cookie = new Cookie(cookies[i].getName(),null);
            cookie.setMaxAge(0);
            cookie.setPath("/");
            getResponse().addCookie(cookie);
        }
    }
	/**
	 * 开启缓存
	 */
	@Before({ CacheInterceptor.class })
	public void newdata() {
		logIndex.info("newdata执行!");
		int newcount = reportService.getNewData();
		String username = getSession().getAttribute(ConfigConstant.USERNAME)
				.toString();
		setAttr("newcount", newcount);
		setAttr("username", username);
		renderJson();
	}

	/*
	 * public void export(){ SimpleDateFormat format=new
	 * SimpleDateFormat("yyyyMMddHHmmss"); List<Reportcard>
	 * users=Reportcard.dao.
	 * find("select * from "+ConfigConstant.REPORTCARDTABLE+" limit 150");
	 * String
	 * projectPath=getRequest().getServletContext().getRealPath("export"); int
	 * userCount=users.size(); List<Map<String, Object>> mps=new
	 * ArrayList<Map<String,Object>>(users.size()); for(int
	 * i=0;i<userCount;i++){ Map<String, Object>
	 * m=BeanMapUtil.transBean2Map(users.get(i)); mps.add(m); }
	 * logIndex.info("report:"+mps.toString()); List<String> titles=new
	 * ArrayList<String>(mps.get(0).size()-1); titles.add("adddate");
	 * titles.add("age"); titles.add("deliveryaddress"); titles.add("id");
	 * titles.add("name"); titles.add("phone"); titles.add("sex"); String
	 * file=projectPath+format.format(new Date())+"."+ConfigConstant.EXCELSTR;
	 * POIExcelUtil.export(titles, mps,file); renderFile(new File(file)); }
	 */

	public void testWrapper() {
		renderJson(getRequest().getParameterMap());
	}
}
