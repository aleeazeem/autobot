package com.autodesk.bsm.pelican.util;

import com.autodesk.ism.pelican.client.CustomHttpResponse;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import org.springframework.http.HttpStatus;

import java.io.Serializable;

/**
 * @author zaheer
 */
public class ChangeNotificationAuthResponse implements Serializable {
    private static final long serialVersionUID = 7967147817488773332L;

    private CustomHttpResponse response;

    public ChangeNotificationAuthResponse(final CustomHttpResponse response) {
        this.response = response;
    }

    public int getHttpStatus() {
        return response.getStatusCode();
    }

    public AuthDetails getResponse() {
        return new GsonBuilder().create().fromJson(response.getResponseBody(), AuthDetails.class);
    }

    public boolean isSuccessful() {
        final HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusCode());
        return httpStatus.is2xxSuccessful();
    }

    /**
     * JSON Serialization format
     *
     * @author zaheer
     */
    public static class AuthDetails implements Serializable {
        private static final long serialVersionUID = -5797913886469360218L;

        @SerializedName("token_type")
        private String tokenType;
        @SerializedName("expires_in")
        private int expiresIn;
        @SerializedName("access_token")
        private String accessToken;

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(final String tokenType) {
            this.tokenType = tokenType;
        }

        public int getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(final int expiresIn) {
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(final String accessToken) {
            this.accessToken = accessToken;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("AuthDetails [tokenType=");
            builder.append(tokenType);
            builder.append(", expiresIn=");
            builder.append(expiresIn);
            builder.append(", accessToken=");
            builder.append(accessToken);
            builder.append("]");
            return builder.toString();
        }
    }
}
