
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.NameValuePair;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@SuppressWarnings("deprecation")
public class LoginWeibo {
	
	private String LoginUrl = "http://login.weibo.cn/login/";
	private HttpClient httpclient = new DefaultHttpClient();
	private HttpResponse response = null;
	private String LoginUser = "*************";
	private String LoginPassword = "*****************";
	private String LoginVK = "";
	private String LoginBackURL = "";
	private String LoginBackTitle = "";
	private String LoginSubmit = "";
	
	private List<String> TempList = null;
	
	
	public static void main(String[] args) throws Exception {
		LoginWeibo loginWeibo = new LoginWeibo();
		loginWeibo.Login();
		
	}
	
	@SuppressWarnings("unused")
	public void Login() throws Exception {
		
		HttpPost httpost = new HttpPost( LoginUrl );
		Document Doc = Jsoup.connect(LoginUrl).timeout(50000)
				.userAgent("Mozilla/5.0 (X11; Linux i686) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
				.get();
		LoginBackURL = Doc.select("input[name=backURL]").attr("value");
		LoginBackTitle = Doc.select("input[name=backTitle]").attr("value");
		LoginVK = Doc.select("input[name=vk]").attr("value");
		LoginSubmit = Doc.select("input[name=submit]").attr("value");
		
		//System.out.println(LoginBackURL);
		//System.out.println(LoginBackTitle);
		//System.out.println(LoginVK);
		//System.out.println(LoginSubmit);
		//System.out.println("password_" + LoginVK.substring(0, 4));
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("mobile", LoginUser));
		nvps.add(new BasicNameValuePair("password_"+LoginVK.substring(0, 4), LoginPassword));
		nvps.add(new BasicNameValuePair("remember", "on"));
		nvps.add(new BasicNameValuePair("backURL", LoginBackURL));
		nvps.add(new BasicNameValuePair("backTitle", LoginBackTitle));
		nvps.add(new BasicNameValuePair("tryCount", ""));
		nvps.add(new BasicNameValuePair("vk", LoginVK));
		nvps.add(new BasicNameValuePair("submit", LoginSubmit));
		
		try {
			httpost.setEntity(new UrlEncodedFormEntity( nvps, "UTF-8" ));
			response = httpclient.execute(httpost);
			String HTML = EntityUtils.toString(response.getEntity());
			//System.out.println(HTML);
			//System.out.println(response.getStatusLine());
			String LoginSuccess = response.getFirstHeader("Location").getValue();
			//System.out.println(LoginSuccess);
			HttpGet httpget = new HttpGet(LoginSuccess);
			response = httpclient.execute(httpget);
			String LoginSuccessHTML = EntityUtils.toString(response.getEntity());
			//System.out.println(LoginSuccessHTML);
			Document LoginDoc = Jsoup.parse(LoginSuccessHTML);
			String NewLogin = LoginDoc.select("a[href]").attr("href");
			//System.out.println(NewLogin);
			httpget = new HttpGet(NewLogin);
			response = httpclient.execute(httpget);
			String NewLoginHTML = EntityUtils.toString(response.getEntity());
			//System.out.println(NewLoginHTML);
			//getFansLists( NewLoginHTML );
			//saveFansLists();
			//String Topic = "西邮Linux";
			//getTopic( NewLoginHTML, Topic );
			//saveTopic( Topic );
			String User = "王力宏";
			System.out.println(getUserWeibo( NewLoginHTML, User ));
			saveUserWeibo( User );
			
		} catch( Exception e ) {
			e.printStackTrace();
		}
		
		System.out.println("Success");
	}
	
	public void getFansLists( String NewLoginHTML ) throws Exception {
		TempList = new ArrayList<String>();
		Elements FansTags = Jsoup.parse(NewLoginHTML).select("div[class=tip2]").select("a[href]");
		String FansUrl = "";
		for ( Element FansTag:FansTags ) {
			if ( FansTag.text().indexOf("关注") >= 0 ) {
				FansUrl = FansTag.attr("href");
			}
		}
		FansUrl = "http://weibo.cn" + FansUrl;
		HttpGet httpget = new HttpGet(FansUrl);
		response = httpclient.execute(httpget);
		String FansListHTML = EntityUtils.toString(response.getEntity());
		Document FansListDoc = Jsoup.parse(FansListHTML);
		Elements FansListTables = FansListDoc.select("table");
		for ( Element FansListTable:FansListTables ) {
			String UserName = FansListTable.select("td[valign]").last().select("a[href]").first().text();
			System.out.println("UserName = " + UserName);
			TempList.add(UserName);
		}
		String NextPage = FansListDoc.getElementById("pagelist").select("a").first().attr("href");
		FansLists( "http://weibo.cn" + NextPage );
	}
	
	public void FansLists( String NextPage ) throws Exception {
		HttpGet httpget = new HttpGet(NextPage);
		response = httpclient.execute(httpget);
		String FansListHTML = EntityUtils.toString(response.getEntity());
		Document FansListDoc = Jsoup.parse(FansListHTML);
		Elements FansListTables = FansListDoc.select("table");
		for ( Element FansListTable:FansListTables ) {
			String UserName = FansListTable.select("td[valign]").last().select("a[href]").first().text();
			System.out.println("UserName = " + UserName);
			TempList.add(UserName);
		}
		if ( FansListDoc.getElementById("pagelist").select("a").first().text().compareTo("下页") != 0 ) {
			return;
		}
		NextPage = FansListDoc.getElementById("pagelist").select("a").first().attr("href");		
		FansLists( "http://weibo.cn" + NextPage );
	}
	
	public void saveFansLists() throws Exception {
		for ( String UserName:TempList ) {
			FileUtils.writeStringToFile(new File("FansList.txt"), UserName+"\r\n", "UTF-8", true);
		}
	}
	
	public void getTopic( String NewLoginHTML, String Topic ) throws Exception {
		TempList = new ArrayList<String>();
		Elements ClassNATags = Jsoup.parse(NewLoginHTML).select("div[class=n]").select("a[href]");
		String SearchURL = "";
		for ( Element Tag:ClassNATags ) {
			if ( Tag.text().indexOf("搜索") >= 0 ) {
				SearchURL = Tag.attr("href");
				break;
			}
		}
		HttpGet httpget = new HttpGet(SearchURL);
		response = httpclient.execute(httpget);
		String SearchPage = EntityUtils.toString(response.getEntity());
		String SearchPostUrl = "http://weibo.cn" + Jsoup.parse(SearchPage).select("form").attr("action");
		System.out.println(SearchPostUrl);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("keyword", Topic));
		nvps.add(new BasicNameValuePair("smblog", "搜微博"));
		
		HttpPost httpost = new HttpPost(SearchPostUrl);
		httpost.setEntity(new UrlEncodedFormEntity( nvps, "UTF-8" ));
		response = httpclient.execute(httpost);
		String TopicHTML = EntityUtils.toString(response.getEntity());
		ExtractInfo( TopicHTML );
	}
	
	public void ExtractInfo( String TopicHTML ) throws Exception {
		Document TopicDoc = Jsoup.parse(TopicHTML);
		Elements TagSpans = TopicDoc.select("span[class=ctt]");
		for ( Element TagSpan:TagSpans ) {
			String UserName = TagSpan.firstElementSibling().text();
			String WeiboText = TagSpan.text();
			Element TagSpanParent = TagSpan.parent();
			String WeiboFrom = TagSpanParent.select("span[class=ct]").text();
			if ( WeiboFrom.length() == 0) {
				WeiboFrom = TagSpanParent.parent().select("span[class=ct]").text();
			}
			TempList.add(UserName+WeiboText+"\r\n"+WeiboFrom);
			System.out.println(UserName+WeiboText);
			System.out.println(WeiboFrom);
			System.out.println();
		}
		
		Element NextTagA= TopicDoc.getElementById("pagelist").select("a[href]").first();
		System.out.println(NextTagA.parent().text());
		if ( NextTagA.text().compareTo("下页") != 0 ) {
			return;
		}
		String NextPage = "http://weibo.cn" + NextTagA.attr("href");
		System.out.println("NextPage = " + NextPage);
		HttpGet httpget = new HttpGet(NextPage);
		response = httpclient.execute(httpget);
		TopicHTML = EntityUtils.toString(response.getEntity());
		ExtractInfo( TopicHTML );
	}
	
	public void saveTopic( String Topic ) throws Exception {
		for ( String TopicInfo:TempList ) {
			FileUtils.writeStringToFile(new File("Topic-" + Topic), TopicInfo+"\r\n\r\n", "UTF-8", true);
		}
	}

	public boolean getUserWeibo( String NewLoginHTML, String User ) throws Exception {
		TempList = new ArrayList<String>();
		Elements ClassNATags = Jsoup.parse(NewLoginHTML).select("div[class=n]").select("a[href]");
		String SearchURL = "";
		for ( Element Tag:ClassNATags ) {
			if ( Tag.text().indexOf("搜索") >= 0 ) {
				SearchURL = Tag.attr("href");
				break;
			}
		}
		if ( SearchURL.length() == 0 ) {
			Elements SearchURLs = Jsoup.parse(NewLoginHTML).body().select("a[href]");
			for ( Element SearchURLnode:SearchURLs ) {
				if ( SearchURLnode.text().compareTo("搜索") == 0 ) {
					SearchURL = SearchURLnode.attr("href");
					break;
				}
			}
		}
		//SearchURL = "http://weibo.cn/search/";
		System.out.println(Jsoup.parse(NewLoginHTML).toString());
		System.out.println("SearchURL = " + SearchURL);
		HttpGet httpget = new HttpGet(SearchURL);
		response = httpclient.execute(httpget);
		String SearchPage = EntityUtils.toString(response.getEntity());
		String SearchPostUrl = "http://weibo.cn" + Jsoup.parse(SearchPage).select("form").attr("action");
		//System.out.println(SearchPostUrl);
		
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("keyword", User));
		nvps.add(new BasicNameValuePair("suser", "找人"));
		
		HttpPost httpost = new HttpPost(SearchPostUrl);
		httpost.setEntity(new UrlEncodedFormEntity( nvps, "UTF-8" ));
		response = httpclient.execute(httpost);
		String UserHTML = EntityUtils.toString(response.getEntity());
		
		return FindUser( UserHTML, User );
	}
	
	public boolean FindUser( String UserHTML, String User ) throws Exception {
		
		System.out.println(UserHTML);
		Document FindDoc = Jsoup.parse(UserHTML);
		
		String isFinded = FindDoc.body().text();
		if ( isFinded.indexOf("抱歉，未找到") >= 0 ) {
			System.out.println(isFinded);
			return false;
		}
		try {
			Elements UserTables = FindDoc.select("table");
			Element UserTable = UserTables.first();
			Element firstUserNode = UserTable.select("td[valign]").last().select("a[href]").first();
			System.out.println("匹配率最高：" + firstUserNode.text() + "\tURL: " + firstUserNode.attr("href"));
			
			String defineUser = firstUserNode.text();
			String defineUserURL = firstUserNode.attr("href");
			for ( Element userTable:UserTables ) {
				Element TempNode = userTable.select("td[valign]").last().select("a[href]").first();
				if ( TempNode.text().compareTo(User) == 0 ) {
					defineUser = TempNode.text();
					defineUserURL = TempNode.attr("href");
					break;
				}
			}
			defineUserURL = "http://weibo.cn" + defineUserURL;
			System.out.println("最终抓取用户：" + defineUser + "\tURL: " + defineUserURL );
			return ExtractUserWeibo( defineUserURL, User );
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	public boolean ExtractUserWeibo( String UserPageUrl, String UserName ) throws Exception {
		HttpGet httpget = new HttpGet( UserPageUrl );
		response = httpclient.execute(httpget);
		String UserPageHTML = EntityUtils.toString(response.getEntity());
		System.out.println(UserPageHTML);
		
		Document UserDoc = null;
		try {
			UserDoc = Jsoup.parse(UserPageHTML);
			Elements TagSpans = UserDoc.select("span[class=ctt]");
			for ( Element TagSpan:TagSpans ) {
				String WeiboText = TagSpan.text();
				Element TagSpanParent = TagSpan.parent();
				String WeiboFrom = TagSpanParent.select("span[class=ct]").text();
				if ( WeiboFrom.length() == 0) {
					WeiboFrom = TagSpanParent.parent().select("span[class=ct]").text();
				}
				TempList.add(UserName+": " + WeiboText+"\r\n"+WeiboFrom);
				System.out.println(UserName+": " + WeiboText);
				System.out.println(WeiboFrom);
				System.out.println();
			}
		} catch( Exception e ) {
			System.out.println(e);
		}
		
		Element NextTagA = null;
		try {
			NextTagA= UserDoc.getElementById("pagelist").select("a[href]").first();
		} catch( NullPointerException e ) {
			return false;
		}
		
		if ( NextTagA.text().compareTo("下页") != 0 ) {
			return true;
		}
		System.out.println(NextTagA.parent().text());
		String NextPage = "http://weibo.cn" + NextTagA.attr("href");
		System.out.println("NextPage = " + NextPage);
		
		return ExtractUserWeibo( NextPage, UserName );
	}
	
	public void saveUserWeibo( String UserName ) throws Exception {
		for ( String WeiboInfo:TempList ) {
			FileUtils.writeStringToFile(new File("Weibo-" + UserName), WeiboInfo, "UTF-8", true);
		}
	}
}
