package com.awspure.system.test.main;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import com.awspure.system.common.entity.OrgUser;

public class TestWebService {
	private static OMFactory fac = OMAbstractFactory.getOMFactory();
    private static OMNamespace omNs;
    private static List<OrgUser> nameList = new ArrayList<OrgUser>();
    
	public static void main(String[] args) throws AxisFault {
		testWebService();
	}
    
	@SuppressWarnings("rawtypes")
	public static void testWebService() throws AxisFault{
		ServiceClient sc = new ServiceClient();
        Options opts = new Options();
        opts.setTo(new EndpointReference("http://172.20.107.240:8080/manage/html/SOAPServiceExt"));
        opts.setAction("syncEmployeeToAWS");
        sc.setOptions(opts);
        
        
        OMElement obj = setUserQuery("2016-08-19","2016-09-19");
        OMElement res = sc.sendReceive(obj);     
        
        res = res.getFirstElement();
        Iterator iterator = res.getChildElements();
        digui(iterator);
        /*while (iterator.hasNext()) {
        	OMElement omcol1 = (OMElement) iterator.next();
    		System.out.println(omcol1.toString());
    		System.out.println("----------");
        }
        Iterator iterator = res.getChildElements();
        int n=1;
        while (iterator.hasNext()) {
        	OMElement omcol = (OMElement) iterator.next();
        	Iterator iterator1 = omcol.getChildren();
        	while(iterator1.hasNext()){
        		OMElement omcol1 = (OMElement) iterator.next();
        		omcol1.getChildrenWithLocalName(arg0)
        		System.out.println(omcol1.toString());
            	System.out.println(n+"----------");
            	n++;
        	}
        }*/
	}
	
    @SuppressWarnings("rawtypes")
	public static List<OrgUser> digui(Iterator iterator){
   	 while (iterator.hasNext()) {
   		 OMElement omcol = (OMElement) iterator.next();
   		 if(omcol.getLocalName().equals("O_SYS_REFCUR")){
       		 Iterator rows = omcol.getChildElements();
       		while(rows.hasNext()){
      			OMElement row = (OMElement) rows.next();
      			Iterator contents = row.getChildElements();
      			OrgUser user = new OrgUser();
      			while(contents.hasNext()){
      				OMElement content = (OMElement) contents.next();
      				QName qName = new QName("name");
      				String name = content.getAttribute(qName).getAttributeValue();
      				String value = content.getText();
      				if("personCode".equals(name)){
      					user.setUserId(value);
      					user.setUserNo(value);
      					user.setPassWord("");
      				}else if("firstName".equals(name)){
      					user.setUserName(value);
      				}else if("positionCodeName".equals(name)){
      					user.setPositionName(value);
      				}else if("partyCode".equals(name)){
      					user.setDepartmentNo(value);
      				}else if("positionLevel".equals(name)){
      					user.setLevel(value);
      				}else if("statusId".equals(name)){
      					user.setStatus("1");
      				}
      			}
      			nameList.add(user);
      		 }
       		 break;
   		 }
   		 digui(omcol.getChildElements());
   	 }
   	 return nameList;
    }
	
   private static OMElement setUserQuery(String fromDate, String thruDate) {
       OMElement SendEmail = null;
       try {
    	   SendEmail = fac.createOMElement("syncEmployeeToAWS", omNs);
           OMElement mapMap = fac.createOMElement("map-Map", omNs);
           SendEmail.addChild(mapMap);
           mapMap.addChild(createMapEntry("fromDate", fromDate));
           mapMap.addChild(createMapEntry("thruDate", thruDate));
       } catch (Exception e) {
           e.printStackTrace();
       }
       return SendEmail;
    }
	
   private static OMElement createMapEntry(String key, String val) {

       OMElement mapEntry = fac.createOMElement("map-Entry", omNs);

       // create the key
       OMElement mapKey = fac.createOMElement("map-Key", omNs);
       OMElement keyElement = fac.createOMElement("std-String", omNs);
       OMAttribute keyAttribute = fac.createOMAttribute("value", null, key);

       mapKey.addChild(keyElement);
       keyElement.addAttribute(keyAttribute);

       // create the value
       OMElement mapValue = fac.createOMElement("map-Value", omNs);
       OMElement valElement = fac.createOMElement("std-String", omNs);
       OMAttribute valAttribute = fac.createOMAttribute("value", null, val);

       mapValue.addChild(valElement);
       valElement.addAttribute(valAttribute);

       // attach to map-Entry
       mapEntry.addChild(mapKey);
       mapEntry.addChild(mapValue);

       return mapEntry;
    }
}
