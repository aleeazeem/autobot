package com.autodesk.bsm.pelican.enums;

import com.google.gson.annotations.SerializedName;

/**
 * Job state for triggers
 *
 * @author jains
 */
public enum JobState {

    @SerializedName("in_progress")
    IN_PROGRESS,

    @SerializedName("complete")
    COMPLETE,

    @SerializedName("complete_with_failures")
    FAILED,

    UNKNOWN
}
