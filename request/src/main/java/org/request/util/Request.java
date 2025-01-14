package org.request.util;

public class Request {
    private String command;
    private String userId;
    private String userName;
    private String userPassword;
    private String userLogin;
    private String bodyOfRequest;

    public String createRequest(String command, String... request){
        StringBuilder sb = new StringBuilder();
        sb.append(command).append(":");
        for (String part : request){
            sb.append(part).append(":");
        }
        return sb.toString();
    }
}
