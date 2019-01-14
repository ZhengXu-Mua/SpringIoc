package classLoader;

import entity.Person;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BeanLoader {
	private Map<String , Object> beanMap = new HashMap<String, Object>();
	
	public void init(String xml){
		
		try {
			SAXReader reader = new SAXReader();
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			InputStream ins = classLoader.getResourceAsStream(xml);
			
			Document doc = reader.read(ins);
			Element root = doc.getRootElement();
			

			setBean(root);

			setPv(root);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setBean(Element root) throws Exception {
		for(Iterator i = root.elementIterator("bean");i.hasNext();){
			Element foo = (Element) i.next();
			
			String id = foo.attribute("id").getText();
			String cls = foo.attribute("class").getText();
			
			Class bean  = Class.forName(cls);
			Object obj = bean.newInstance();
			
			beanMap.put(id, obj);
		}
	}
	
	public void setPv(Element root) throws Exception{
		for (Iterator i = root.elementIterator(); i.hasNext();) {
			Element foo = (Element) i.next();
			
			String id = foo.attribute("id").getText();
			String cls = foo.attribute("class").getText();
			
			Class bean = Class.forName(cls);
			
			BeanInfo info = Introspector.getBeanInfo(bean);
			
			PropertyDescriptor pd[] = info.getPropertyDescriptors();
			
			for (Iterator iterator = foo.elementIterator("property"); iterator.hasNext();) {
				Element foo1 = (Element) iterator.next();
				String name = foo1.attribute("name").getText();
				String ref = foo1.attribute("ref").getText();
				
				for (int j = 0; j < pd.length; j++) {
					if(pd[j].getName().equalsIgnoreCase(name)){
						Method mSet = null;
						mSet = pd[j].getWriteMethod();
						mSet.invoke(beanMap.get(id), beanMap.get(ref));
					}
				}
				break;
			}
		}
	}
	
	public <T>T getBean(String beanName,Class<T>clz){
		
		return clz.cast(beanMap.get(beanName));
	}
	
	public static void main(String[] args) {
		BeanLoader beanLoader = new BeanLoader();
		beanLoader.init("spring.xml");
		Person person = beanLoader.getBean("Person",Person.class);
		System.out.println(person.eat());
	}
}
