package org.akaza.openclinica.logic.rulerunner;

import org.akaza.openclinica.domain.rule.action.RuleActionBean;
import org.akaza.openclinica.domain.rule.action.ShowActionBean;

import java.util.ArrayList;
import java.util.HashMap;

public class MessageContainer {

    TheContainer container;
    HashMap<String, ArrayList<TheContainer>> groupOrdinalPLusItemOid2 = new HashMap<String, ArrayList<TheContainer>>();

    public void add(String groupOrdinalPLusItemOid, RuleActionBean ruleActionBean) {
        addInternal(groupOrdinalPLusItemOid, ruleActionBean.getSummary(), MessageType.ERROR);
        if (ruleActionBean instanceof ShowActionBean) {
            String[] oids = (((ShowActionBean) ruleActionBean).getOIDs()).split(",");
            for (String oid : oids) {
                addInternal(oid, ruleActionBean.getSummary(), MessageType.WARNING);
            }
        }
    }

    public void addInternal(String groupOrdinalPLusItemOid, String summary, MessageType messageType) {
        if (groupOrdinalPLusItemOid2.containsKey(groupOrdinalPLusItemOid)) {
            groupOrdinalPLusItemOid2.get(groupOrdinalPLusItemOid).add(new TheContainer(summary, messageType));
        } else {
            ArrayList<TheContainer> temp = new ArrayList<TheContainer>();
            temp.add(new TheContainer(summary, messageType));
            groupOrdinalPLusItemOid2.put(groupOrdinalPLusItemOid, temp);
        }
    }

    public HashMap<String, ArrayList<String>> getByMessageType(MessageType messageType) {
        HashMap<String, ArrayList<String>> h = new HashMap<String, ArrayList<String>>();
        for (String key : groupOrdinalPLusItemOid2.keySet()) {
            ArrayList<TheContainer> container = groupOrdinalPLusItemOid2.get(key);
            for (TheContainer theContainer : container) {
                if (theContainer.getType().equals(messageType)) {
                    if (h.get(key) != null) {
                        h.get(key).add(theContainer.getMessage());
                    } else {
                        ArrayList<String> a = new ArrayList<String>();
                        a.add(theContainer.getMessage());
                        h.put(key, a);
                    }
                }
            }
        }
        return h;
    }

    public enum MessageType {
        ERROR, WARNING
    }

    private class TheContainer {
        String message;
        MessageType type;

        public TheContainer(String message, MessageType type) {
            this.message = message;
            this.type = type;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public MessageType getType() {
            return type;
        }

        public void setType(MessageType type) {
            this.type = type;
        }

    }

}
