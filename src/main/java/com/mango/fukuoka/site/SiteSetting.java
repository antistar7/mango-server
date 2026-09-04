package com.mango.fukuoka.site;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "site_setting")
public class SiteSetting {

    @Id
    @Column(name = "setting_key", length = 100)
    private String settingKey;

    @Column(name = "setting_value", length = 1000)
    private String settingValue;

    protected SiteSetting() {
    }

    public static SiteSetting of(String key, String value) {
        SiteSetting setting = new SiteSetting();
        setting.settingKey = key;
        setting.settingValue = value;
        return setting;
    }

    public String getSettingKey() {
        return settingKey;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }
}
