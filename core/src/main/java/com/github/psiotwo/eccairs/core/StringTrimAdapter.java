package com.github.psiotwo.eccairs.core;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringTrimAdapter extends XmlAdapter<String, String> {

    @Override
    public String unmarshal(String v) {
        if (v == null)
            return null;
        return v.trim();
    }

    @Override
    public String marshal(String v) {
        if (v == null)
            return null;
        return v.trim();
    }
}