package cn.bliss.android.grabber;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Xml;
import cn.bliss.android.grabber.searcher.AbstractSearcher;
import cn.bliss.android.grabber.searcher.HttpSearcher;
import cn.bliss.android.grabber.searcher.PagingSearcher;
import cn.bliss.android.grabber.searcher.SimpleSearcher;

/**
 * 抓取配置
 * 
 * @author dragon
 * 
 */
public class Config {
	private int cfgResId;
	private Context context;
	private String userAgent;
	private List<Searcher> list;

	/**
	 * @param cfgResId
	 *            配置文件的资源id
	 */
	public Config(Context context, int cfgResId) {
		this.context = context;
		this.cfgResId = cfgResId;
	}

	public String getUserAgent() {
		return userAgent;
	}

	/**
	 * 获取抓取配置列表
	 * 
	 * @return
	 */
	public List<Searcher> list() {
		if (list != null)
			return list;

		list = new ArrayList<Searcher>();
		this.loadXml(context.getResources().openRawResource(cfgResId));
		return list;
	}

	public void setCfgResId(int cfgResId) {
		this.cfgResId = cfgResId;
	}

	private void loadXml(InputStream in) {
		try {
			AbstractSearcher searcher = null;
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, "utf-8");
			int event = parser.getEventType();
			String id, type, name;
			while (event != XmlPullParser.END_DOCUMENT) {
				name = parser.getName();
				switch (event) {
				case XmlPullParser.START_TAG:
					if ("userAgent".equals(name)) {
						this.userAgent = parser.nextText().toString();
					} else if ("searcher".equals(name)) {
						id = parser.getAttributeValue(0);
						type = parser.getAttributeValue(1);
						//System.out.println("id=" + id);
						//System.out.println("type=" + type);
						if ("simple".equals(type)) {
							searcher = new SimpleSearcher();
						} else if ("paging".equals(type)) {
							searcher = new PagingSearcher();
						}
						if (searcher != null)
							searcher.setId(id);

					} else if ("name".equals(name)) {
						searcher.setName(parser.nextText().toString());
					} else if ("path".equals(name)) {
						searcher.setPath(parser.nextText().toString());
					} else if ("url".equals(name)) {
						if (searcher instanceof HttpSearcher)
							((HttpSearcher) searcher).setUrl(parser.nextText()
									.toString());
					} else if ("selector".equals(name)) {
						if (searcher instanceof HttpSearcher)
							((HttpSearcher) searcher).setSelector(parser
									.nextText().toString());
					}
					break;
				case XmlPullParser.END_TAG:
					if ("searcher".equals(name)) {
						if (searcher != null)
							this.list.add(searcher);
					}
					break;
				default:
					break;
				}
				event = parser.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
