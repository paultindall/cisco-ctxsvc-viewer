package com.cisco.pt.ctxsvc.ctxviewer;

import com.cisco.thunderhead.client.ContextServiceClient;
import static com.cisco.thunderhead.client.Operation.AND;
import com.cisco.thunderhead.customer.Customer;
import com.cisco.thunderhead.pod.Pod;
import com.cisco.thunderhead.util.DataElementUtils;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;


@Theme("ctxviewertheme")
@Widgetset("com.cisco.pt.ctxviewer.CtxViewerWidgetset")
public class CtxViewerUi extends UI {

    enum HistProperties {TIMESTAMP, TYPE, NOTES, REFERENCE}

    CustomerForm cusform;
    Button btn_submit, btn_prev, btn_next; 
    CheckBox matchall;
    Label x_of_y;
    Table hist;

    List<Customer> cuslist;
    Customer cus;
    int cusindex;
    
    @Override
    protected void init(VaadinRequest vaadinRequest) {

// Page layout

        VerticalLayout pagelayout = new VerticalLayout();
        pagelayout.setMargin(new MarginInfo(true, false, false, true));
        pagelayout.addComponent(new Label("<h2><b>Cisco Cloud Context Service</b></h2>", ContentMode.HTML));
        setContent(pagelayout);
        getPage().setTitle("Context Service Browser");

// Customer form layout

        cusform = new CustomerForm();

        matchall = new CheckBox("Match All Fields", false);
        matchall.addStyleName("ctx-match-checkbox");
        
        x_of_y = new Label();
        x_of_y.addStyleName("ctx-label-x-of-y");
        x_of_y.setVisible(false);

// Customer contact history table layout

        hist = new Table("Contact History");
        hist.addStyleName("ctx-hist-table");
        hist.setSelectable(true);
        hist.setPageLength(12);
        hist.setSizeFull();
        hist.setHeight("547px");

        hist.addContainerProperty(HistProperties.TIMESTAMP, String.class, "", "Date / Time", null, null);
        hist.addContainerProperty(HistProperties.TYPE, String.class, "", "Type", null, null);
        hist.addContainerProperty(HistProperties.NOTES, String.class, 0, "Notes", null, null);
        hist.addContainerProperty(HistProperties.REFERENCE, String.class, 0, "Reference", null, null);
        
        hist.setColumnCollapsingAllowed(true);
        
// Buttons layout

        Button btn_search = new Button("Search");
        btn_search.addStyleName("ctx-button-search");
        btn_search.setHeight("2em");
        btn_search.addClickListener((ClickEvent event) -> search());

        btn_submit = new Button("Add");
        btn_submit.addStyleName("ctx-button-submit");
        btn_submit.setHeight("2em");            
        btn_submit.addClickListener((ClickEvent event) -> submit());

        Button btn_clear = new Button("Clear");
        btn_clear.addStyleName("ctx-button-reset");
        btn_clear.setHeight("2em");
        btn_clear.addClickListener((ClickEvent event) -> clear());

        btn_prev = new Button("<");
        btn_prev.addStyleName("ctx-button-traverse");
        btn_prev.setHeight("2em");
        btn_prev.setVisible(false);
        btn_prev.addClickListener((ClickEvent event) -> previous());

        btn_next = new Button(">");
        btn_next.addStyleName("ctx-button-traverse");
        btn_next.setHeight("2em");
        btn_next.setVisible(false);
        btn_next.addClickListener((ClickEvent event) -> next());

        HorizontalLayout button_row_1 = new HorizontalLayout();
        button_row_1.addStyleName("ctx-button-layout");
        button_row_1.setSpacing(true);
        button_row_1.addComponents(matchall, btn_prev, x_of_y, btn_next);

        HorizontalLayout button_row_2 = new HorizontalLayout();
        button_row_2.addStyleName("ctx-button-layout");
        button_row_2.setSpacing(true);
        button_row_2.addComponents(btn_search, btn_submit, btn_clear);

        cusform.addComponents(button_row_1, button_row_2);
        
// Do layout on main page        

        VerticalLayout lhs = new VerticalLayout(cusform);
        lhs.addStyleName("ctx-lhs-layout");
        lhs.setSpacing(true);
 
        VerticalLayout rhs = new VerticalLayout(hist);
        rhs.addStyleName("ctx-rhs-layout");
        rhs.setSpacing(true);
        rhs.setWidth("850px");
 
        HorizontalLayout mainlayout = new HorizontalLayout(lhs, rhs);
        mainlayout.setSpacing(true);
        pagelayout.addComponent(mainlayout);
        
// Now populate form with request params if specified and do a search

        init_search(vaadinRequest);
    }


    private void init_search(VaadinRequest req) {

        String p;

        if ((p = req.getParameter("firstname")) != null) cusform.firstname.setValue(p);
        if ((p = req.getParameter("lastname")) != null) cusform.lastname.setValue(p);
        if ((p = req.getParameter("street")) != null) cusform.street.setValue(p);
        if ((p = req.getParameter("city")) != null) cusform.city.setValue(p);
        if ((p = req.getParameter("country")) != null) cusform.country.setValue(p);
        if ((p = req.getParameter("phone")) != null) cusform.phone.setValue(p);
        if ((p = req.getParameter("mobile")) != null) cusform.mobile.setValue(p);
        if ((p = req.getParameter("email")) != null) cusform.email.setValue(p);
        if ((p = req.getParameter("account")) != null) cusform.account.setValue(p);
        
        if (!cusform.isEmpty()) {
            boolean all;
            matchall.setValue(all = req.getParameter("matchall") != null);
        
            searchAndShow(all);
        }
    }
    
    
    private void search() {

        if (cusform.isEmpty()) {
            (new Notification("Missing Search Criteria", "Enter at least one field", Notification.Type.WARNING_MESSAGE)).show(Page.getCurrent());            

        } else {            
            searchAndShow(matchall.getValue());
        }
    }

    
    private void searchAndShow(boolean allfields) {

            cuslist = cusform.search(allfields);
            
            if (cuslist.isEmpty()) {
                (new Notification("No Matching Customers", null, Notification.Type.WARNING_MESSAGE)).show(Page.getCurrent());

            } else {
                (new Notification("Customer Lookup", "Found " + cuslist.size() + " Matching Customers")).show(Page.getCurrent());
                cusindex = 0;
                cus = cuslist.get(cusindex);
                cusform.showCustomer(cus);
                btn_submit.setCaption("Update");

                showCustomerPods();
            }

            if (cuslist.size() > 1) {
                btn_prev.setVisible(true);
                btn_next.setVisible(true);
                x_of_y.setVisible(true);
                x_of_y.setValue("1 of " + cuslist.size());

            } else {
                btn_prev.setVisible(false);
                btn_next.setVisible(false);
                x_of_y.setVisible(false);
            }
    }


    private void showCustomerPods() {

        hist.removeAllItems();

        Map<String, String> podLookup = new TreeMap<String, String>() {
            {
                put("customerId", cus.getCustomerId().toString());
            }
        };
        List<Pod> podsByCustomer = CtxViewerUi.CtxViewerUiServlet.ctxcon.search(Pod.class, podLookup, AND);

        podsByCustomer.stream().forEach(pod -> {
            Map<String, Object> poddata = DataElementUtils.convertDataSetToMap(pod.getDataElements());
            String time = new SimpleDateFormat("d-MMM-y HH:mm:ss").format(pod.getLastUpdated().getDate());
            String type = pod.getMediaType();
            String notes = (String) poddata.get("Context_Notes");
            String link = (String) poddata.get("Context_POD_Activity_Link");
            
            hist.addItem(new Object[] {time, type, notes, link}, null);
        });
    }
    
    
    private void previous() {
        if (cusindex > 0) {
            cusindex--;
            cus = cuslist.get(cusindex);
            cusform.showCustomer(cus);
            showCustomerPods();
            x_of_y.setValue((cusindex + 1) + " of " + cuslist.size());
        }
    }
    
    
    private void next() {
        if (cusindex < cuslist.size() - 1) {
            cusindex++;
            cus = cuslist.get(cusindex);
            cusform.showCustomer(cus);
            showCustomerPods();
            x_of_y.setValue((cusindex + 1) + " of " + cuslist.size());
        }
    }


    private void clear() {
        if (cuslist != null) cuslist.clear();
        cusform.clear();
        hist.removeAllItems();
        btn_submit.setCaption("Add");
        btn_prev.setVisible(false);
        btn_next.setVisible(false);
        x_of_y.setVisible(false);
    }   
    
    
    private void submit() {

        String stsmsg = null;

        if (cusform.isEmpty()) {
            (new Notification("Missing Data", "Enter at least one field", Notification.Type.WARNING_MESSAGE)).show(Page.getCurrent());

        } else if (cuslist == null || cuslist.isEmpty()) {

// No current customer has been retrieved so create a new one

            cuslist = cusform.create();
            
            if (cuslist.isEmpty()) {
                (new Notification("New Customer Could Not Be Added", null, Notification.Type.ERROR_MESSAGE)).show(Page.getCurrent());

            } else {
                (new Notification("Customer Added", null, Notification.Type.HUMANIZED_MESSAGE)).show(Page.getCurrent());
                cusindex = 0;
                cus = cuslist.get(cusindex);
                cusform.showCustomer(cus);
                btn_submit.setCaption("Update");
            }

        } else {

// Update current customer record 

            cusform.update(cus);
            (new Notification("Customer Updated", null, Notification.Type.HUMANIZED_MESSAGE)).show(Page.getCurrent());
        }
    }
    

// Custom servlet class that Vaadin instantiates.
// Set the Context Service connection ContextMode servlet parameter to lab or production
    
    @WebServlet(urlPatterns = "/*",
                name = "CtxViewerUiServlet",
                asyncSupported = true,
                loadOnStartup = 1,
                initParams =
                {
                    @WebInitParam(name = "ContextMode", value = "lab"),
//                  @WebInitParam(name = "ContextMode", value = "production"),
                    @WebInitParam(name = "ConfigPath", value = "connector.property"),
                    @WebInitParam(name = "AppHostName", value = "test.ptlab.cisco.com"),
                    @WebInitParam(name = "ConnectionData", value = "connection.data")
                })
    @VaadinServletConfiguration(ui = CtxViewerUi.class, productionMode = false)

    public static class CtxViewerUiServlet extends VaadinServlet {

        static ContextServiceClient ctxcon; 

        @Override
        protected void servletInitialized() throws ServletException {

            super.servletInitialized();
            String host = getInitParameter("AppHostName");
            String key = getInitParameter("ConnectionData");
            String cfgpath = getInitParameter("ConfigPath");
            String mode = getInitParameter("ContextMode");

            ctxcon = (new CtxSvcConnection(host, key, cfgpath)).connect("lab".equals(mode));
            
            if (ctxcon == null) {
                throw new ServletException("Context Service connection could not be established");
            }
        }
    }
}