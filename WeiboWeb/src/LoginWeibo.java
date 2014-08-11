

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.http.NameValuePair;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class LoginWeibo {

	HttpClient httpclient = new DefaultHttpClient();
	HttpGet httpget = null;
	HttpPost httpost = null;
	HttpResponse response = null;
	String LoginURL = "https://login.sina.com.cn/sso/login.php?client=ssologin.js(v1.4.15)";
	String PreLogin = "http://login.sina.com.cn/sso/prelogin.php?entry=account&callback=sinaSSOController.preloginCallBack&su=USERNAME&rsakt=mod&client=ssologin.js(v1.4.15)";
	String Username = "*************";
	String Password = "*************";
	String sysdate = "";
	
	{
		String base64 = Base64.getMimeEncoder().encodeToString(URLEncoder.encode(Username).getBytes());
		PreLogin = PreLogin.replaceAll("USERNAME", base64);
		sysdate = new Long(System.currentTimeMillis()).toString();
		//LoginURL += "&_=" + sysdate;
	}
	
	public static void main(String[] args) throws Exception {
		LoginWeibo obj = new LoginWeibo();
		HashMap<String,String> LoginMap = obj.getPreLogin();
		obj.encodeUserPass(LoginMap);
		obj.login(LoginMap);
	}
	
	public HashMap<String,String> getPreLogin() throws Exception {
		URL url = new URL(PreLogin);
		InputStream is = url.openStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuffer sb = new StringBuffer();
		String line = "";
		while((line = br.readLine()) != null) {
			sb.append(line);
		}
		String resultJSON = sb.toString();
		resultJSON = resultJSON.substring(resultJSON.indexOf('{'), resultJSON.length()-1);
		JSONObject objson = new JSONObject(resultJSON);
		String servertime = objson.getString("servertime");
		String nonce = objson.getString("nonce");
		String pubkey = objson.getString("pubkey");
		String rsakv = objson.getString("rsakv");
		System.out.println(pubkey);
		//pubkey = new BigInteger(pubkey, 16).toString();
		HashMap<String,String> PreLoginMap = new HashMap<String,String>(); 
		PreLoginMap.put("servertime", servertime);
		PreLoginMap.put("nonce", nonce);
		PreLoginMap.put("pubkey", pubkey);
		PreLoginMap.put("rsakv", rsakv);
		System.out.println(pubkey);
		return PreLoginMap;
	}
	
	public void encodeUserPass( HashMap<String,String> LoginMap ) throws Exception {
	
		ScriptEngineManager sem = new ScriptEngineManager();
		ScriptEngine se = sem.getEngineByName("javascript");
		String encodeJS = FileUtils.readFileToString(new File("ssologin.js"));
		se.eval(encodeJS);
		String encode_password = "";
		if ( se instanceof Invocable ) {
			Invocable invoke = (Invocable) se;
			encode_password = invoke.invokeFunction("sinaRsa", 
					LoginMap.get("pubkey"),LoginMap.get("servertime"), LoginMap.get("nonce"), Password).toString();
			//encode_password = invoke.invokeFunction("getpass",
              //      Password, LoginMap.get("servertime"), LoginMap.get("nonce"), LoginMap.get("pubkey")).toString();
		}
		LoginMap.put("sp", encode_password);
		LoginMap.put("su", Base64.getMimeEncoder().encodeToString(URLEncoder.encode(Username).getBytes()));
		System.out.println(encode_password);
	}
	
	public void login(HashMap<String,String> LoginMap) throws Exception {
		httpost = new HttpPost(LoginURL);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		/*nvps.add(new BasicNameValuePair("entry", "account"));
		nvps.add(new BasicNameValuePair("gateway", "1"));
		nvps.add(new BasicNameValuePair("from", ""));
		nvps.add(new BasicNameValuePair("savestate", "30"));
		nvps.add(new BasicNameValuePair("useticket", "0"));
		nvps.add(new BasicNameValuePair("pagerefer", "http://login.sina.com.cn/sso/logout.php"));
		nvps.add(new BasicNameValuePair("vsnf", "1"));
		nvps.add(new BasicNameValuePair("su", LoginMap.get("su")));
		nvps.add(new BasicNameValuePair("service", "sso"));
		nvps.add(new BasicNameValuePair("servertime", LoginMap.get("servertime")));
		nvps.add(new BasicNameValuePair("nonce", LoginMap.get("nonce")));
		nvps.add(new BasicNameValuePair("pwencode", "rsa2"));
		nvps.add(new BasicNameValuePair("rsakv", LoginMap.get("rsakv")));
		nvps.add(new BasicNameValuePair("sp", LoginMap.get("sp")));
		nvps.add(new BasicNameValuePair("sr", "1366*768"));
		nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
		nvps.add(new BasicNameValuePair("cdult", "3"));
		nvps.add(new BasicNameValuePair("domain", "sina.com.cn"));
		nvps.add(new BasicNameValuePair("prelt", "76"));
		nvps.add(new BasicNameValuePair("returntype", "TEXT"));*/
		nvps.add(new BasicNameValuePair("entry", "weibo"));
		nvps.add(new BasicNameValuePair("gateway", "1"));
		nvps.add(new BasicNameValuePair("from", ""));
		nvps.add(new BasicNameValuePair("savestate", "7"));
		nvps.add(new BasicNameValuePair("useticket", "1"));
		nvps.add(new BasicNameValuePair("pagerefer", "http://login.sina.com.cn/sso/logout.php?entry=miniblog&r=http://weibo.com/logout.php?backurl=%2F"));
		nvps.add(new BasicNameValuePair("vsnf", "1"));
		nvps.add(new BasicNameValuePair("su", LoginMap.get("su")));
		nvps.add(new BasicNameValuePair("service", "miniblog"));
		nvps.add(new BasicNameValuePair("servertime", LoginMap.get("servertime")));
		nvps.add(new BasicNameValuePair("nonce", LoginMap.get("nonce")));
		nvps.add(new BasicNameValuePair("pwencode", "rsa2"));
		nvps.add(new BasicNameValuePair("rsakv", LoginMap.get("rsakv")));
		nvps.add(new BasicNameValuePair("sp", LoginMap.get("sp")));
		nvps.add(new BasicNameValuePair("sr", "1280*1024"));
		nvps.add(new BasicNameValuePair("encoding", "UTF-8"));
		nvps.add(new BasicNameValuePair("prelt", "222"));
		nvps.add(new BasicNameValuePair("url", "http://weibo.com/ajaxlogin.php?framelogin=1&callback=parent.sinaSSOController.feedBackUrlCallBack"));
		nvps.add(new BasicNameValuePair("returntype", "META"));
		
		for ( NameValuePair obj:nvps ) {
			System.out.println(obj.getName()+"\t"+obj.getValue());
		}
		System.out.println(LoginMap.get("sp"));
		httpost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
		response = httpclient.execute(httpost);
		String entity = EntityUtils.toString(response.getEntity());
		Document Doc = Jsoup.parse(entity);
		System.out.println(response.getStatusLine());
		System.out.println(Doc.toString());
		//String replaceUrl = entity.substring(entity.indexOf("location.replace('")+"location.replace('".length());
		httpget = new HttpGet("http://login.sina.com.cn/crossdomain2.php?action=login&entry=weibo&r=https://passport.weibo.com/wbsso/login?ssosavestate=1439263837&url=http%3A%2F%2Fweibo.com%2Fajaxlogin.php%3Fframelogin%3D1%26callback%3Dparent.sinaSSOController.feedBackUrlCallBack&ticket=ST-MjMxNDI4MzIzNQ==-1407727837-gz-2FF40AC562118C3D8E4D71ECC6806CC3&retcode=0&sr=1280*1024");
		response = httpclient.execute(httpget);
		Doc = Jsoup.parse(EntityUtils.toString(response.getEntity()));
		System.out.println(Doc.toString());
       // replaceUrl = replaceUrl.substring(0,replaceUrl.indexOf("'"));
        //System.out.println(replaceUrl);
//		String url = entity.substring(
//				entity.indexOf("http://weibo.com/sso/login.php?"),
//				entity.indexOf("code=0")+6);
//		httpget = new HttpGet(url);
//		response = httpclient.execute(httpget);
//		String html = EntityUtils.toString(response.getEntity());
//		System.out.println(html);
	}

}
