package com.mandou.voucher;

import java.util.Map;

public class ActionModel {

    private String actionType = "NAVIGATE";

    private String pageName;

    private String eventName;

    private boolean isPaymentPage;

    private Map<String, Object> attachData;

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public boolean isPaymentPage() {
        return isPaymentPage;
    }

    public void setPaymentPage(boolean paymentPage) {
        isPaymentPage = paymentPage;
    }

    public void setAttachData(Map<String, Object> attachData) {
        this.attachData = attachData;
    }

    public Map<String, Object> getAttachData() {
        return attachData;
    }
}
