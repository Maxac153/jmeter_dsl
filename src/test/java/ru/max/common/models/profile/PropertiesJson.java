package ru.max.common.models.profile;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.HashMap;

@Data
public class PropertiesJson {
    @SerializedName("PROPERTIES")
    private HashMap<String, String> properties;
}
