package org.motechproject.workflow.commands;

import org.joda.time.DateTime;

public class CallToPlace {

    private String language;
    private String phoneNum;
    private String callFlowId;
    private String channel;
    private DateTime callTime;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getCallFlowId() {
        return callFlowId;
    }

    public void setCallFlowId(String callFlowId) {
        this.callFlowId = callFlowId;
    }

    public DateTime getCallTime() {
        return callTime;
    }

    public void setCallTime(DateTime callTime) {
        this.callTime = callTime;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
