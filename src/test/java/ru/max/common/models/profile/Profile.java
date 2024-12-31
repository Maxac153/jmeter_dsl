package ru.max.common.models.profile;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.ArrayList;

@Data
public class Profile {
    @SerializedName("THREAD_GROUP_NAME")
    private String threadGroupName;
    @SerializedName("SCRIPT_EXECUTION_TIME")
    private Integer scriptExecutionTime;
    @SerializedName("MAX_THREADS")
    private Integer maxThreads;
    @SerializedName("PACING_MULTIPLIER")
    private Integer pacingMultiplier;
    @SerializedName("STEPS")
    private ArrayList<Step> steps;
}
