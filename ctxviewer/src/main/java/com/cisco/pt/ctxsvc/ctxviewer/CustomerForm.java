package com.cisco.pt.ctxsvc.ctxviewer;

import static com.cisco.thunderhead.client.Operation.AND;
import static com.cisco.thunderhead.client.Operation.OR;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.util.DataElementUtils;
import com.sun.jersey.api.client.ClientResponse;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CustomerForm extends FormLayout {

    TextField firstname;
    TextField lastname;
    TextField street;
    TextField city;
    TextField country;
    TextField phone;
    TextField mobile;
    TextField email;
    TextField account;

    public CustomerForm() {

        super.addStyleName("ctx-lhs-form");
        super.setCaption("Customer");
        super.setMargin(new MarginInfo(false, true, false, false));

        firstname = new TextField("First Name");
        firstname.setNullRepresentation("...");

        lastname = new TextField("Last Name");
        lastname.setNullRepresentation("...");

        street = new TextField("Street");
        street.setNullRepresentation("...");

        city = new TextField("City");
        city.setNullRepresentation("...");

        country = new TextField("Country");
        country.setNullRepresentation("...");

        phone = new TextField("Phone");
        phone.setNullRepresentation("...");

        mobile = new TextField("Mobile");
        mobile.setNullRepresentation("...");

        email = new TextField("Email");
        email.setNullRepresentation("...");

        account = new TextField("Account");
        account.setNullRepresentation("...");
        
        super.addComponents(firstname, lastname, street, city, country, phone, mobile, email, account);
    }

    void clear() {
        firstname.setValue("");
        lastname.setValue("");
        street.setValue("");
        city.setValue("");
        country.setValue("");
        phone.setValue("");
        mobile.setValue("");
        email.setValue("");
        account.setValue("");
    }
    
    boolean isEmpty() {
        return (firstname.isEmpty() && 
                lastname.isEmpty() && 
                street.isEmpty() && 
                city.isEmpty() && 
                country.isEmpty() && 
                phone.isEmpty() && 
                mobile.isEmpty() && 
                email.isEmpty() && 
                account.isEmpty());
    }

    List<Customer> search(boolean match_all_fields) {

        Map<String, String> lookupParams = new TreeMap<>();

        if (!firstname.isEmpty()) lookupParams.put("Context_First_Name", firstname.getValue());
        if (!lastname.isEmpty())  lookupParams.put("Context_Last_Name", lastname.getValue());
        if (!street.isEmpty())    lookupParams.put("Context_Street_Address_1", street.getValue()); 
        if (!city.isEmpty())      lookupParams.put("Context_City", city.getValue());
        if (!country.isEmpty())   lookupParams.put("Context_Country", country.getValue()); 
        if (!phone.isEmpty())     lookupParams.put("Context_Work_Phone", phone.getValue());
        if (!mobile.isEmpty())    lookupParams.put("Context_Mobile_Phone", mobile.getValue());
        if (!email.isEmpty())     lookupParams.put("Context_Work_Email", email.getValue());
        if (!account.isEmpty())   lookupParams.put("Context_Customer_External_ID", account.getValue());
            
        return CtxViewerUi.CtxViewerUiServlet.ctxcon.search(Customer.class, lookupParams, match_all_fields ? AND : OR);
    }


    List<Customer> create() {

        Map<String, Object> createData = new TreeMap<>();

        if (!firstname.isEmpty()) createData.put("Context_First_Name", firstname.getValue());
        if (!lastname.isEmpty())  createData.put("Context_Last_Name", lastname.getValue());
        if (!street.isEmpty())    createData.put("Context_Street_Address_1", street.getValue()); 
        if (!city.isEmpty())      createData.put("Context_City", city.getValue());
        if (!country.isEmpty())   createData.put("Context_Country", country.getValue()); 
        if (!phone.isEmpty())     createData.put("Context_Work_Phone", phone.getValue());
        if (!mobile.isEmpty())    createData.put("Context_Mobile_Phone", mobile.getValue());
        if (!email.isEmpty())     createData.put("Context_Work_Email", email.getValue());
        if (!account.isEmpty())   createData.put("Context_Customer_External_ID", account.getValue());
            
        Customer cus = new Customer(DataElementUtils.convertDataMapToSet(createData));
        cus.setFieldsets(new ArrayList<>(Arrays.asList("cisco.base.customer")));
        ClientResponse rsp = CtxViewerUi.CtxViewerUiServlet.ctxcon.create(cus);

        cus = CtxViewerUi.CtxViewerUiServlet.ctxcon.get(Customer.class, rsp.getLocation().toString());

        List<Customer> newcuslist = new ArrayList();
        newcuslist.add(cus);

        return newcuslist;        
    }


    void update(Customer cus) {

        Map<String, Object> updateData = new TreeMap<>();

        if (!firstname.isEmpty()) updateData.put("Context_First_Name", firstname.getValue());
        if (!lastname.isEmpty())  updateData.put("Context_Last_Name", lastname.getValue());
        if (!street.isEmpty())    updateData.put("Context_Street_Address_1", street.getValue()); 
        if (!city.isEmpty())      updateData.put("Context_City", city.getValue());
        if (!country.isEmpty())   updateData.put("Context_Country", country.getValue()); 
        if (!phone.isEmpty())     updateData.put("Context_Work_Phone", phone.getValue());
        if (!mobile.isEmpty())    updateData.put("Context_Mobile_Phone", mobile.getValue());
        if (!email.isEmpty())     updateData.put("Context_Work_Email", email.getValue());
        if (!account.isEmpty())   updateData.put("Context_Customer_External_ID", account.getValue());

        Map<String, Object> existingData = DataElementUtils.convertDataSetToMap(cus.getDataElements());
        existingData.putAll(updateData);
        cus.setDataElements(DataElementUtils.convertDataMapToSet(existingData));
        cus.setFieldsets(new ArrayList<>(Arrays.asList("cisco.base.customer")));

        CtxViewerUi.CtxViewerUiServlet.ctxcon.update(cus);
    }


    void showCustomer(Customer cus) {
        Map<String, Object> cusdata = DataElementUtils.convertDataSetToMap(cus.getDataElements());

        firstname.setValue((String) cusdata.get("Context_First_Name"));
        lastname.setValue((String) cusdata.get("Context_Last_Name"));
        street.setValue((String) cusdata.get("Context_Street_Address_1"));
        city.setValue((String) cusdata.get("Context_City"));
        country.setValue((String) cusdata.get("Context_Country"));
        phone.setValue((String) cusdata.get("Context_Work_Phone"));
        mobile.setValue((String) cusdata.get("Context_Mobile_Phone"));
        email.setValue((String) cusdata.get("Context_Work_Email"));
        account.setValue((String) cusdata.get("Context_Customer_External_ID"));
    }
}
