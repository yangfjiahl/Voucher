package com.mandou.voucher;

import java.util.Date;

public class ActionModel {

    private String actionType = "NAVIGATE";

    private String pageName;

    private String eventName;

    private String attachData;

    private Date gmtOccur;

    private boolean isPaymentPage;

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

    public String getAttachData() {
        return attachData;
    }

    public void setAttachData(String attachData) {
        this.attachData = attachData;
    }

    public Date getGmtOccur() {
        return gmtOccur;
    }

    public void setGmtOccur(Date gmtOccur) {
        this.gmtOccur = gmtOccur;
    }

    public boolean isPaymentPage() {
        return isPaymentPage;
    }

    public void setPaymentPage(boolean paymentPage) {
        isPaymentPage = paymentPage;
    }
}
