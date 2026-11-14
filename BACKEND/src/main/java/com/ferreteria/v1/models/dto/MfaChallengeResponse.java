package com.ferreteria.v1.models.dto;

import java.util.List;

public class MfaChallengeResponse {
    private boolean mfaRequired;
    private List<String> methods;

    public MfaChallengeResponse(boolean mfaRequired, List<String> methods) {
        this.mfaRequired = mfaRequired;
        this.methods = methods;
    }

    public boolean isMfaRequired() { return mfaRequired; }
    public List<String> getMethods() { return methods; }
}
