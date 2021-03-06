package org.cobbzilla.util.string;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ResourceBundle;

@Slf4j
public abstract class ResourceMessages  {

    protected abstract String getBundleName();

    @Getter(lazy=true) private final ResourceBundle bundle = ResourceBundle.getBundle(getBundleName());

    public String translate(String messageTemplate) {

        // strip leading/trailing curlies if they are there
        while (messageTemplate.startsWith("{")) messageTemplate = messageTemplate.substring(1);
        while (messageTemplate.endsWith("}")) messageTemplate = messageTemplate.substring(0, messageTemplate.length()-1);

        try {
            return getBundle().getString(messageTemplate);
        } catch (Exception e) {
            log.error("translate: Error looking up "+messageTemplate+": "+e);
            return messageTemplate;
        }
    }

}
